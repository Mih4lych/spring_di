package com.moh4lych.springdi.controller;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class CustomErrorController {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity methodArgumentNotValidExceptionResponseEntity(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errorList = ex.getFieldErrors().stream().map(fieldError -> {
            Map<String, String> map = new HashMap<>();
            map.put(fieldError.getField(), fieldError.getDefaultMessage());
            return map;
        }).toList();

        return ResponseEntity.badRequest().body(errorList);
    }
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity transactionHandler(TransactionSystemException ex) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.badRequest();

        if (ex.getCause().getCause() instanceof ConstraintViolationException exConstr) {
            List<Map<String, String>> error = exConstr.getConstraintViolations().stream().map(violation -> {
                Map<String, String> map = new HashMap<>();
                map.put(violation.getPropertyPath().toString(), violation.getMessage());
                return map;
            }).toList();

            builder.body(error);
        }

        return builder.build();
    }
}
