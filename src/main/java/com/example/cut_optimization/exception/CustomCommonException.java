package com.example.cut_optimization.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomCommonException {

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ErrorResponse> handleException(CommonException e){
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()),
                e.getHttpStatus());
    }
}
