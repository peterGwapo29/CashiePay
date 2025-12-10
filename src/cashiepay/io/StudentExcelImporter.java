package cashiepay.io;

import cashiepay.model.DBConnection;
import cashiepay.model.Student;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;

public class StudentExcelImporter {

    /**
     * Expected Excel format:
     * Row 0: header (will be skipped)
     * Columns:
     *   0 - Student ID (required, used for duplicate checking)
     *   1 - First Name
     *   2 - Last Name
     *   3 - Middle Name
     *   4 - Suffix (can be empty)
     *
     * Status is always set to 'Active' on insert.
     */
    public static boolean importStudents(Connection conn) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Students");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        java.io.File file = fileChooser.showOpenDialog(null);
        if (file == null) {
            return false;
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            String insertSql = "INSERT INTO student " +
                    "(student_id, first_name, last_name, middle_name, suffix, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            String checkSql = "SELECT COUNT(*) FROM student WHERE student_id = ?";

            PreparedStatement insertPs = conn.prepareStatement(insertSql);
            PreparedStatement checkPs  = conn.prepareStatement(checkSql);

            boolean skipHeader = true;
            int importedCount = 0;

            StringBuilder duplicateMessages = new StringBuilder();
            StringBuilder invalidRowMessages = new StringBuilder();

            // to catch duplicates within the same Excel file too
            HashSet<String> seenStudentIdsInFile = new HashSet<>();

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

                // Trim all
                if (studentId != null) studentId = studentId.trim();
                if (firstName != null) firstName = firstName.trim();
                if (lastName != null) lastName = lastName.trim();
                if (middleName != null) middleName = middleName.trim();
                if (suffix != null) suffix = suffix.trim();

                // skip completely empty rows
                if ((studentId == null || studentId.isEmpty()) &&
                    (firstName == null || firstName.isEmpty()) &&
                    (lastName == null || lastName.isEmpty()) &&
                    (middleName == null || middleName.isEmpty()) &&
                    (suffix == null || suffix.isEmpty())) {
                    continue;
                }

                // Student ID is required
                if (studentId == null || studentId.isEmpty()) {
                    invalidRowMessages.append("Row ")
                            .append(row.getRowNum() + 1)
                            .append(" → Missing Student ID. Row skipped.\n");
                    continue;
                }

                // Check duplicates within the same Excel file
                if (!seenStudentIdsInFile.add(studentId)) {
                    duplicateMessages.append("Row ")
                            .append(row.getRowNum() + 1)
                            .append(" → Student ID \"")
                            .append(studentId)
                            .append("\" is duplicated in this Excel file. Skipped.\n");
                    continue;
                }

                // Check duplicates in DB
                checkPs.setString(1, studentId);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        duplicateMessages.append("Row ")
                                .append(row.getRowNum() + 1)
                                .append(" → Student ID \"")
                                .append(studentId)
                                .append("\" already exists in database. Skipped.\n");
                        continue;
                    }
                }

                // If first/last name are missing, you can either skip or allow.
                // Here we allow but you could also enforce them.
                if (firstName == null) firstName = "";
                if (lastName == null) lastName = "";
                if (middleName == null) middleName = "";
                if (suffix == null) suffix = "";

                insertPs.setString(1, studentId);
                insertPs.setString(2, firstName);
                insertPs.setString(3, lastName);
                insertPs.setString(4, middleName);
                insertPs.setString(5, suffix);
                insertPs.setString(6, "Active");

                insertPs.addBatch();
                importedCount++;
            }

            if (importedCount > 0) {
                insertPs.executeBatch();
            }

            // show duplicate warnings
            if (duplicateMessages.length() > 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Duplicate Students Detected");
                alert.setHeaderText("Some students were not imported because their Student ID already exists or is duplicated in the file.");
                alert.setContentText(duplicateMessages.toString());
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(600, 400);
                alert.showAndWait();
            }

            // show invalid row warnings
            if (invalidRowMessages.length() > 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Rows Skipped");
                alert.setHeaderText("Some rows were skipped due to missing required data.");
                alert.setContentText(invalidRowMessages.toString());
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(600, 400);
                alert.showAndWait();
            }

            return importedCount > 0;

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Import Error");
            alert.setHeaderText("Failed to import students.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return false;
        }
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
}
