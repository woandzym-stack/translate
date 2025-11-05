package com.example.translate.util;

import com.example.translate.model.word.DynamicTable;
import com.example.translate.model.word.TableStyle;

import java.util.*;

public class WordTest {
    public static void main(String[] args) throws Exception {
        // 1. 准备文本替换数据
        Map<String, String> textData = new HashMap<>();
        textData.put("input", "This is a input text");
        textData.put("output","这是一个测试");

        // 2. 准备动态表格数据
        List<DynamicTable> dynamicTables = new ArrayList<>();
        DynamicTable vocabularyTable = createVocabularyTable();
        dynamicTables.add(vocabularyTable);

        AdvancedWordGenerator.generateWord(
                "D:\\zym\\工作\\translate\\translate\\src\\main\\resources\\file\\template.docx",
                "translate.docx",
                textData,
                dynamicTables
        );
    }

    private static DynamicTable createVocabularyTable() {
        List<String> headers = Arrays.asList("单词", "中文", "解释");

        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("test", "测试", "测试解释"));

        DynamicTable table = new DynamicTable("vocabulary", headers, rows);

        // 设置表格样式
        TableStyle style = table.getStyle();
        style.setWidth("90%");
        style.setAutoLayout(true);

        return table;
    }

}
