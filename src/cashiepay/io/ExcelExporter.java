package cashiepay.io;

import cashiepay.model.PaymentRecord;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;

public class ExcelExporter {

    public static void exportExcel(TableView<PaymentRecord> tableView) {
        ObservableList<PaymentRecord> records = tableView.getItems();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        java.io.File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Collection Export");

            String[] headers = {"Student ID", "First Name", "Last Name", "Middle Name",
                                "Suffix", "OR Number", "Particular", "MFO/PAP",
                                "Amount", "Date Paid", "SMS"};

            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            int rowIndex = 1;

            for (PaymentRecord r : records) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(r.getStudentId());
                row.createCell(1).setCellValue(r.getFirstName());
                row.createCell(2).setCellValue(r.getLastName());
                row.createCell(3).setCellValue(r.getMiddleName());
                row.createCell(4).setCellValue(r.getSuffix());
                row.createCell(5).setCellValue(r.getOrNumber());
                row.createCell(6).setCellValue(r.getParticular());
                row.createCell(7).setCellValue(r.getMfoPap());
                row.createCell(8).setCellValue(r.getAmount());
                row.createCell(9).setCellValue(r.getDatePaid());
                row.createCell(10).setCellValue(r.getSmsStatus());
            }

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();

            System.out.println("EXPORT SUCCESS!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
