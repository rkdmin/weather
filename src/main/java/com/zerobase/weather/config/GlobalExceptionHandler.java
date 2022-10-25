package com.zerobase.weather.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice// 전체 컨트롤러에 적용
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)// 500번에러 반환
    @ExceptionHandler(Exception.class)// 해당 컨트롤러의 오류를 잡아줌
    public Exception handleAllException(){
        System.out.println("error발생");
        return new Exception();
    }
}
