package cashiepay.controller;

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
import javafx.scene.control.TableView;

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
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnAddNew.setOnAction(e -> openStudentPaymentModal());
        conn = DBConnection.getConnection();
        btnAddNew.setOnAction(e -> openStudentPaymentModal());
        setupTableColumns();
        loadPayments();
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

    private void loadPayments() {
        ObservableList<PaymentRecord> payments = FXCollections.observableArrayList();
        String sql = "SELECT c.id, c.student_id, c.first_name, c.last_name, c.middle_name, c.suffix, " +
                     "c.or_number, p.particular_name, m.mfo_pap_name, c.amount, c.paid_at, c.sms_status " +
                     "FROM collection c " +
                     "JOIN particular p ON c.particular = p.id " +
                     "JOIN mfo_pap m ON c.mfo_pap = m.id " +
                     "ORDER BY c.id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
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
            }
            tableView.setItems(payments);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openStudentPaymentModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/StudentPayment.fxml"));
            Parent root = loader.load();

            Stage modal = new Stage();
            modal.setTitle("Student Payment Transaction");
            modal.setScene(new Scene(root));
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setResizable(false);
            modal.showAndWait();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
