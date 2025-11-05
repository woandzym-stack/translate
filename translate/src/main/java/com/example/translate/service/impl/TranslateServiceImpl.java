package com.example.translate.service.impl;

import com.example.translate.model.Enum.OutputFormat;
import com.example.translate.model.TranslateInput;
import com.example.translate.model.TranslateOutput;
import com.example.translate.model.Vocabulary;
import com.example.translate.model.word.DynamicTable;
import com.example.translate.model.word.TableStyle;
import com.example.translate.service.TranslateService;
import com.example.translate.thirdpart.DeepSeekProcessor;
import com.example.translate.util.AdvancedWordGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Service
@Slf4j
public class TranslateServiceImpl implements TranslateService {

    @Autowired
    DeepSeekProcessor deepSeekProcessor;

    @Autowired
    private ResourceLoader resourceLoader;

    private static final String DOWNLOAD_DIR = "download";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public TranslateOutput translate(TranslateInput input) {
        TranslateOutput translateOutput = new TranslateOutput();
        String prompt = preparePrompt(input);

        String result;
        try {
          result = deepSeekProcessor.processQuestion("",prompt+"The text is:" + input.getText());
        } catch (Exception e) {
            log.error("调用 DeepSeek失败", e);
            translateOutput.setSuccess(Boolean.FALSE);
            translateOutput.setTranslation("调用DeepSeek失败");
            return translateOutput;
        }

        result = removeMarkdownCodeBlock(result);
        try {
            translateOutput = OBJECT_MAPPER.readValue(result, TranslateOutput.class);
        } catch (Exception e) {
            log.error("处理 DeepSeek返回值时出现异常，原始返回：{}", result, e);
            translateOutput.setSuccess(Boolean.FALSE);
            translateOutput.setTranslation("处理DeepSeek返回值时出现异常");
            return translateOutput;
        }

        if(!translateOutput.getSuccess()){
            translateOutput.setTranslation("翻译失败，输入不是英文");
            return translateOutput;
        }

        if(StringUtils.equals(input.getOutputFormat(), OutputFormat.WORD.getCode())) {
            //生成word文档
            try{
                String outputPath = getPath();
                generateWordDocument(input, translateOutput,outputPath);
                translateOutput.setWordDocumentUrl(outputPath);
            }catch (Exception e) {
                log.error("生成word文档时出现异常", e);
                translateOutput.setSuccess(Boolean.FALSE);
                translateOutput.setTranslation("生成word文档失败");
                translateOutput.setWordDocumentUrl(null);
            }
        }
        return translateOutput;
    }

    /**
     * 生成prompt
     * @param input
     * @return
     */
    private String preparePrompt(TranslateInput input) {
        String promptUrl = null;
        if(input.isIncludeVocabulary()){
            promptUrl = "classpath:file/prompt-with-vocabulary.txt";
        }else{
            promptUrl = "classpath:file/prompt-no-vocabulary.txt";
        }
        Resource resource = resourceLoader.getResource(promptUrl);
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            String fileContent = content.toString();
            return fileContent;
        } catch (IOException e) {
            log.error("读取prompt文件时出现异常", e);
            return null;
        }
    }

    /**
     * 有时会返回markdown格式的json，需要移除
     * @param jsonString
     * @return
     */
    private  String removeMarkdownCodeBlock(String jsonString) {
        // 移除开头的 ```json 和结尾的 ```
        return jsonString
                .replaceAll("^```json\\s*", "")  // 移除开头的 ```json
                .replaceAll("\\s*```$", "")      // 移除结尾的 ```
                .trim();
    }

    /**
     * 生成结果的word文档
     *
     * @param translateInput 翻译输入
     * @param translateOutput 翻译输出
     * @param outputPath 文档输出路径
     * @throws IOException
     */
    private void generateWordDocument(TranslateInput translateInput, TranslateOutput translateOutput,String outputPath) throws IOException {
        if(translateInput==null|| translateOutput == null) {
            return;
        }
        if(StringUtils.isEmpty(translateInput.getText())) {
            return;
        }
        Map<String, String> data = new HashMap<>();
        data.put("input", translateInput.getText());
        data.put("output", translateOutput.getTranslation());

        List<DynamicTable> dynamicTables = new ArrayList<>();
        DynamicTable vocabularyTable = createVocabularyTable(translateOutput);
        dynamicTables.add(vocabularyTable);

        AdvancedWordGenerator.generateWord(
                resourceLoader.getResource("classpath:file/template.docx").getInputStream(),
                outputPath,
                data,
                dynamicTables
        );

    }

    private DynamicTable createVocabularyTable(TranslateOutput translateOutput) {

        List<String> headers = Arrays.asList("单词", "中文", "解释");
        List<List<String>> rows = new ArrayList<>();
        if (translateOutput != null && translateOutput.getVocabulary() != null) {
            for (Vocabulary v : translateOutput.getVocabulary()) {
                rows.add(Arrays.asList(
                        StringUtils.defaultString(v.getEnglish()),
                        StringUtils.defaultString(v.getChinese()),
                        StringUtils.defaultString(v.getExplanation())
                ));
            }
        }
        DynamicTable table = new DynamicTable("vocabulary", headers, rows);

        // 设置表格样式
        TableStyle style = table.getStyle();
        style.setWidth("90%");
        style.setAutoLayout(true);

        table.setStyle(style);

        return table;
    }

    private String getPath() {
        String datePath = DateFormatUtils.format(new Date(), "yyyyMMdd");
        String FileName = "translate_" +  UUID.randomUUID().toString() + ".docx";
        return DOWNLOAD_DIR+"/" + datePath + "/" + FileName;
    }
}
