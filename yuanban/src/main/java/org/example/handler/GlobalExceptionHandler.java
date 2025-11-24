package org.example.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 处理Ollama API调用异常
    @ExceptionHandler(RestClientException.class)
    @ResponseBody
    public Map<String, String> handleOllamaException(RestClientException e) {
        Map<String, String> result = new HashMap<>();
        result.put("code", "500");
        result.put("message", "AI医疗服务调用失败：" + e.getMessage());
        return result;
    }

    // 其他全局异常处理...
}