package com.finance.planner.ai.ocr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ocr.baidu")
public class BaiduOcrProperties {

    private String apiKey = "";
    private String secretKey = "";
    private String tokenUrl = "https://aip.baidubce.com/oauth/2.0/token";
    private String ocrUrl = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic";
}
