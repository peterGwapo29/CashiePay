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
import java.util.List;
import javafx.scene.control.Alert;

public class ExcelExporter {

    public static void exportExcel(ObservableList<PaymentRecord> records) {
        if (records == null || records.isEmpty()) return;
        exportToFile(records);
    }

    public static void exportExcelBySms(Connection conn, String smsFilter) {
        List<PaymentRecord> records = new ArrayList<>();
        String sql = "SELECT c.id, c.student_id, c.first_name, c.last_name, c.middle_name, c.suffix, " +
                     "c.or_number, p.particular_name, m.mfo_pap_name, c.amount, c.paid_at, c.sms_status " +
                     "FROM collection c " +
                     "JOIN particular p ON c.particular = p.id " +
                     "JOIN mfo_pap m ON c.mfo_pap = m.id ";

        if (smsFilter != null && !smsFilter.equalsIgnoreCase("All")) {
            sql += "WHERE c.sms_status = ?";
        }

        sql += " ORDER BY c.id ASC";

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
                        rs.getString("mfo_pap_name"),
                        rs.getDouble("amount"),
                        rs.getString("paid_at"),
                        rs.getString("sms_status"),
                        ""
                ));
            }

            exportToFile(records);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportFiltered(Connection conn, String smsFilter,
                                    LocalDate startDate, LocalDate endDate) {

      List<PaymentRecord> records = new ArrayList<>();

      StringBuilder sql = new StringBuilder(
          "SELECT c.id, c.student_id, c.first_name, c.last_name, c.middle_name, c.suffix, " +
          "c.or_number, p.particular_name, m.mfo_pap_name, c.amount, c.paid_at, c.sms_status " +
          "FROM collection c " +
          "JOIN particular p ON c.particular = p.id " +
          "JOIN mfo_pap m ON c.mfo_pap = m.id WHERE 1=1 "
      );

      if (startDate != null && endDate != null) {
          sql.append(" AND DATE(c.paid_at) BETWEEN '")
             .append(startDate)
             .append("' AND '")
             .append(endDate)
             .append("' ");
      } else if (startDate != null) {
          sql.append(" AND DATE(c.paid_at) >= '").append(startDate).append("' ");
      } else if (endDate != null) {
          sql.append(" AND DATE(c.paid_at) <= '").append(endDate).append("' ");
      }

      if (smsFilter != null && !smsFilter.equalsIgnoreCase("All")) {
          sql.append(" AND c.sms_status = '").append(smsFilter).append("' ");
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
                  rs.getString("mfo_pap_name"),
                  rs.getDouble("amount"),
                  rs.getString("paid_at"),
                  rs.getString("sms_status"),
                  ""
              ));
          }

          exportToFile(records);

      } catch (Exception e) {
          e.printStackTrace();
      }
  }
    
    private static void exportToFile(List<PaymentRecord> records) {
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
                    "MFO/PAP",
                    "Amount",
                    "SMS"
                        
                };

                Row header = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    header.createCell(i).setCellValue(headers[i]);
                }

                int rowIndex = 1;
                double totalAmount = 0;

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
                }

                Row totalRow = sheet.createRow(rowIndex);
                totalRow.createCell(4).setCellValue("TOTAL");
                totalRow.createCell(5).setCellValue(totalAmount);

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

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
