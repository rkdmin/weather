package com.zerobase.weather.controller;

import com.zerobase.weather.domain.Diary;
import com.zerobase.weather.model.DiaryInput;
import com.zerobase.weather.service.DiaryService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;


    @ApiOperation(value = "일기 텍스트와 날씨를 이용해서 DB에 일기 저장")// swagger 컨트롤러 설명
    @PostMapping("/create/diary")
    void createDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "yyyy-MM-dd") LocalDate date,
                     @RequestBody DiaryInput diaryInput){

        diaryService.createDiary(date, diaryInput.getText());
    }


    @ApiOperation(value = "선택한 날짜의 모든 일기 데이터를 가져옵니다.")
    @GetMapping("/read/diary")
    List<Diary> readDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "yyyy-MM-dd") LocalDate date){

        return diaryService.readDiary(date);
    }

    @ApiOperation(value = "선택한 날짜 기간의 모든 일기 데이터를 가져옵니다.")
    @GetMapping("/read/diaries")
    List<Diary> readDiaries(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "yyyy-MM-dd") LocalDate startDate,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "yyyy-MM-dd") LocalDate endDate){

        return diaryService.readDiaries(startDate, endDate);
    }

    @ApiOperation(value = "선택한 날짜의 일기를 수정합니다.")
    @PutMapping("/update/diary")
    void updateDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "yyyy-MM-dd") LocalDate date,
                     @RequestBody DiaryInput diaryInput){
        System.out.println(diaryInput.getText());
        diaryService.updateDiary(date, diaryInput.getText());
    }

    @ApiOperation(value = "선택한 날짜의 일기 데이터를 삭제합니다.")
    @DeleteMapping("/delete/diary")
    void deleteDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "yyyy-MM-dd") LocalDate date){
        diaryService.deleteDiary(date);
    }

}
