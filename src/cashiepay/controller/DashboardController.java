package cashiepay.controller;

import cashiepay.model.DBConnection;
import cashiepay.model.PaymentRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

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
    private Label paidStudentsLabel;
    @FXML
    private Label outstandingStudentsLabel;
    @FXML
    private Label totalStudentsLabel;
    
    private Connection conn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conn = DBConnection.getConnection();
        
        setupTableView();
        loadRecentTransactions();
        loadDashboardCards();

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
        setPaidStudents();
        setOutstandingStudents();
        setTotalStudents();
//        setPendingPayments();
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
                todaysRevenueLabel.setText("₱" + rs.getDouble("revenue"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setPaidStudents() {
        String sql = "SELECT COUNT(DISTINCT student_id) AS paid_students " +
                     "FROM collection " +
                     "WHERE paid_at IS NOT NULL AND paid_at <> '' AND paid_at <> '0000-00-00 00:00:00'";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                paidStudentsLabel.setText(String.valueOf(rs.getInt("paid_students")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setOutstandingStudents() {
        String sql = "SELECT COUNT(DISTINCT student_id) AS outstanding " +
                     "FROM collection WHERE paid_at IS NULL OR paid_at = ''";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                outstandingStudentsLabel.setText(String.valueOf(rs.getInt("outstanding")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTotalStudents() {
        String sql = "SELECT COUNT(*) AS total FROM collection";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                totalStudentsLabel.setText(String.valueOf(rs.getInt("total")));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void loadRecentTransactions() {
        ObservableList<PaymentRecord> list = FXCollections.observableArrayList();

        String sql = "SELECT \n" +
                        "    c.id,\n" +
                        "    c.student_id,\n" +
                        "    c.first_name,\n" +
                        "    c.last_name,\n" +
                        "    c.middle_name,\n" +
                        "    c.suffix,\n" +
                        "    c.or_number,\n" +
                        "    p.particular_name AS particular_name,\n" +
                        "    m.mfo_pap_name AS mfo_pap_name,\n" +
                        "    c.amount,\n" +
                        "    c.paid_at\n" +
                        "FROM collection c\n" +
                        "LEFT JOIN particular p ON c.particular = p.id\n" +
                        "LEFT JOIN mfo_pap m ON c.mfo_pap = m.id\n" +
                        "ORDER BY c.paid_at DESC\n" +
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
                        rs.getString("mfo_pap_name"),
                        rs.getDouble("amount"),
                        rs.getString("paid_at"),
                        "", // smsStatus placeholder
                        ""  // status placeholder
                ));
            }

            recentTransactionsTable.setItems(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
