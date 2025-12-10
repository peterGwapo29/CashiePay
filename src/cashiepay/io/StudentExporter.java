package cashiepay.io;

import cashiepay.model.DBConnection;
import cashiepay.model.Student;
import javafx.collections.ObservableList;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.stage.FileChooser;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StudentExporter {

    public static void exportStudents() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Students");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );

        java.io.File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("STUDENTS");

            // HEADER STYLE
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Create header row
            Row header = sheet.createRow(0);
            String[] columns = { "Student ID", "First Name", "Last Name", "Middle Name", "Suffix" };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fetch from DB
            String sql = "SELECT student_id, first_name, last_name, middle_name, suffix " +
                         "FROM student ORDER BY student_id ASC";

            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            int rowIndex = 1;

            while (rs.next()) {
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(rs.getString("student_id"));
                row.createCell(1).setCellValue(rs.getString("first_name"));
                row.createCell(2).setCellValue(rs.getString("last_name"));
                row.createCell(3).setCellValue(rs.getString("middle_name"));
                row.createCell(4).setCellValue(rs.getString("suffix"));
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Save file
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();

            System.out.println("Student export successful!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
