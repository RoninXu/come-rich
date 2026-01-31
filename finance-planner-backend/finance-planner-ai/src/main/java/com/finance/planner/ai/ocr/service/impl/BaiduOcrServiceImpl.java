package com.finance.planner.ai.ocr.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.planner.ai.ocr.config.BaiduOcrProperties;
import com.finance.planner.ai.ocr.service.BaiduOcrService;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class BaiduOcrServiceImpl implements BaiduOcrService {

    private final BaiduOcrProperties properties;
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;

    private final AtomicReference<String> accessToken = new AtomicReference<>();

    @Override
    public String recognizeText(byte[] imageBytes) {
        if (properties.getApiKey().isBlank() || properties.getSecretKey().isBlank()) {
            throw new BusinessException(ErrorCode.OCR_RECOGNITION_FAILED, "OCR服务未配置API密钥");
        }

        try {
            String token = getAccessToken();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String body = "image=" + URLEncoder.encode(base64Image, StandardCharsets.UTF_8);

            WebClient webClient = webClientBuilder.build();
            String response = webClient.post()
                    .uri(properties.getOcrUrl() + "?access_token=" + token)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseOcrResponse(response);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Baidu OCR recognition failed: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OCR_RECOGNITION_FAILED);
        }
    }

    private String getAccessToken() {
        String token = accessToken.get();
        if (token != null) {
            return token;
        }

        try {
            WebClient webClient = webClientBuilder.build();
            String response = webClient.post()
                    .uri(properties.getTokenUrl()
                            + "?grant_type=client_credentials"
                            + "&client_id=" + properties.getApiKey()
                            + "&client_secret=" + properties.getSecretKey())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode json = objectMapper.readTree(response);
            token = json.get("access_token").asText();
            accessToken.set(token);
            return token;
        } catch (Exception e) {
            log.error("Failed to get Baidu OCR access token: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OCR_RECOGNITION_FAILED, "OCR服务认证失败");
        }
    }

    private String parseOcrResponse(String response) {
        try {
            JsonNode json = objectMapper.readTree(response);
            JsonNode wordsResult = json.get("words_result");
            if (wordsResult == null || !wordsResult.isArray() || wordsResult.isEmpty()) {
                throw new BusinessException(ErrorCode.OCR_EMPTY_RESULT);
            }

            StringBuilder sb = new StringBuilder();
            for (JsonNode item : wordsResult) {
                sb.append(item.get("words").asText()).append("\n");
            }
            return sb.toString().trim();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse OCR response: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OCR_RECOGNITION_FAILED);
        }
    }
}
