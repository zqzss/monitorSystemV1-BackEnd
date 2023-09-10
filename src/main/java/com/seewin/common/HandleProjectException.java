package com.seewin.common;

import com.seewin.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class HandleProjectException {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result exceptionHandle(Exception e) {
        e.printStackTrace();
        return new Result(500, null, e.toString());
    }

    @ExceptionHandler(BusinessException.class)
    public Result businessExceptionHandle(BusinessException e) {
        return new Result<>(e.getCode(), null,e.toString());
    }

    @ExceptionHandler(TokenException.class)
    public Result TokenExceptionHandle(TokenException e) {
        return new Result<>(e.getCode(), null, e.getMessage());
    }

}
