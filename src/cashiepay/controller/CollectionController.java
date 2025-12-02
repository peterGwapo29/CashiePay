package cashiepay.controller;

import cashiepay.io.ExcelExporter;
import cashiepay.io.ExcelImporter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import cashiepay.model.DBConnection;
import cashiepay.model.PaymentRecord;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import org.apache.poi.ss.usermodel.*;


public class CollectionController implements Initializable {

    @FXML
    private Button btnAddNew;

    @FXML
    private TableView<PaymentRecord> tableView;

    @FXML
    private TableColumn<PaymentRecord, String> colStudentId;
    @FXML
    private TableColumn<PaymentRecord, String> colFirstName;
    @FXML
    private TableColumn<PaymentRecord, String> colLastName;
    @FXML
    private TableColumn<PaymentRecord, String> colMiddleName;
    @FXML
    private TableColumn<PaymentRecord, String> colSuffix;
    @FXML
    private TableColumn<PaymentRecord, String> colOrNumber;
    @FXML
    private TableColumn<PaymentRecord, String> colParticular;
    @FXML
    private TableColumn<PaymentRecord, String> colMfoPap;
    @FXML
    private TableColumn<PaymentRecord, Double> colAmount;
    @FXML
    private TableColumn<PaymentRecord, String> colDatePaid;
    @FXML
    private TableColumn<PaymentRecord, String> colSms;
    @FXML
    private TableColumn<PaymentRecord, String> id;
    
    private Connection conn;
    @FXML
    private Label lblTotalTransactions;
    @FXML
    private Label lblPending;
    @FXML
    private ComboBox<String> filterComboBox;
    @FXML
    private Label lblTotalCollected;
    @FXML
    private Button btnImport;
    @FXML
    private Button btnExport;
    @FXML
    private ComboBox<String> filterSMS;
    @FXML
    private TableColumn<?, ?> colSms1;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnAddNew.setOnAction(e -> openStudentPaymentModal());

        conn = DBConnection.getConnection();

        setupTableColumns();
        loadPayments("ALL");

        filterComboBox.getItems().addAll("ALL", "TODAY", "WEEKLY", "MONTHLY");
        filterComboBox.setValue("ALL");

        filterComboBox.setOnAction(e -> applyFilter());
        
        filterSMS.getItems().addAll("All", "iSMS", "eSMS");
        filterSMS.setValue("All");
        filterSMS.setOnAction(e -> applySmsFilter());
        
//      OLDDDD
//        btnImport.setOnAction(e -> importExcel());
//        btnExport.setOnAction(e -> {
//            applySmsFilter();
//            try {
//                exportExcel();
//            } catch (ClassNotFoundException ex) {
//                System.out.println("Error: " + ex);
//            }
//        });
//          NEWWWWW
//            btnImport.setOnAction(e -> ExcelImporter.importExcel(conn, tableView));
            btnImport.setOnAction(e -> ExcelImporter.importExcel(conn, tableView, this));

            btnExport.setOnAction(e -> {
                applySmsFilter();
                ExcelExporter.exportExcel(tableView);
            });
    }

    private void applyFilter() {
        String filter = filterComboBox.getValue();
        loadPayments(filter);
    }
    
    private void applySmsFilter() {
        String smsFilter = filterSMS.getValue();
        String timeFilter = filterComboBox.getValue();
        
        loadPayments(timeFilter);
        if (smsFilter == null || smsFilter.equalsIgnoreCase("All")) {
            return;
        }

        ObservableList<PaymentRecord> filtered = FXCollections.observableArrayList();
        for (PaymentRecord record : tableView.getItems()) {
            String status = record.getSmsStatus();
            if (status != null && status.trim().equalsIgnoreCase(smsFilter)) {
                filtered.add(record);
            }
        }
        tableView.setItems(filtered);
    }

    private void setupTableColumns() {
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colMiddleName.setCellValueFactory(new PropertyValueFactory<>("middleName"));
        colSuffix.setCellValueFactory(new PropertyValueFactory<>("suffix"));
        colOrNumber.setCellValueFactory(new PropertyValueFactory<>("orNumber"));
        colParticular.setCellValueFactory(new PropertyValueFactory<>("particular"));
        colMfoPap.setCellValueFactory(new PropertyValueFactory<>("mfoPap"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDatePaid.setCellValueFactory(new PropertyValueFactory<>("datePaid"));
        colSms.setCellValueFactory(new PropertyValueFactory<>("smsStatus"));
    }

    public void loadPayments(String filter) {
        ObservableList<PaymentRecord> payments = FXCollections.observableArrayList();

        String sql = "SELECT c.id, c.student_id, c.first_name, c.last_name, c.middle_name, c.suffix, " +
                     "c.or_number, p.particular_name, m.mfo_pap_name, c.amount, c.paid_at, c.sms_status " +
                     "FROM collection c " +
                     "JOIN particular p ON c.particular = p.id " +
                     "JOIN mfo_pap m ON c.mfo_pap = m.id ";

        switch (filter) {
            case "TODAY":
                sql += "WHERE DATE(c.paid_at) = CURDATE() ";
                break;
            case "WEEKLY":
                sql += "WHERE YEARWEEK(c.paid_at, 1) = YEARWEEK(CURDATE(), 1) ";
                break;
            case "MONTHLY":
                sql += "WHERE MONTH(c.paid_at) = MONTH(CURDATE()) AND YEAR(c.paid_at) = YEAR(CURDATE()) ";
                break;
            default:
                break;
        }

        sql += "ORDER BY c.id ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            double totalCollected = 0;
            int totalTransactions = 0;
            int pending = 0;

            while (rs.next()) {
                payments.add(new PaymentRecord(
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
                    rs.getString("sms_status")
                ));

                totalTransactions++;
                totalCollected += rs.getDouble("amount");
                if (rs.getString("sms_status").equalsIgnoreCase("Pending")) {
                    pending++;
                }
            }

            tableView.setItems(payments);

            lblTotalTransactions.setText(String.valueOf(totalTransactions));
            lblTotalCollected.setText(String.format("₱%.2f", totalCollected));
            lblPending.setText(String.valueOf(pending));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openStudentPaymentModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/StudentPayment.fxml"));
            Parent root = loader.load();
            
            StudentPaymentController controller = loader.getController();
            controller.setParentController(this);

            Stage modal = new Stage();
            modal.setScene(new Scene(root));
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setResizable(false);
            modal.showAndWait();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void exportExcel() throws ClassNotFoundException {
        ExcelExporter.exportExcel(tableView);
    }
    
    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    private int getIdByName(Connection conn, String table, String nameColumn, String nameValue) throws Exception {
        String sql = "SELECT id FROM " + table + " WHERE " + nameColumn + " = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, nameValue.trim());
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("id");
        } else {
            throw new Exception("No ID found for " + nameValue + " in table " + table);
        }
    }
    
    private int resolveId(Connection conn, String table, String nameColumn, String value) throws Exception {
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

        if (rs.next()) {
            return rs.getInt("id");
        }

        throw new Exception("No ID found for '" + value + "' in table " + table);
    }
}
