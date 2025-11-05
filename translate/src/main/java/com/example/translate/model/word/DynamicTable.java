package com.example.translate.model.word;

import lombok.Data;

import java.util.List;

/**
 * 动态表格数据模型
 */
@Data
public class DynamicTable {
    private String placeholder;  // 占位符名称，如：${userTable}
    private List<String> headers; // 表头
    private List<List<String>> rows; // 数据行
    private TableStyle style;    // 表格样式

    // 构造方法
    public DynamicTable(String placeholder, List<String> headers, List<List<String>> rows) {
        this.placeholder = placeholder;
        this.headers = headers;
        this.rows = rows;
        this.style = new TableStyle();
    }
}
