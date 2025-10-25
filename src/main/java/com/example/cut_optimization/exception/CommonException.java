package com.example.cut_optimization.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
public class CommonException extends RuntimeException {
  String message;
  HttpStatus httpStatus;
}

