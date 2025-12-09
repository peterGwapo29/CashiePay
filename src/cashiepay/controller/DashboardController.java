package cashiepay.controller;

import cashiepay.model.DBConnection;
import cashiepay.model.PaymentRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;


public class DashboardController implements Initializable {

    @FXML
    private TableView<PaymentRecord> recentTransactionsTable;

    @FXML
    private TableColumn<PaymentRecord, String> colDate;
    @FXML
    private TableColumn<PaymentRecord, String> colOrNumber;
    @FXML
    private TableColumn<PaymentRecord, String> colPayor;
    @FXML
    private TableColumn<PaymentRecord, String> colParticular;
    @FXML
    private TableColumn<PaymentRecord, String> colMfoPap;
    @FXML
    private TableColumn<PaymentRecord, Double> colAmount;
    @FXML
    private Label totalTransactionsLabel;
    @FXML
    private Label todaysRevenueLabel;
    @FXML
    private Label totalStudentsLabel;
    
    private Connection conn;
    @FXML
    private Label dateToday;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conn = DBConnection.getConnection();
        
        recentTransactionsTable.setColumnResizePolicy(recentTransactionsTable.CONSTRAINED_RESIZE_POLICY);
        setupTableView();
        loadRecentTransactions();
        loadDashboardCards();

        colDate.setCellValueFactory(cell -> cell.getValue().datePaidProperty());
        colOrNumber.setCellValueFactory(cell -> cell.getValue().orNumberProperty());
        colPayor.setCellValueFactory(cell -> 
            new SimpleStringProperty(
                cell.getValue().getStudentId()  + ", " +
                cell.getValue().getLastName()   + ", " +
                cell.getValue().getFirstName()  + ", " +
                cell.getValue().getMiddleName() + ", " +
                cell.getValue().getSuffix()
            )
        );
        colParticular.setCellValueFactory(cell -> cell.getValue().particularProperty());
        colMfoPap.setCellValueFactory(cell -> cell.getValue().mfoPapProperty());
        colAmount.setCellValueFactory(cell -> cell.getValue().amountProperty().asObject());
        
        displayCurrentDate();

    }
    
    private void setupTableView() {
        colDate.setCellValueFactory(cell -> cell.getValue().datePaidProperty());
        colOrNumber.setCellValueFactory(cell -> cell.getValue().orNumberProperty());
        colPayor.setCellValueFactory(cell -> 
            new SimpleStringProperty(
                cell.getValue().getFirstName() + " " + cell.getValue().getLastName()
            )
        );
        colParticular.setCellValueFactory(cell -> cell.getValue().particularProperty());
        colMfoPap.setCellValueFactory(cell -> cell.getValue().mfoPapProperty());
        colAmount.setCellValueFactory(cell -> cell.getValue().amountProperty().asObject());
    }
    
     private void loadDashboardCards() {
        setTotalTransactions();
        setTodaysRevenue();
        setTotalStudents();
    }
     
     private void setTotalTransactions() {
        String sql = "SELECT COUNT(*) AS total FROM collection WHERE DATE(paid_at) = CURDATE()";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                totalTransactionsLabel.setText(String.valueOf(rs.getInt("total")));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
     
     private void setTodaysRevenue() {
        String sql = "SELECT SUM(amount) AS revenue FROM collection WHERE DATE(paid_at) = CURDATE()";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                double revenue = rs.getDouble("revenue");
                java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance();
                String formatted = formatter.format(revenue);

                todaysRevenueLabel.setText("₱" + formatted);

            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setTotalStudents() {
        String sql = "SELECT COUNT(DISTINCT student_id) AS total FROM collection";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                totalStudentsLabel.setText(String.valueOf(rs.getInt("total")));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void loadRecentTransactions() {
        ObservableList<PaymentRecord> list = FXCollections.observableArrayList();

        String sql =
            "SELECT " +
            "    c.id, " +
            "    s.student_id, " +
            "    s.first_name, " +
            "    s.last_name, " +
            "    s.middle_name, " +
            "    s.suffix, " +
            "    c.or_number, " +
            "    p.particular_name AS particular_name, " +
            "    f.fund_name       AS fund_name, " +
            "    c.amount, " +
            "    c.paid_at, " +
            "    c.sms_status, " +
            "    c.status " +
            "FROM collection c " +
            "JOIN student s      ON c.student_id   = s.id " +
            "LEFT JOIN particular p ON c.particular_id = p.id " +
            "LEFT JOIN fund f       ON c.mfo_pap_id    = f.id " +
            "ORDER BY c.paid_at DESC " +
            "LIMIT 10";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new PaymentRecord(
                        rs.getString("id"),
                        rs.getString("student_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("middle_name"),
                        rs.getString("suffix"),
                        rs.getString("or_number"),
                        rs.getString("particular_name"),
                        rs.getString("fund_name"),      // goes into mfoPapProperty()
                        rs.getDouble("amount"),
                        rs.getString("paid_at"),
                        rs.getString("sms_status"),
                        rs.getString("status")
                ));
            }

            recentTransactionsTable.setItems(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    private void displayCurrentDate() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter formatter =
        java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d");

        dateToday.setText(today.format(formatter));
    }
}
