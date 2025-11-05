package com.example.translate.web;


import com.example.translate.model.Enum.OutputFormat;
import com.example.translate.model.TranslateInput;
import com.example.translate.model.TranslateOutput;
import com.example.translate.service.TranslateService;
import com.example.translate.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;

@Controller
@RequestMapping("/api/")
@Slf4j
public class TranslateController {

    @Autowired
    private TranslateService translateService;

    @PostMapping("v1/translate")
    @ResponseBody
    public TranslateOutput translate(@RequestBody TranslateInput input) {
        TranslateOutput output = new TranslateOutput();
        try{
            if(StringUtils.isEmpty(input.getText())|| StringUtils.isEmpty(input.getText().trim())) {
                throw new IllegalArgumentException("请输入要翻译的文本");
            }
            if(OutputFormat.getFormat(input.getOutputFormat()) == null) {
                throw new IllegalArgumentException("不支持的输出格式："+input.getOutputFormat());
            }
            if(!StringUtils.equals(input.getIncludeVocabulary(), "true")&& !StringUtils.equals(input.getIncludeVocabulary(), "false")) {
                throw new IllegalArgumentException("include_vocabulary不支持的参数值："+input.getIncludeVocabulary());
            }

            return translateService.translate(input);
        }catch (Exception e) {
            log.error("处理问题时出现异常", e);
            output.setSuccess(Boolean.FALSE);
            output.setTranslation(e.getMessage());
            return output;
        }
    }

    @GetMapping("/download")
    public void fileDownload(String fileUrl, HttpServletResponse response, HttpServletRequest request){
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        try {
            FileUtils.setAttachmentResponseHeader(response, fileUrl);
            FileUtils.writeBytes(fileUrl, response.getOutputStream());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
