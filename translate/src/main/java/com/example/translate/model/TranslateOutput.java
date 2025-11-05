package com.example.translate.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TranslateOutput {
    Boolean success;
    String translation;
    @JsonProperty("word_document_url")
    String wordDocumentUrl;
    List<Vocabulary> vocabulary;
}

