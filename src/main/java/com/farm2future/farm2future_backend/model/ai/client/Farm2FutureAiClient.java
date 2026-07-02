package com.farm2future.farm2future_backend.model.ai.client;

import com.farm2future.farm2future_backend.model.ai.dto.AiEvaluateRequest;
import com.farm2future.farm2future_backend.model.ai.dto.AiEvaluateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class Farm2FutureAiClient {

    private final RestTemplate restTemplate;

    @Value("${farm2future.ai.evaluate-url}")
    private String evaluateUrl;

    public AiEvaluateResponse evaluate(AiEvaluateRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AiEvaluateRequest> entity = new HttpEntity<>(request, headers);

        log.info("Calling Farm2FutureAI evaluate API, farmId={}, period={}",
                request.getFarmId(),
                request.getPeriod());

        return restTemplate.postForObject(
                evaluateUrl,
                entity,
                AiEvaluateResponse.class
        );
    }
}