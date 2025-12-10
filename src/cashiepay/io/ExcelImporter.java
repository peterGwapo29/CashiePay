package cashiepay.io;

import cashiepay.controller.CollectionController;
import cashiepay.model.PaymentRecord;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ExcelImporter {

    public static boolean importExcel(Connection conn,
                                      TableView<PaymentRecord> tableView,
                                      CollectionController controller,
                                      String smsOverride) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Excel File");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        java.io.File file = fileChooser.showOpenDialog(null);

        if (file == null) {
            return false;
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // CURRENT INDEX EXPORT FORMAT COLUMNS
            // 0 Date, 1 OR#, 2 Name of Payor (STUDENT_ID, Last, First Middle Suffix),
            // 3 Particulars, 4 MFO/PAP(Fund), 5 Amount, [6 SMS - optional/old]
            String sql = "INSERT INTO collection (" +
                    "student_id, or_number, particular_id, mfo_pap_id, amount, paid_at, sms_status, status" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);

            boolean skipHeader = true;
            StringBuilder duplicateMessages = new StringBuilder();
            StringBuilder missingStudentMessages = new StringBuilder();
            int importedCount = 0;

            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                // read all cells of the row
//                String dateStr       = getCellStringValue(row.getCell(0));
//                String orNumber      = getCellStringValue(row.getCell(1));
                
                String dateStrRaw    = getCellStringValue(row.getCell(0));
                String orNumber      = getCellStringValue(row.getCell(1));
                String payorName     = getCellStringValue(row.getCell(2));
                String particularVal = getCellStringValue(row.getCell(3));
                String mfoPapVal     = getCellStringValue(row.getCell(4));
                String amountStr     = getCellStringValue(row.getCell(5));
                String smsStatus     = getCellStringValue(row.getCell(6));

                if ("TOTAL".equalsIgnoreCase(mfoPapVal)) {
                    continue;
                }

                if (isRowEmpty(orNumber, payorName,
                               particularVal, mfoPapVal, amountStr, smsStatus)) {
                    continue;
                }

                amountStr = amountStr.trim();
                if (amountStr.isEmpty()) {
                    amountStr = "0";
                }
                double amount = Double.parseDouble(amountStr);

                if (smsStatus == null || smsStatus.trim().isEmpty()) {
                    smsStatus = "N/A";
                }

                if (smsOverride != null && !smsOverride.trim().isEmpty()) {
                    smsStatus = smsOverride.trim();
                }

                NameParts np = parseName(payorName);
                String studentNumber = np.studentId;

                    if (studentNumber == null || studentNumber.trim().isEmpty()) {
                        if (payorName == null || payorName.trim().isEmpty()) {
                            continue;
                        }
                        missingStudentMessages.append("Row ")
                                .append(row.getRowNum() + 1)
                                .append(" → Missing Student ID in Name of Payor: \"")
                                .append(payorName)
                                .append("\"\n");
                        continue;
                    }

                Integer studentPk = getStudentPkIfExists(conn, studentNumber);
                if (studentPk == null) {
                    missingStudentMessages.append("Row ")
                            .append(row.getRowNum() + 1)
                            .append(" → Student ID \"")
                            .append(studentNumber)
                            .append("\" not found in student table.\n");
                    continue;
                }

                int particularId = resolveId(conn, "particular", "particular_name", particularVal);

                Integer fundId = resolveNullableId(conn, "fund", "fund_name", mfoPapVal);

                if (isAlreadyPaid(conn, studentPk, particularId)) {
                    duplicateMessages.append("Row ")
                            .append(row.getRowNum() + 1)
                            .append(" → Student ID: ")
                            .append(studentNumber)
                            .append(" has already paid the particular: \"")
                            .append(particularVal)
                            .append("\"\n");
                    continue;
                }

                ps.setInt(1, studentPk);
                ps.setString(2, orNumber);
                ps.setInt(3, particularId);

                if (fundId == null) {
                    ps.setNull(4, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(4, fundId);
                }

                ps.setDouble(5, amount);
                String dbPaidAt = normalizePaidAt(dateStrRaw);
                if (dbPaidAt != null) {
                    ps.setString(6, dbPaidAt);
                } else {
                    ps.setNull(6, java.sql.Types.TIMESTAMP);
                }
                ps.setString(7, smsStatus);
                ps.setString(8, "Active");

                ps.addBatch();
                importedCount++;
            }

            if (importedCount > 0) {
                ps.executeBatch();
            }

            // Show warnings for duplicates
            if (duplicateMessages.length() > 0) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING
                );
                alert.setTitle("Duplicate Payments Detected");
                alert.setHeaderText("Some student + particular combinations already exist:");
                alert.setContentText(duplicateMessages.toString());
                alert.showAndWait();
            }

            // Show warnings for missing students
            if (missingStudentMessages.length() > 0) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR
                );
                alert.setTitle("Missing Students");
                alert.setHeaderText("Some rows were skipped because Student ID was not found.");
                alert.setContentText(missingStudentMessages.toString());
                alert.showAndWait();
            }

            System.out.println("IMPORT SUCCESS WITH DUPLICATE & STUDENT CHECKS!");
            return importedCount > 0;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static boolean isAlreadyPaid(Connection conn,
                                         int studentPk,
                                         int particularId) throws Exception {
        String sql = "SELECT COUNT(*) AS cnt FROM collection " +
                     "WHERE student_id = ? AND particular_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, studentPk);
        ps.setInt(2, particularId);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("cnt") > 0;
        }
        return false;
    }

    private static boolean isRowEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static String getCellStringValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                double d = cell.getNumericCellValue();
                if (d == (long) d) {
                    return String.valueOf((long) d);
                } else {
                    return String.valueOf(d);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    double fd = cell.getNumericCellValue();
                    if (fd == (long) fd) {
                        return String.valueOf((long) fd);
                    } else {
                        return String.valueOf(fd);
                    }
                }
            default:
                return "";
        }
    }

    private static int resolveId(Connection conn,
                                 String table,
                                 String nameColumn,
                                 String value) throws Exception {
        if (value == null || value.trim().isEmpty()) {
            throw new Exception("Empty value for FK field in table " + table);
        }

        value = value.trim();

        String sql = "SELECT id FROM " + table + " WHERE " + nameColumn + " = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, value);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }

        if (value.matches("\\d+")) {
            return Integer.parseInt(value);
        }

        throw new Exception("No ID found for '" + value + "' in " + table);
    }

    private static Integer resolveNullableId(Connection conn,
                                             String table,
                                             String nameColumn,
                                             String value) throws Exception {
        if (value == null) return null;

        value = value.trim();
        if (value.isEmpty()) return null;
        if (value.equalsIgnoreCase("N/A")) return null;

        String sql = "SELECT id FROM " + table + " WHERE " + nameColumn + " = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, value);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }

        // numeric direct id
        if (value.matches("\\d+")) {
            return Integer.parseInt(value);
        }

        throw new Exception("No ID found for '" + value + "' in " + table);
    }

    private static class NameParts {
        String studentId  = "";
        String firstName  = "";
        String middleName = "";
        String lastName   = "";
        String suffix     = "";
    }

    private static NameParts parseName(String fullName) {
        NameParts np = new NameParts();
        if (fullName == null) return np;

        fullName = fullName.trim();
        if (fullName.isEmpty()) return np;

        String[] parts = fullName.split(",");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        if (parts.length >= 3) {
            np.studentId = parts[0];
            np.lastName  = parts[1];
            np.firstName = parts[2];

            if (parts.length >= 4) {
                String midRaw = parts[3];
                if (!midRaw.isEmpty()) {
                    String[] tokens = midRaw.split("\\s+");
                    int len = tokens.length;

                    if (len > 0) {
                        String lastToken = tokens[len - 1];

                        if (isSuffix(lastToken)) {
                            np.suffix = lastToken;
                            len--;
                        }

                        if (len > 0) {
                            StringBuilder mid = new StringBuilder();
                            for (int i = 0; i < len; i++) {
                                if (mid.length() > 0) mid.append(" ");
                                mid.append(tokens[i]);
                            }
                            np.middleName = mid.toString();
                        }
                    }
                }
            }
            return np;
        }

        if (fullName.contains(",")) {
            String[] p = fullName.split(",", 2);
            np.lastName = p[0].trim();
            String rest = p.length > 1 ? p[1].trim() : "";

            if (!rest.isEmpty()) {
                String[] tokens = rest.split("\\s+");
                int len = tokens.length;

                String lastToken = tokens[len - 1];
                if (isSuffix(lastToken)) {
                    np.suffix = lastToken;
                    len--;
                }

                if (len >= 1) {
                    np.firstName = tokens[0];
                }
                if (len >= 2) {
                    StringBuilder mid = new StringBuilder();
                    for (int i = 1; i < len; i++) {
                        if (mid.length() > 0) mid.append(" ");
                        mid.append(tokens[i]);
                    }
                    np.middleName = mid.toString();
                }
            }
            return np;
        }

        String[] tokens = fullName.split("\\s+");
        int len = tokens.length;

        if (len == 1) {
            np.firstName = tokens[0];
            return np;
        }

        String lastToken = tokens[len - 1];
        boolean hasSuffix = isSuffix(lastToken);
        int endIndex = len;
        if (hasSuffix) {
            np.suffix = lastToken;
            endIndex = len - 1;
        }

        if (endIndex == 2) {
            np.firstName = tokens[0];
            np.lastName = tokens[1];
        } else if (endIndex == 3) {
            np.firstName = tokens[0];
            np.middleName = tokens[1];
            np.lastName = tokens[2];
        } else if (endIndex > 3) {
            np.firstName = tokens[0];
            np.lastName = tokens[endIndex - 1];

            StringBuilder mid = new StringBuilder();
            for (int i = 1; i < endIndex - 1; i++) {
                if (mid.length() > 0) mid.append(" ");
                mid.append(tokens[i]);
            }
            np.middleName = mid.toString();
        } else {
            np.firstName = tokens[0];
            np.lastName = tokens[endIndex - 1];
        }

        return np;
    }

    private static boolean isSuffix(String token) {
        if (token == null) return false;
        String t = token.replace(".", "").toUpperCase();
        return t.equals("JR") || t.equals("SR") ||
               t.equals("II") || t.equals("III") ||
               t.equals("IV") || t.equals("V");
    }

    private static Integer getStudentPkIfExists(Connection conn, String studentNumber) throws Exception {
        String sql = "SELECT id FROM student WHERE student_id = ? AND status = 'Active' ";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return null;
    }
    
    private static String normalizePaidAt(String excelDate) {
        if (excelDate == null) return null;
        excelDate = excelDate.trim();
        if (excelDate.isEmpty()) return null;

        // final format to store in DB
        DateTimeFormatter dbFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // New export format: "December 10, 2025"
        try {
            DateTimeFormatter humanFmt = DateTimeFormatter.ofPattern("MMMM d, yyyy");
            LocalDate d = LocalDate.parse(excelDate, humanFmt);
            return d.atStartOfDay().format(dbFmt);   // 2025-12-10 00:00:00
        } catch (Exception ignore) { }

        // Old full datetime: "2025-12-10 00:00:00"
        try {
            LocalDateTime dt = LocalDateTime.parse(excelDate, dbFmt);
            return dt.format(dbFmt);
        } catch (Exception ignore) { }

        // Old date-only: "2025-12-10"
        try {
            DateTimeFormatter dateOnly = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate d = LocalDate.parse(excelDate, dateOnly);
            return d.atStartOfDay().format(dbFmt);
        } catch (Exception ignore) { }

        return null;
    }
}
