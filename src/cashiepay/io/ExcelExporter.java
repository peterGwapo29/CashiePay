package cashiepay.io;

import cashiepay.model.PaymentRecord;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.control.Alert;
import org.apache.poi.ss.util.CellRangeAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class ExcelExporter {

    public static void exportExcel(ObservableList<PaymentRecord> records) {
        if (records == null || records.isEmpty()) return;
        exportToFile(new ArrayList<>(records), "SUMMARY OF COLLECTION (TABLE VIEW)");
    }

    public static void exportExcelBySms(Connection conn, String smsFilter) {
        List<PaymentRecord> records = new ArrayList<>();

        String sql =
            "SELECT c.id, " +
            "       s.student_id AS student_id, " +
            "       s.first_name, " +
            "       s.last_name, " +
            "       s.middle_name, " +
            "       s.suffix, " +
            "       c.or_number, " +
            "       p.particular_name, " +
//            "       f.fund_name, " +
            "COALESCE(f.fund_name, 'N/A') AS fund_name, " +
            "COALESCE(a.account_name, 'N/A') AS account_name, " + 
            "       c.amount, " +
            "       c.paid_at, " +
            "       c.sms_status " +
            "FROM collection c " +
            "JOIN student    s ON c.student_id  = s.id " +
            "JOIN particular p ON c.particular_id = p.id " +
            "LEFT JOIN fund       f ON c.mfo_pap_id  = f.id " +
            "LEFT JOIN account a ON c.account_id = a.id ";

        if (smsFilter != null && !smsFilter.equalsIgnoreCase("All")) {
            sql += "WHERE c.sms_status = ? ";
        }

        sql += "ORDER BY c.id ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (smsFilter != null && !smsFilter.equalsIgnoreCase("All")) {
                ps.setString(1, smsFilter);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                records.add(new PaymentRecord(
                        rs.getString("id"),
                        rs.getString("student_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("middle_name"),
                        rs.getString("suffix"),
                        rs.getString("or_number"),
                        rs.getString("particular_name"),
                        rs.getString("fund_name"),
                        rs.getString("account_name"),
                        rs.getDouble("amount"),
                        rs.getString("paid_at"),
                        rs.getString("sms_status"),
                        ""
                ));
            }
            
            String title = "SUMMARY OF COLLECTION BY SMS STATUS: " +
                    ((smsFilter == null || smsFilter.equalsIgnoreCase("All")) ? "ALL" : smsFilter);
            exportToFile(records, title);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportFiltered(Connection conn,
                                  String smsFilter,
                                  String statusFilter,
                                  LocalDate startDate,
                                  LocalDate endDate,
                                  String summaryTitleIgnored) {   // name changed just to show it's ignored

    List<PaymentRecord> records = new ArrayList<>();

    StringBuilder sql = new StringBuilder(
        "SELECT c.id, " +
        "       s.student_id AS student_id, " +
        "       s.first_name, " +
        "       s.last_name, " +
        "       s.middle_name, " +
        "       s.suffix, " +
        "       c.or_number, " +
        "       p.particular_name, " +
        "COALESCE(f.fund_name, 'N/A') AS fund_name, " +
        "COALESCE(a.account_name, 'N/A') AS account_name, " + 
        "       c.amount, " +
        "       c.paid_at, " +
        "       c.sms_status, " +
        "       c.status " +
        "FROM collection c " +
        "JOIN student    s ON c.student_id  = s.id " +
        "JOIN particular p ON c.particular_id = p.id " +
        "LEFT JOIN fund       f ON c.mfo_pap_id  = f.id " +
        "LEFT JOIN account a ON c.account_id = a.id " +
        "WHERE 1=1 "
    );

    // Date range (same as before)
    if (startDate != null && endDate != null) {
        sql.append(" AND DATE(c.paid_at) BETWEEN '")
           .append(startDate).append("' AND '").append(endDate).append("' ");
    } else if (startDate != null) {
        sql.append(" AND DATE(c.paid_at) >= '").append(startDate).append("' ");
    } else if (endDate != null) {
        sql.append(" AND DATE(c.paid_at) <= '").append(endDate).append("' ");
    }

    // SMS filter
    if (smsFilter != null && !smsFilter.equalsIgnoreCase("All")) {
        sql.append(" AND c.sms_status = '").append(smsFilter).append("' ");
    }

    // Status filter
    if (statusFilter != null && !statusFilter.equalsIgnoreCase("All")) {
        sql.append(" AND c.status = '").append(statusFilter).append("' ");
    }

    sql.append(" ORDER BY c.id ASC");

    try (PreparedStatement ps = conn.prepareStatement(sql.toString());
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            records.add(new PaymentRecord(
                rs.getString("id"),
                rs.getString("student_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("middle_name"),
                rs.getString("suffix"),
                rs.getString("or_number"),
                rs.getString("particular_name"),
                rs.getString("fund_name"),
                rs.getString("account_name"),
                rs.getDouble("amount"),
                rs.getString("paid_at"),
                rs.getString("sms_status"),
                rs.getString("status")
            ));
        }

        // 🔥 Build nice title like:
        // SUMMARY OF COLLECTION AS OF NOVEMBER 1-28, 2025 WITH OR # 2306397-2306476 (eSMS Account)
        String computedTitle = buildSummaryTitle(startDate, endDate, smsFilter, records);

        exportToFile(records, computedTitle);

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
    private static String buildSummaryTitle(LocalDate startDate,
                                        LocalDate endDate,
                                        String smsFilter,
                                        List<PaymentRecord> records) {

    // ----- DATE PART -----
    String datePart;
    if (startDate != null && endDate != null) {
        if (startDate.getYear() == endDate.getYear()
                && startDate.getMonth() == endDate.getMonth()) {
            // e.g. NOVEMBER 1-28, 2025
            String month = startDate.getMonth().toString(); // already UPPERCASE
            datePart = month + " " + startDate.getDayOfMonth() +
                       "-" + endDate.getDayOfMonth() +
                       ", " + startDate.getYear();
        } else {
            // e.g. NOVEMBER 28, 2025 - DECEMBER 5, 2025
            datePart = formatFullDate(startDate) + " - " + formatFullDate(endDate);
        }
    } else if (startDate != null) {
        datePart = formatFullDate(startDate) + " - PRESENT";
    } else if (endDate != null) {
        datePart = "UNTIL " + formatFullDate(endDate);
    } else {
        datePart = "ALL DATES";
    }

    // ----- OR RANGE PART -----
    int minOr = Integer.MAX_VALUE;
    int maxOr = Integer.MIN_VALUE;

    for (PaymentRecord r : records) {
        String orStr = safe(r.getOrNumber());
        if (orStr.isEmpty()) continue;
        try {
            int n = Integer.parseInt(orStr);
            if (n < minOr) minOr = n;
            if (n > maxOr) maxOr = n;
        } catch (NumberFormatException ignored) {
            // non-numeric OR#, just skip for range
        }
    }

    String orPart = "";
    if (minOr != Integer.MAX_VALUE && maxOr != Integer.MIN_VALUE) {
        orPart = " WITH OR # " + minOr + "-" + maxOr;
    }

    // ----- SMS PART -----
    String smsPart = "";
    if (smsFilter != null && !smsFilter.equalsIgnoreCase("All")) {
        smsPart = " (" + smsFilter + " Account)";
    }

    return "SUMMARY OF COLLECTION AS OF " + datePart + orPart + smsPart;
}

// helper for a single date (e.g. NOVEMBER 28, 2025)
private static String formatFullDate(LocalDate d) {
    if (d == null) return "";
    String month = d.getMonth().toString(); // UPPERCASE
    return month + " " + d.getDayOfMonth() + ", " + d.getYear();
}



    private static void exportToFile(List<PaymentRecord> records, String summaryTitle) {
        try {
            if (records == null || records.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export");
                alert.setHeaderText("No Data");
                alert.setContentText("There are no records to export.");
                alert.showAndWait();
                return;
            }

             records.sort((a, b) -> {
                try {
                    int n1 = Integer.parseInt(a.getOrNumber());
                    int n2 = Integer.parseInt(b.getOrNumber());
                    return Integer.compare(n1, n2);
                } catch (Exception e) {
                    return a.getOrNumber().compareTo(b.getOrNumber());
                }
            });

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Excel File");
            fileChooser.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
            java.io.File file = fileChooser.showSaveDialog(null);

            if (file == null) {
                return;
            }

            try (Workbook workbook = new XSSFWorkbook()) {

                CreationHelper createHelper = workbook.getCreationHelper();

                Font titleFont = workbook.createFont();
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 14);

                    CellStyle titleStyle = workbook.createCellStyle();
                    titleStyle.setFont(titleFont);
                    titleStyle.setAlignment(HorizontalAlignment.CENTER);
                    titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    titleStyle.setWrapText(true);


                Font headerFont = workbook.createFont();
                headerFont.setBold(true);

                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);
                headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                headerStyle.setBorderTop(BorderStyle.THIN);
                headerStyle.setBorderBottom(BorderStyle.THIN);
                headerStyle.setBorderLeft(BorderStyle.THIN);
                headerStyle.setBorderRight(BorderStyle.THIN);

                CellStyle amountStyle = workbook.createCellStyle();
                amountStyle.setDataFormat(
                        createHelper.createDataFormat().getFormat("#,##0.00"));
                amountStyle.setAlignment(HorizontalAlignment.RIGHT);
                amountStyle.setBorderTop(BorderStyle.THIN);
                amountStyle.setBorderBottom(BorderStyle.THIN);
                amountStyle.setBorderLeft(BorderStyle.THIN);
                amountStyle.setBorderRight(BorderStyle.THIN);

                CellStyle dataStyle = workbook.createCellStyle();
                dataStyle.setBorderTop(BorderStyle.THIN);
                dataStyle.setBorderBottom(BorderStyle.THIN);
                dataStyle.setBorderLeft(BorderStyle.THIN);
                dataStyle.setBorderRight(BorderStyle.THIN);

                Font boldFont = workbook.createFont();
                boldFont.setBold(true);
                CellStyle boldAmountStyle = workbook.createCellStyle();
                boldAmountStyle.cloneStyleFrom(amountStyle);
                boldAmountStyle.setFont(boldFont);

                CellStyle boldTextStyle = workbook.createCellStyle();
                boldTextStyle.cloneStyleFrom(dataStyle);
                boldTextStyle.setFont(boldFont);
                
                CellStyle boldAmountPesoStyle = workbook.createCellStyle();
                    boldAmountPesoStyle.cloneStyleFrom(boldAmountStyle);
                    boldAmountPesoStyle.setDataFormat(
                    createHelper.createDataFormat().getFormat("\"₱\"#,##0.00")
                );

                Sheet sheet = workbook.createSheet("Collection Export");

                String[] headers = {
                        "Date",
                        "OR#",
                        "Name of Payor",
                        "Particulars",
                        "MFO/PAP",
                        "Amount"
                };

                Row header = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                sheet.createFreezePane(0, 1);

                int rowIndex = 1;
                double totalAmount = 0;

                java.util.LinkedHashMap<String, Object[]> perParticular =
                        new java.util.LinkedHashMap<>();

                DateTimeFormatter inFmt  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                DateTimeFormatter outFmt = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

                for (PaymentRecord r : records) {
                    Row row = sheet.createRow(rowIndex++);

                    Cell c0 = row.createCell(0);
                    String rawDate = safe(r.getDatePaid());
                    String displayDate = rawDate;
                    if (!rawDate.isEmpty()) {
                        try {
                            LocalDateTime dt = LocalDateTime.parse(rawDate, inFmt);
                            displayDate = dt.format(outFmt);
                        } catch (Exception ex) {
                        }
                    }
                    c0.setCellValue(displayDate);
                    c0.setCellStyle(dataStyle);

                    Cell c1 = row.createCell(1);
                    c1.setCellValue(r.getOrNumber());
                    c1.setCellStyle(dataStyle);

                    Cell c2 = row.createCell(2);
                    c2.setCellValue(buildPayorName(r));
                    c2.setCellStyle(dataStyle);

                    Cell c3 = row.createCell(3);
                    c3.setCellValue(r.getParticular());
                    c3.setCellStyle(dataStyle);

                    Cell c4 = row.createCell(4);
                    c4.setCellValue(r.getMfoPap());
                    c4.setCellStyle(dataStyle);

                    Cell c5 = row.createCell(5);
                    c5.setCellValue(r.getAmount());
                    c5.setCellStyle(amountStyle);

                    totalAmount += r.getAmount();

                    String partRaw = safe(r.getParticular());
                    String key = partRaw.toUpperCase();
                    if (!key.isEmpty()) {
                        Object[] arr = perParticular.get(key);
                        if (arr == null) {
                            arr = new Object[]{partRaw, r.getAmount()};
                        } else {
                            arr[1] = ((Double) arr[1]) + r.getAmount();
                        }
                        perParticular.put(key, arr);
                    }
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                rowIndex++;

                Row titleRow = sheet.createRow(rowIndex++);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue(summaryTitle.toUpperCase());
                titleCell.setCellStyle(titleStyle);
                
                sheet.addMergedRegion(new CellRangeAddress(
                titleRow.getRowNum(), titleRow.getRowNum(), 0, 1));
                titleRow.setHeightInPoints(64);

                Row sHeader = sheet.createRow(rowIndex++);
                Cell sh0 = sHeader.createCell(0);
                sh0.setCellValue("PARTICULAR");
                sh0.setCellStyle(headerStyle);

                Cell sh1 = sHeader.createCell(1);
                sh1.setCellValue("AMOUNT");
                sh1.setCellStyle(headerStyle);

                for (Object[] val : perParticular.values()) {
                    String displayName = (String) val[0];
                    double sum = (Double) val[1];

                    Row r = sheet.createRow(rowIndex++);
                    Cell cPart = r.createCell(0);
                    cPart.setCellValue(displayName);
                    cPart.setCellStyle(dataStyle);

                    Cell cAmt = r.createCell(1);
                    cAmt.setCellValue(sum);
                    cAmt.setCellStyle(amountStyle);
                }

                Row sTotal = sheet.createRow(rowIndex);
                Cell tLabel = sTotal.createCell(0);
                tLabel.setCellValue("TOTAL");
                tLabel.setCellStyle(boldTextStyle);

                Cell tAmt = sTotal.createCell(1);
                tAmt.setCellValue(totalAmount);
                tAmt.setCellStyle(boldAmountPesoStyle);

                sheet.autoSizeColumn(0);
                sheet.autoSizeColumn(1);

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export");
                alert.setHeaderText("Export Complete");
                alert.setContentText("Excel file has been exported successfully.");
                alert.showAndWait();
            }

        } catch (Exception e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText("An error occurred during export.");
            alert.setContentText("Details: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String buildPayorName(PaymentRecord r) {
        String studentId = safe(r.getStudentId());
        String last      = safe(r.getLastName());
        String first     = safe(r.getFirstName());
        String middle    = safe(r.getMiddleName());
        String suffix    = safe(r.getSuffix());

        StringBuilder middlePart = new StringBuilder();
        if (!middle.isEmpty()) {
            middlePart.append(middle);
        }
        if (!suffix.isEmpty()) {
            if (middlePart.length() > 0) middlePart.append(" ");
            middlePart.append(suffix);
        }

        StringBuilder result = new StringBuilder();
        result.append(studentId);
        result.append(", ");
        result.append(last);
        result.append(", ");
        result.append(first);
        result.append(", ");
        result.append(middlePart.toString());

        return result.toString();
    }
}
