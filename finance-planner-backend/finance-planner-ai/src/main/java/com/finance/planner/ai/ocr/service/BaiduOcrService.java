package com.finance.planner.ai.ocr.service;

public interface BaiduOcrService {

    /**
     * Recognize text from an image
     * @param imageBytes image file bytes
     * @return recognized text
     */
    String recognizeText(byte[] imageBytes);
}
