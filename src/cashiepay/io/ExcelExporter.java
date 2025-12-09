//package cashiepay.io;
//
//import cashiepay.model.PaymentRecord;
//import javafx.collections.ObservableList;
//import javafx.stage.FileChooser;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
//import java.io.FileOutputStream;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import javafx.scene.control.Alert;
//
//public class ExcelExporter {
//
//    public static void exportExcel(ObservableList<PaymentRecord> records) {
//        if (records == null || records.isEmpty()) return;
//        exportToFile(records);
//    }
//
//    public static void exportExcelBySms(Connection conn, String smsFilter) {
//        List<PaymentRecord> records = new ArrayList<>();
//
//        String sql =
//            "SELECT " +
//            "  c.id, " +
//            "  s.student_id, " +
//            "  s.first_name, " +
//            "  s.last_name, " +
//            "  s.middle_name, " +
//            "  s.suffix, " +
//            "  c.or_number, " +
//            "  p.particular_name, " +
//            "  f.fund_name AS mfo_pap_name, " +   // alias kept as mfo_pap_name for existing code
//            "  c.amount, " +
//            "  c.paid_at, " +
//            "  c.sms_status, " +
//            "  c.status " +
//            "FROM collection c " +
//            "JOIN student s    ON c.student_id   = s.id " +
//            "JOIN particular p ON c.particular_id = p.id " +
//            "JOIN fund f       ON c.mfo_pap_id    = f.id ";
//
//        if (smsFilter != null && !smsFilter.equalsIgnoreCase("All")) {
//            sql += "WHERE c.sms_status = ?";
//        }
//
//        sql += " ORDER BY c.id ASC";
//
//        try (PreparedStatement ps = conn.prepareStatement(sql)) {
//            if (smsFilter != null && !smsFilter.equalsIgnoreCase("All")) {
//                ps.setString(1, smsFilter);
//            }
//
//            ResultSet rs = ps.executeQuery();
//            while (rs.next()) {
//                records.add(new PaymentRecord(
//                        rs.getString("id"),
//                        rs.getString("student_id"),
//                        rs.getString("first_name"),
//                        rs.getString("last_name"),
//                        rs.getString("middle_name"),
//                        rs.getString("suffix"),
//                        rs.getString("or_number"),
//                        rs.getString("particular_name"),
//                        rs.getString("mfo_pap_name"),   // actually fund_name
//                        rs.getDouble("amount"),
//                        rs.getString("paid_at"),
//                        rs.getString("sms_status"),
//                        rs.getString("status")
//                ));
//            }
//
//            exportToFile(records);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public static void exportFiltered(Connection conn,
//                                    String smsFilter,
//                                    String statusFilter,
//                                    LocalDate startDate,
//                                    LocalDate endDate) {
//
//      List<PaymentRecord> records = new ArrayList<>();
//
//      StringBuilder sql = new StringBuilder(
//          "SELECT " +
//          "  c.id, " +
//          "  s.student_id, " +
//          "  s.first_name, " +
//          "  s.last_name, " +
//          "  s.middle_name, " +
//          "  s.suffix, " +
//          "  c.or_number, " +
//          "  p.particular_name, " +
//          "  f.fund_name AS mfo_pap_name, " +   // alias for PaymentRecord constructor
//          "  c.amount, " +
//          "  c.paid_at, " +
//          "  c.sms_status, " +
//          "  c.status " +
//          "FROM collection c " +
//          "JOIN student s    ON c.student_id   = s.id " +
//          "JOIN particular p ON c.particular_id = p.id " +
//          "JOIN fund f       ON c.mfo_pap_id    = f.id " +
//          "WHERE 1=1 "
//      );
//
//      // Date filters
//      if (startDate != null && endDate != null) {
//          sql.append(" AND DATE(c.paid_at) BETWEEN '")
//             .append(startDate)
//             .append("' AND '")
//             .append(endDate)
//             .append("' ");
//      } else if (startDate != null) {
//          sql.append(" AND DATE(c.paid_at) >= '").append(startDate).append("' ");
//      } else if (endDate != null) {
//          sql.append(" AND DATE(c.paid_at) <= '").append(endDate).append("' ");
//      }
//
//      // SMS filter
//      if (smsFilter != null && !smsFilter.equalsIgnoreCase("All")) {
//          sql.append(" AND c.sms_status = '").append(smsFilter).append("' ");
//      }
//
//      // Status filter
//      if (statusFilter != null && !statusFilter.equalsIgnoreCase("All")) {
//          sql.append(" AND c.status = '").append(statusFilter).append("' ");
//      }
//
//      sql.append(" ORDER BY c.id ASC");
//
//      try (PreparedStatement ps = conn.prepareStatement(sql.toString());
//            ResultSet rs = ps.executeQuery()) {
//
//           while (rs.next()) {
//               records.add(new PaymentRecord(
//                   rs.getString("id"),
//                   rs.getString("student_id"),
//                   rs.getString("first_name"),
//                   rs.getString("last_name"),
//                   rs.getString("middle_name"),
//                   rs.getString("suffix"),
//                   rs.getString("or_number"),
//                   rs.getString("particular_name"),
//                   rs.getString("mfo_pap_name"),     // actually fund_name
//                   rs.getDouble("amount"),
//                   rs.getString("paid_at"),
//                   rs.getString("sms_status"),
//                   rs.getString("status")
//               ));
//           }
//
//           exportToFile(records);
//
//       } catch (Exception e) {
//           e.printStackTrace();
//       }
//    }
//
//    
//    private static void exportToFile(List<PaymentRecord> records) {
//        try {
//            if (records == null || records.isEmpty()) {
//                Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                alert.setTitle("Export");
//                alert.setHeaderText("No Data");
//                alert.setContentText("There are no records to export.");
//                alert.showAndWait();
//                return;
//            }
//
//            FileChooser fileChooser = new FileChooser();
//            fileChooser.setTitle("Save Excel File");
//            fileChooser.getExtensionFilters()
//                       .add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
//            java.io.File file = fileChooser.showSaveDialog(null);
//
//            if (file == null) {
//                return;
//            }
//
//            try (Workbook workbook = new XSSFWorkbook()) {
//                Sheet sheet = workbook.createSheet("Collection Export");
//
//                String[] headers = {
//                    "Date",
//                    "OR#",
//                    "Name of Payor",
//                    "Particulars",
//                    "MFO/PAP",
//                    "Amount",
//                    "SMS"
//                        
//                };
//
//                Row header = sheet.createRow(0);
//                for (int i = 0; i < headers.length; i++) {
//                    header.createCell(i).setCellValue(headers[i]);
//                }
//
//                int rowIndex = 1;
//                double totalAmount = 0;
//
//                for (PaymentRecord r : records) {
//                    Row row = sheet.createRow(rowIndex++);
//
//                    row.createCell(0).setCellValue(r.getDatePaid());
//                    row.createCell(1).setCellValue(r.getOrNumber());
//                    row.createCell(2).setCellValue(buildPayorName(r));
//                    row.createCell(3).setCellValue(r.getParticular());
//                    row.createCell(4).setCellValue(r.getMfoPap());
//                    row.createCell(5).setCellValue(r.getAmount());
//                    row.createCell(6).setCellValue(r.getSmsStatus());
//
//                    totalAmount += r.getAmount();
//                }
//
//                Row totalRow = sheet.createRow(rowIndex);
//                totalRow.createCell(4).setCellValue("TOTAL");
//                totalRow.createCell(5).setCellValue(totalAmount);
//
//                for (int i = 0; i < headers.length; i++) {
//                    sheet.autoSizeColumn(i);
//                }
//
//                try (FileOutputStream fos = new FileOutputStream(file)) {
//                    workbook.write(fos);
//                }
//
//                System.out.println("EXPORT SUCCESS!");
//
//                Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                alert.setTitle("Export");
//                alert.setHeaderText("Export Complete");
//                alert.setContentText("Excel file has been exported successfully.");
//                alert.showAndWait();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle("Export Failed");
//            alert.setHeaderText("An error occurred during export.");
//            alert.setContentText("Details: " + e.getMessage());
//            alert.showAndWait();
//        }
//    }
//
//    private static String safe(String s) {
//        return s == null ? "" : s.trim();
//    }
//
//    private static String buildPayorName(PaymentRecord r) {
//        String studentId = safe(r.getStudentId());
//        String last      = safe(r.getLastName());
//        String first     = safe(r.getFirstName());
//        String middle    = safe(r.getMiddleName());
//        String suffix    = safe(r.getSuffix());
//
//        StringBuilder middlePart = new StringBuilder();
//        if (!middle.isEmpty()) {
//            middlePart.append(middle);
//        }
//        if (!suffix.isEmpty()) {
//            if (middlePart.length() > 0) middlePart.append(" ");
//            middlePart.append(suffix);
//        }
//
//        StringBuilder result = new StringBuilder();
//        result.append(studentId);
//        result.append(", ");
//        result.append(last);
//        result.append(", ");
//        result.append(first);
//        result.append(", ");
//        result.append(middlePart.toString());
//
//        return result.toString();
//    }
//}


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

public class ExcelExporter {

    // For quick export of what is already in the TableView (if you still use this)
    public static void exportExcel(ObservableList<PaymentRecord> records) {
        if (records == null || records.isEmpty()) return;
        exportToFile(new ArrayList<>(records), "SUMMARY OF COLLECTION (TABLE VIEW)");
    }

    // ---- EXPORT BY SMS FILTER (whole DB) ----
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
            "       f.fund_name, " +
            "       c.amount, " +
            "       c.paid_at, " +
            "       c.sms_status " +
            "FROM collection c " +
            "JOIN student    s ON c.student_id  = s.id " +
            "JOIN particular p ON c.particular_id = p.id " +
            "JOIN fund       f ON c.mfo_pap_id  = f.id ";

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

    // ---- EXPORT WITH FILTERS (used by your CollectionController) ----
    public static void exportFiltered(Connection conn,
                                      String smsFilter,
                                      String statusFilter,
                                      LocalDate startDate,
                                      LocalDate endDate,
                                      String summaryTitle) {

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
            "       f.fund_name, " +
            "       c.amount, " +
            "       c.paid_at, " +
            "       c.sms_status, " +
            "       c.status " +
            "FROM collection c " +
            "JOIN student    s ON c.student_id  = s.id " +
            "JOIN particular p ON c.particular_id = p.id " +
            "JOIN fund       f ON c.mfo_pap_id  = f.id " +
            "WHERE 1=1 "
        );

        // Date range
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
                    rs.getDouble("amount"),
                    rs.getString("paid_at"),
                    rs.getString("sms_status"),
                    rs.getString("status")
                ));
            }

            exportToFile(records, summaryTitle);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------------------
    //  DETAIL SHEET + SUMMARY BY PARTICULAR (like your sample image)
    // ----------------------------------------------------------------
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

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters()
                   .add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        java.io.File file = fileChooser.showSaveDialog(null);

        if (file == null) {
            return;
        }

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Collection Export");

            String[] headers = {
                "Date",
                "OR#",
                "Name of Payor",
                "Particulars",
                "Fund",
                "Amount",
                "SMS"
            };

            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            int rowIndex = 1;
            double totalAmount = 0;

            // ===== accumulate totals per particular (case-insensitive) =====
            // key = uppercased particular, value[0] = display name, value[1] = sum
            java.util.LinkedHashMap<String, Object[]> perParticular = new java.util.LinkedHashMap<>();

            for (PaymentRecord r : records) {
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(r.getDatePaid());
                row.createCell(1).setCellValue(r.getOrNumber());
                row.createCell(2).setCellValue(buildPayorName(r));
                row.createCell(3).setCellValue(r.getParticular());
                row.createCell(4).setCellValue(r.getMfoPap());
                row.createCell(5).setCellValue(r.getAmount());
                row.createCell(6).setCellValue(r.getSmsStatus());

                totalAmount += r.getAmount();

                String partRaw = safe(r.getParticular());
                String key = partRaw.toUpperCase(); // normalize
                if (!key.isEmpty()) {
                    Object[] arr = perParticular.get(key);
                    if (arr == null) {
                        arr = new Object[] { partRaw, r.getAmount() };
                    } else {
                        arr[1] = ((Double) arr[1]) + r.getAmount();
                    }
                    perParticular.put(key, arr);
                }
            }

            // ----- GRAND TOTAL row under detail -----
            Row totalRow = sheet.createRow(rowIndex++);
            totalRow.createCell(4).setCellValue("TOTAL");
            totalRow.createCell(5).setCellValue(totalAmount);

            // auto-size columns for detail
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // ===== SUMMARY BY PARTICULAR (like your sample image) =====
            rowIndex++; // one blank row

            Row titleRow = sheet.createRow(rowIndex++);
            titleRow.createCell(0).setCellValue(summaryTitle);

            // header of summary table
            Row sHeader = sheet.createRow(rowIndex++);
            sHeader.createCell(0).setCellValue("PARTICULAR");
            sHeader.createCell(1).setCellValue("AMOUNT");

            for (Object[] val : perParticular.values()) {
                String displayName = (String) val[0];
                double sum = (Double) val[1];

                Row r = sheet.createRow(rowIndex++);
                r.createCell(0).setCellValue(displayName);
                r.createCell(1).setCellValue(sum);
            }

            // TOTAL line in summary
            Row sTotal = sheet.createRow(rowIndex);
            sTotal.createCell(0).setCellValue("TOTAL");
            sTotal.createCell(1).setCellValue(totalAmount);

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            System.out.println("EXPORT SUCCESS!");

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
    // ---- helpers ---------------------------------------------------

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
