package cashiepay.io;

import cashiepay.controller.CollectionController;
import cashiepay.model.PaymentRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ExcelImporter {
    
    public static boolean importExcel(Connection conn,
                                      TableView<PaymentRecord> tableView,
                                      CollectionController controller) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Excel File");
        fileChooser.getExtensionFilters()
                   .add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        java.io.File file = fileChooser.showOpenDialog(null);

        // ⛔ User clicked CANCEL -> no import
        if (file == null) {
            return false;   // ✅ return a boolean, not `return;`
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            String sql = "INSERT INTO collection (student_id, first_name, last_name, middle_name," +
                    "suffix, or_number, particular, mfo_pap, amount, paid_at, sms_status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);

            boolean skipHeader = true;
            StringBuilder duplicateMessages = new StringBuilder();
            int importedCount = 0;   // ✅ count actual inserted rows

            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                String studentId   = getCellStringValue(row.getCell(0));
                String firstName   = getCellStringValue(row.getCell(1));
                String lastName    = getCellStringValue(row.getCell(2));
                String middleName  = getCellStringValue(row.getCell(3));
                String suffix      = getCellStringValue(row.getCell(4));
                String orNumber    = getCellStringValue(row.getCell(5));

                String particularVal = getCellStringValue(row.getCell(6));
                int particularId     = resolveId(conn, "particular", "particular_name", particularVal);

                String mfoPapVal = getCellStringValue(row.getCell(7));
                int mfoPapId     = resolveId(conn, "mfo_pap", "mfo_pap_name", mfoPapVal);

                String amtStr = getCellStringValue(row.getCell(8)).trim();
                if (amtStr.isEmpty()) amtStr = "0";
                double amount = Double.parseDouble(amtStr);

                String paidDate  = getCellStringValue(row.getCell(9));
                String smsStatus = getCellStringValue(row.getCell(10));

                // 🔁 Duplicate check
                if (isAlreadyPaid(conn, studentId, particularId)) {
                    duplicateMessages.append("Row ")
                            .append(row.getRowNum() + 1)
                            .append(" → Student: ")
                            .append(studentId)
                            .append(" has already paid the particular: \"")
                            .append(particularVal)
                            .append("\"\n");
                    continue;
                }

                ps.setString(1, studentId);
                ps.setString(2, firstName);
                ps.setString(3, lastName);
                ps.setString(4, middleName);
                ps.setString(5, suffix);
                ps.setString(6, orNumber);
                ps.setInt(7, particularId);
                ps.setInt(8, mfoPapId);
                ps.setDouble(9, amount);
                ps.setString(10, paidDate);
                ps.setString(11, smsStatus);

                ps.addBatch();
                importedCount++;      // ✅ count this inserted row
            }

            if (importedCount > 0) {
                ps.executeBatch();
            }

            // ⚠️ Show alert if there are duplicates
            if (duplicateMessages.length() > 0) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING
                );
                alert.setTitle("Duplicate Payments Detected");
                alert.setHeaderText("Some students already paid these particulars:");
                alert.setContentText(duplicateMessages.toString());
                alert.showAndWait();
            }

            System.out.println("IMPORT SUCCESS WITH DUPLICATES CHECKED!");

            // ❌ optional: you can remove this and let the controller call loadPayments()
            // controller.loadPayments();

            // ✅ tell caller if anything was actually imported
            return importedCount > 0;

        } catch (Exception e) {
            e.printStackTrace();
            // Let the controller's try/catch show "Import Failed"
            throw new RuntimeException(e);
        }
    }

    
    private static boolean isAlreadyPaid(Connection conn, String studentId, int particularId) throws Exception {
        String sql = "SELECT COUNT(*) AS cnt FROM collection WHERE student_id = ? AND particular = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, studentId);
        ps.setInt(2, particularId);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("cnt") > 0;
        }
        return false;
    }


    private static String getCellStringValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
        }
        return "";
    }

    private static int resolveId(Connection conn, String table, String nameColumn, String value) throws Exception {
        if (value == null || value.trim().isEmpty()) {
            throw new Exception("Empty value for FK field in table " + table);
        }

        value = value.trim();

        if (value.matches("\\d+")) {
            return Integer.parseInt(value);
        }

        String sql = "SELECT id FROM " + table + " WHERE " + nameColumn + " = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, value);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) return rs.getInt("id");

        throw new Exception("No ID found for '" + value + "'");
    }
}
