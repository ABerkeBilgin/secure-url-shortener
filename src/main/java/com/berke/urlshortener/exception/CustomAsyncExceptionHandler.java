package com.berke.urlshortener.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.lang.NonNull; 

import java.lang.reflect.Method;
import java.util.Arrays; 

@Slf4j
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(@NonNull Throwable ex, @NonNull Method method, @NonNull Object... params) {

        String paramStr = Arrays.toString(params);
        if (ex instanceof ShortUrlNotFoundException) {
            log.warn("Async Warning: {} | Method: {} | Params: {}", 
                ex.getMessage(), method.getName(), paramStr);
        } else {
            log.error("ASYNC CRITICAL ERROR: Method: {} | Params: {}", 
                    method.getName(), paramStr, ex);
        }
    }
}