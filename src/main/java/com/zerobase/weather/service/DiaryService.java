package com.zerobase.weather.service;

import com.zerobase.weather.WeatherApplication;
import com.zerobase.weather.domain.DateWeather;
import com.zerobase.weather.domain.Diary;
import com.zerobase.weather.repository.DateWeatherRepository;
import com.zerobase.weather.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;
    @Value("${openweathermap.key}")
    private String apiKey;
    private static final Logger logger = LoggerFactory.getLogger(WeatherApplication.class);

    /**
     * 다이어리 생성
     */
    @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
    public void createDiary(LocalDate date, String text){
        // 캐시를 사용하기에 이제 api 안불러도 된다.
        // Map<String, Object> parsedWeather = getWeatherParsed();

        logger.info("started to create diary");
        // 날씨 데이터 가져오기 (Api or DB)
        DateWeather dateWeather = getDateWeather(date);

        // 일기 db에 넣기
        Diary nowDiary = new Diary();
        nowDiary.setDateWeather(dateWeather);
        nowDiary.setText(text);
        diaryRepository.save(nowDiary);
        logger.info("end to create diary");
    }

    /**
     * 다이어리 조회 1개의 날짜
     */
    public List<Diary> readDiary(LocalDate date) {
        logger.debug("read diary");
        return diaryRepository.findAllByDate(date);
    }

    /**
     * 다이어리 조회 범위 날짜
     */
    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        logger.debug("read diaries");
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    /**
     * 다이어리 수정
     */
    @Transactional(readOnly = false)
    public void updateDiary(LocalDate date, String text) {
        logger.debug("started to update diary");
        Diary diary = diaryRepository.getFirstByDate(date);
        diary.setText(text);

        diaryRepository.save(diary);
        logger.debug("end to update diary");

    }

    /**
     * 다이어리 삭제
     */
    @Transactional(readOnly = false)
    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
        logger.debug("delete diary");
    }

    /**
     * 매일 1시에 날씨 저장
     */
    @Scheduled(cron = "0 0 1 * * *")// 초분시일월년
    @Transactional(readOnly = false)
    public void saveWeatherDate(){
        logger.info("1시에 날씨 데이터 가져옴");
        dateWeatherRepository.save(getWeatherFromApi());
    }

    private DateWeather getWeatherFromApi(){
        String weatherData = getWeatherString();
        Map<String, Object> parsedWeather = parseWeather(weatherData);

        return DateWeather.builder()
            .date(LocalDate.now())
            .weather(parsedWeather.get("main").toString())
            .icon(parsedWeather.get("icon").toString())
            .temperature((Double) parsedWeather.get("temp"))
            .build();
    }

    private String getWeatherString(){
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=ansan&appid=" + apiKey;

        try{
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            BufferedReader br;
            if(responseCode == 200){
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }else{
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            String inputLine;
            StringBuilder response = new StringBuilder();
            while((inputLine = br.readLine()) != null){
                response.append(inputLine);
            }
            br.close();

            return response.toString();

        } catch (Exception e) {
            return "failed to get response";
        }
    }

    private Map<String, Object> parseWeather(String jsonString){
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try{
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        }catch (ParseException e){
            throw new RuntimeException(e);
        }

        Map<String, Object> resultMap = new HashMap<>();

        // "main":{"temp":298.81, "feels_like":298.76 ...}
        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));
        JSONObject weatherData =
            (JSONObject) ((JSONArray) jsonObject.get("weather")).get(0);
        resultMap.put("main", weatherData.get("main"));
        resultMap.put("icon", weatherData.get("icon"));
        return resultMap;

    }

    private DateWeather getDateWeather(LocalDate date) {
        List<DateWeather> dateWeatherListFromDb = dateWeatherRepository.findAllByDate(date);
        if(dateWeatherListFromDb.size() == 0){// 캐시에 값이 없는 경우
            // 새로 api에서 날씨 정보를 가져와야한다.
            return getWeatherFromApi();
        }

        return dateWeatherListFromDb.get(0);
    }
}

