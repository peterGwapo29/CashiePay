package cashiepay.controller;

import cashiepay.io.ExcelExporter;
import cashiepay.io.ExcelImporter;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import cashiepay.model.DBConnection;
import cashiepay.model.PaymentRecord;

public class CollectionController implements Initializable {

    @FXML private Button btnAddNew, btnImport, btnExport;
    @FXML private TableView<PaymentRecord> tableView;
    @FXML private TableColumn<PaymentRecord, String> id, colStudentId, colFirstName, colLastName, colMiddleName, colSuffix, colOrNumber, colParticular, colMfoPap, colDatePaid, colSms;
    @FXML private TableColumn<PaymentRecord, Double> colAmount;
    @FXML private TableColumn<?, ?> action;
    @FXML
    private Label lblTotalTransactions;
    @FXML
    private Label lblTotalCollected;
    @FXML private ComboBox<String> filterShow, filterSMS;
    @FXML private TextField txtSearchStudent;
    @FXML private Pagination pagination;
    private DatePicker filterDate;

    private ObservableList<PaymentRecord> masterList = FXCollections.observableArrayList();
    private Connection conn;
    private int rowsPerPage = 10;
    @FXML
    private DatePicker filterStartDate;
    @FXML
    private DatePicker filterEndDate;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conn = DBConnection.getConnection();

        setupTableColumns();
        setupShowPerPage();
        setupSearchFeature();
        setupFilters();
        loadPayments();

        btnAddNew.setOnAction(e -> openStudentPaymentModal());
        btnImport.setOnAction(e -> ExcelImporter.importExcel(conn, tableView, this));
        btnExport.setOnAction(e -> ExcelExporter.exportFiltered(
                conn,
                filterSMS.getValue(),
                filterStartDate.getValue(),
                filterEndDate.getValue()
        ));

    }

    private void setupFilters() {
        filterSMS.getItems().addAll("All", "iSMS", "eSMS");
        filterSMS.setValue("All");

        filterSMS.setOnAction(e -> applyFilters());
        filterStartDate.setOnAction(e -> applyFilters());
        filterEndDate.setOnAction(e -> applyFilters());
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

    private void setupShowPerPage() {
        filterShow.getItems().addAll("10", "20", "50", "100");
        filterShow.setValue("10");
        filterShow.valueProperty().addListener((obs, oldVal, newVal) -> {
            rowsPerPage = Integer.parseInt(newVal);
            updatePagination();
        });
    }

    private void setupSearchFeature() {
        PauseTransition debounce = new PauseTransition(Duration.seconds(2));
        txtSearchStudent.textProperty().addListener((obs, oldText, newText) -> {
            debounce.stop();
            debounce.setOnFinished(event -> applyFilters());
            debounce.play();
        });
    }

    public void loadPayments() {
        masterList.clear();
        applyFilters();
    }

    private void applyFilters() {
        ObservableList<PaymentRecord> payments = FXCollections.observableArrayList();
        String smsFilter = filterSMS.getValue();
        String searchKeyword = txtSearchStudent.getText() != null ? txtSearchStudent.getText().trim().toLowerCase() : "";

        LocalDate startDate = filterStartDate.getValue();
        LocalDate endDate = filterEndDate.getValue();

        StringBuilder sql = new StringBuilder(
            "SELECT c.id, c.student_id, c.first_name, c.last_name, c.middle_name, c.suffix, " +
            "c.or_number, p.particular_name, m.mfo_pap_name, c.amount, c.paid_at, c.sms_status " +
            "FROM collection c " +
            "JOIN particular p ON c.particular = p.id " +
            "JOIN mfo_pap m ON c.mfo_pap = m.id " +
            "WHERE 1=1 "
        );

        // Apply date range filter
        if (startDate != null && endDate != null) {
            sql.append(" AND DATE(c.paid_at) BETWEEN '").append(startDate).append("' AND '").append(endDate).append("' ");
        } else if (startDate != null) {
            sql.append(" AND DATE(c.paid_at) >= '").append(startDate).append("' ");
        } else if (endDate != null) {
            sql.append(" AND DATE(c.paid_at) <= '").append(endDate).append("' ");
        }

        sql.append(" ORDER BY c.id ASC");

        double totalCollected = 0;
        int totalTransactions = 0;
        int pending = 0;

        try (PreparedStatement ps = conn.prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PaymentRecord pr = new PaymentRecord(
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
                );

                if (!smsFilter.equalsIgnoreCase("All") && !smsFilter.equalsIgnoreCase(pr.getSmsStatus())) {
                    continue;
                }

                if (!searchKeyword.isEmpty() && !pr.getStudentId().toLowerCase().contains(searchKeyword)) {
                    continue;
                }

                payments.add(pr);
                totalTransactions++;
                totalCollected += pr.getAmount();
                if ("Pending".equalsIgnoreCase(pr.getSmsStatus())) pending++;
            }

            masterList.setAll(payments);
            updatePagination();
            showPage(0);

            lblTotalTransactions.setText(String.valueOf(totalTransactions));
            lblTotalCollected.setText("₱" + new DecimalFormat("#,###.00").format(totalCollected));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) masterList.size() / rowsPerPage);
        if (pageCount == 0) pageCount = 1;
        pagination.setPageCount(pageCount);
        pagination.setPageFactory(pageIndex -> {
            showPage(pageIndex);
            return new Label("");
        });
    }

    private void showPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, masterList.size());
        tableView.setItems(FXCollections.observableArrayList(masterList.subList(fromIndex, toIndex)));
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

    @FXML
    private void tableShowAction(ActionEvent event) {
    }
}
