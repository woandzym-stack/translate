package com.example.translate.model;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TranslateInput {
    private String text;

    @JsonProperty("output_format")
    private String outputFormat;

    @JsonProperty("include_vocabulary")
    private String includeVocabulary;  // 使用String接收

    public Boolean isIncludeVocabulary() {
        if (includeVocabulary == null) return false;
        String value = includeVocabulary.toString().toLowerCase();
        return "true".equals(value);
    }
}