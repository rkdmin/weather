package com.zerobase.weather.repository;

import com.zerobase.weather.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
@Transactional
public interface DiaryRepository extends JpaRepository<Diary, Integer> {
    List<Diary> findAllByDate(LocalDate date);

    // between은 사이의 데이터를 반환해줌
    List<Diary> findAllByDateBetween(LocalDate startDate, LocalDate endDate);

    // getFirst는 limit 1의 역할을 해줌
    Diary getFirstByDate(LocalDate date);

    void deleteAllByDate(LocalDate date);
}
