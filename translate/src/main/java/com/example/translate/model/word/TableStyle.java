package com.example.translate.model.word;

import lombok.Data;

@Data
public class TableStyle {
    private String width = "100%";  // 表格宽度
    private boolean autoLayout = true; // 是否自动布局
    private String headerBgColor;   // 表头背景色
    private int headerFontSize = 12; // 表头字体大小
}
