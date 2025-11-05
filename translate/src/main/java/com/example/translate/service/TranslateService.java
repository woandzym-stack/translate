package com.example.translate.service;

import com.example.translate.model.TranslateInput;
import com.example.translate.model.TranslateOutput;

public interface TranslateService {
    /**
     * 处理翻译的核心逻辑
     * @param input
     * @return
     */
    TranslateOutput translate(TranslateInput input) ;
}
