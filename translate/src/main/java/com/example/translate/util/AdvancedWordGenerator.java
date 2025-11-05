package com.example.translate.util;

import com.example.translate.model.word.DynamicTable;
import com.example.translate.model.word.TableStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class AdvancedWordGenerator {

    /**
     * 生成Word文档（模板替换 + 动态内容）
     */
    public static void generateWord(String templatePath, String outputPath,
                                    Map<String, String> textData,
                                    List<DynamicTable> dynamicTables) throws IOException {
        try (FileInputStream fis = new FileInputStream(templatePath);
             XWPFDocument document = new XWPFDocument(fis);
             FileOutputStream fos = new FileOutputStream(outputPath)) {

            // 1. 模板文本替换
            replaceTemplateText(document, textData);

            // 2. 插入动态表格
            insertDynamicTables(document, dynamicTables);

            document.write(fos);
        }
    }

    public static void generateWord(InputStream templateStream, String outputPath,
                                    Map<String, String> textData, List<DynamicTable> dynamicTables) throws IOException {

        Objects.requireNonNull(templateStream, "templateStream cannot be null");
        Objects.requireNonNull(outputPath, "outputPath cannot be null");

        Map<String, String> safeTextData = textData == null ? Collections.emptyMap() : textData;
        List<DynamicTable> safeDynamicTables = dynamicTables == null ? Collections.emptyList() : dynamicTables;

        Path target = Paths.get(outputPath).toAbsolutePath().normalize();
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        // 使用 try-with-resources 确保 templateStream、document 和输出流都会被关闭
        try (InputStream in = templateStream;
             XWPFDocument document = new XWPFDocument(in);
             OutputStream os = new BufferedOutputStream(Files.newOutputStream(target,
                     StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE))) {

            // 模板文本替换与表格插入（使用已经保护过的集合）
            replaceTemplateText(document, safeTextData);
            insertDynamicTables(document, safeDynamicTables);

            // 写出并 flush
            document.write(os);
            os.flush();
        } catch (IOException | RuntimeException ex) {
            // 写入失败时尝试删除目标文件，避免留下半成品
            try {
                Files.deleteIfExists(target);
            } catch (Exception ignore) {
            }
            throw ex;
        }
    }

    /**
     * 模板文本替换
     */
    private static void replaceTemplateText(XWPFDocument document, Map<String, String> data) {
        // 替换段落文本
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            replaceInParagraphWithFormat(paragraph, data);
        }

        // 替换表格中的文本
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        replaceInParagraphWithFormat(paragraph, data);
                    }
                }
            }
        }

        // 替换页眉页脚
        replaceInHeaderFooter(document, data);
    }


    private static void replaceInParagraphWithFormat(XWPFParagraph paragraph, Map<String, String> data) {
        String paragraphText = paragraph.getText();
        if (paragraphText == null || paragraphText.isEmpty()) {
            return;
        }

        // 检查是否包含占位符
        boolean hasPlaceholder = false;
        for (String key : data.keySet()) {
            if (paragraphText.contains("${" + key + "}")) {
                hasPlaceholder = true;
                break;
            }
        }

        if (!hasPlaceholder) {
            return;
        }

        // 逐个run处理，保持原有格式
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null || runs.isEmpty()) {
            return;
        }

        // 合并所有run的文本
        StringBuilder fullText = new StringBuilder();
        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text != null) {
                fullText.append(text);
            }
        }

        String finalText = fullText.toString();

        // 替换占位符
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String replacement = entry.getValue() != null ? entry.getValue() : "";
            finalText = finalText.replace(placeholder, replacement);
        }

        // 创建新run，继承第一个run的格式
        if (!runs.isEmpty()) {
            XWPFRun firstRun = runs.get(0);
            XWPFRun newRun = paragraph.createRun();
            newRun.setText(finalText);
            // 继承格式
            copyRunStyle(firstRun, newRun);
            //删除第一个run
            paragraph.removeRun(0);
        }
    }

    /**
     * 复制run的样式
     */
    private static void copyRunStyle(XWPFRun source, XWPFRun target) {
        if (source == null || target == null) {
            return;
        }

        // 字体样式
        target.setBold(source.isBold());
        target.setItalic(source.isItalic());
        target.setStrikeThrough(source.isStrikeThrough());
        target.setUnderline(source.getUnderline());

        // 字体设置
        if (source.getFontSize() != -1) {
            target.setFontSize(source.getFontSize());
        }
        if (source.getFontFamily() != null) {
            target.setFontFamily(source.getFontFamily());
        }
        if (source.getColor() != null) {
            target.setColor(source.getColor());
        }

        // 其他格式
        target.setCharacterSpacing(source.getCharacterSpacing());
    }

    /**
     * 替换页眉页脚
     */
    private static void replaceInHeaderFooter(XWPFDocument document, Map<String, String> data) {
        // 页眉
        for (XWPFHeader header : document.getHeaderList()) {
            for (XWPFParagraph paragraph : header.getParagraphs()) {
                replaceInParagraphWithFormat(paragraph, data);
            }
        }

        // 页脚
        for (XWPFFooter footer : document.getFooterList()) {
            for (XWPFParagraph paragraph : footer.getParagraphs()) {
                replaceInParagraphWithFormat(paragraph, data);
            }
        }
    }

    /**
     * 插入动态表格
     */
    private static void insertDynamicTables(XWPFDocument document, List<DynamicTable> dynamicTables) {
        if (dynamicTables == null || dynamicTables.isEmpty()) return;

        for (DynamicTable dynamicTable : dynamicTables) {
            // 在指定占位符位置插入表格
            insertTableAtPlaceholder(document, dynamicTable);
        }
    }

    /**
     * 在占位符位置插入表格
     */
    private static void insertTableAtPlaceholder(XWPFDocument document, DynamicTable dynamicTable) {
        String placeholder = "${" + dynamicTable.getPlaceholder() + "}";

        // 查找占位符所在的段落
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            XWPFParagraph paragraph = document.getParagraphs().get(i);
            if (paragraph.getText().contains(placeholder)) {
                // 移除占位符段落
                document.removeBodyElement(document.getPosOfParagraph(paragraph));

                // 创建表格
                XWPFTable table = document.createTable();

                // 设置表格样式
                applyTableStyle(table, dynamicTable.getStyle());

                // 创建表头
                createTableHeader(table, dynamicTable.getHeaders());

                // 创建表格数据行
                createTableRows(table, dynamicTable.getRows());

                break;
            }
        }
    }

    /**
     * 应用表格样式
     */
    private static void applyTableStyle(XWPFTable table, TableStyle style) {
        if (style == null) return;

        // 设置表格宽度
        table.setWidth(style.getWidth());

    }

    /**
     * 创建表头
     */
    private static void createTableHeader(XWPFTable table, List<String> headers) {
        if (headers == null || headers.isEmpty()) return;

        XWPFTableRow headerRow = table.getRow(0);
        if (headerRow == null) {
            headerRow = table.createRow();
        }

        // 确保有足够的单元格
        while (headerRow.getTableCells().size() < headers.size()) {
            headerRow.createCell();
        }

        // 设置表头内容
        for (int i = 0; i < headers.size(); i++) {
            XWPFTableCell cell = headerRow.getCell(i);
            if (cell != null) {
                cell.setText(headers.get(i));

                // 设置表头样式
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    for (XWPFRun run : paragraph.getRuns()) {
                        run.setBold(true);
                        run.setFontSize(12);
                    }
                }
            }
        }
    }


    /**
     * 创建数据行
     */
    private static void createTableRows(XWPFTable table, List<List<String>> rows) {
        if (rows == null || rows.isEmpty()) return;

        for (List<String> rowData : rows) {
            XWPFTableRow row = table.createRow();

            for (int i = 0; i < rowData.size(); i++) {
                XWPFTableCell cell = row.getCell(i);
                if (cell != null) {
                    cell.setText(rowData.get(i));
                }
            }
        }
    }

    /**
     * 在文档末尾添加表格
     */
    public static void addTableToDocument(XWPFDocument document, DynamicTable dynamicTable) {
        // 创建表格
        XWPFTable table = document.createTable();

        // 设置表格样式
        applyTableStyle(table, dynamicTable.getStyle());

        // 创建表头
        createTableHeader(table, dynamicTable.getHeaders());

        // 创建数据行
        createTableRows(table, dynamicTable.getRows());
    }
}
