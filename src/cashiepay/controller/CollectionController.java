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
import cashiepay.util.LoadingDialog;
import java.time.format.DateTimeFormatter;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.control.TableCell;
import javafx.scene.input.MouseEvent;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class CollectionController implements Initializable {

    @FXML private Button btnAddNew, btnImport, btnExport;
    @FXML private TableView<PaymentRecord> tableView;
    @FXML private TableColumn<PaymentRecord, String> id, colStudentId, colFirstName, colLastName, colMiddleName, colSuffix, colOrNumber, colParticular, colMfoPap, colDatePaid, colSms;
    @FXML private TableColumn<PaymentRecord, Double> colAmount;
    @FXML private TableColumn<PaymentRecord, Void> action;
    @FXML private Label lblTotalTransactions;
    @FXML private Label lblTotalCollected;
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
    @FXML
    private Button btnClearStart;
    @FXML
    private Button btnClearEnd;
    @FXML
    private Label displayDate;
    
    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    @FXML private ComboBox<String> filterStatus;
    @FXML private Button clearBtn;
    @FXML private ComboBox<String> cmbImportSmsType;
    @FXML private TableColumn<PaymentRecord, String> colAccount;
    @FXML private TableView<ParticularSummary> summaryTable;
    @FXML private TableColumn<ParticularSummary, String> colSummaryParticular;
    @FXML private TableColumn<ParticularSummary, Double> colSummaryAmount;
    private ObservableList<ParticularSummary> summaryList = FXCollections.observableArrayList();
    @FXML
    private ComboBox<SemesterFilterItem> filterSemester;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conn = DBConnection.getConnection();
        
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTableColumns();
        setupActionColumn();
        
        setupShowPerPage();
        setupSearchFeature();
        setupFilters();
        loadPayments();
        loadSemesterFilter();

        btnAddNew.setOnAction(e -> openStudentPaymentModal());
        btnImport.setOnAction(e -> importWithLoading());
        btnExport.setOnAction(e -> ExcelExporter.exportFiltered(
                conn,
                filterSMS.getValue(),
                filterStatus.getValue(), 
                filterStartDate.getValue(),
                filterEndDate.getValue(),
                "SUMMARY OF COLLECTION AS OF " + displayDate.getText()
        ));
        
        btnClearStart.setOnAction(e -> {
            filterStartDate.setValue(null);
            applyFilters();
        });

        btnClearEnd.setOnAction(e -> {
            filterEndDate.setValue(null);
            applyFilters();
        });
        
        cmbImportSmsType.getItems().setAll("iSMS", "eSMS", "IGP");
    }

    private void setupFilters() {
        filterSMS.getItems().addAll("All", "iSMS", "eSMS", "IGP");
        filterSMS.setValue("All");
        
        filterStatus.getItems().addAll("All", "Active", "Inactive");
        filterStatus.setValue("Active");

        filterSMS.setOnAction(e -> applyFilters());
        filterStartDate.setOnAction(e -> applyFilters());
        filterEndDate.setOnAction(e -> applyFilters());
        filterStatus.setOnAction(e -> applyFilters()); 
    }
    
    public class ParticularSummary {

        private final StringProperty particular;
        private final DoubleProperty totalAmount;

        public ParticularSummary(String particular, double totalAmount) {
            this.particular = new SimpleStringProperty(particular);
            this.totalAmount = new SimpleDoubleProperty(totalAmount);
        }

        public StringProperty particularProperty() { return particular; }
        public DoubleProperty totalAmountProperty() { return totalAmount; }

        public String getParticular() { return particular.get(); }
        public double getTotalAmount() { return totalAmount.get(); }
    }
    
    public static class SemesterFilterItem {
        private final int id;
        private final String label;

        public SemesterFilterItem(int id, String label) {
            this.id = id;
            this.label = label;
        }

        public int getId() { return id; }
        public String getLabel() { return label; }

        @Override
        public String toString() {
            return label;
        }
    }

    
    private void importWithLoading() {
        String smsType = cmbImportSmsType.getValue();

        if (smsType == null || smsType.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Missing SMS Type");
            alert.setContentText("Please select what to import (iSMS, eSMS, or IGP) before importing.");
            alert.showAndWait();
            return;
        }

        LoadingDialog loading = new LoadingDialog("Importing, please wait...");

        Platform.runLater(loading::show);

        Platform.runLater(() -> {
            try {
                boolean imported = ExcelImporter.importExcel(conn, tableView, CollectionController.this, smsType);

                loading.close();

                if (imported) {
                    loadPayments();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("Import Complete");
                    alert.setContentText("Excel data imported successfully.");
                    alert.showAndWait();
                }

            } catch (Exception ex) {
                loading.close();
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Import Failed");
                alert.setContentText("Error: " + ex.getMessage());
                alert.showAndWait();
            }
        });
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
        colAccount.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDatePaid.setCellValueFactory(new PropertyValueFactory<>("datePaid"));
        colSms.setCellValueFactory(new PropertyValueFactory<>("smsStatus"));
        
        colSummaryParticular.setCellValueFactory(new PropertyValueFactory<>("particular"));
        colSummaryAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
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
    
    private void loadSemesterFilter() {
        ObservableList<SemesterFilterItem> list = FXCollections.observableArrayList();

        list.add(new SemesterFilterItem(0, "All Semesters"));

        String sql = "SELECT semester_id, academic_year, semester " +
                     "FROM semester " +
                     "WHERE status = 'Active' " +
                     "ORDER BY academic_year DESC, semester";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("semester_id");
                String ay = rs.getString("academic_year");
                String sem = rs.getString("semester");
                String label = ay + " - " + sem;

                list.add(new SemesterFilterItem(id, label));
            }

//            filterSemester.setItems(list);
//            filterSemester.getSelectionModel().selectFirst();
//            filterSemester.setVisibleRowCount(3);

            filterSemester.setItems(list);
            SemesterFilterItem current = getCurrentSemesterItem(list);
            if (current != null) {
                filterSemester.getSelectionModel().select(current);
            } else {
                filterSemester.getSelectionModel().selectFirst();
            }
            filterSemester.setVisibleRowCount(3);


        } catch (Exception e) {
            e.printStackTrace();
        }

        filterSemester.setOnAction(e -> applyFilters());
    }


    private void applyFilters() {
        ObservableList<PaymentRecord> payments = FXCollections.observableArrayList();
        String smsFilter = filterSMS.getValue();
        String statusFilter = filterStatus.getValue();
        String searchKeyword = txtSearchStudent.getText() != null ? txtSearchStudent.getText().trim().toLowerCase() : "";

        LocalDate startDate = filterStartDate.getValue();
        LocalDate endDate = filterEndDate.getValue();
        
        SemesterFilterItem semItem = filterSemester != null ? filterSemester.getValue() : null;

        Integer semesterId = (semItem != null && semItem.getId() != 0)
                ? semItem.getId()
                : null;

        StringBuilder sql = new StringBuilder(
            "SELECT " +
                "c.id, " +
                "s.student_id AS student_id, " +
                "s.first_name, " +
                "s.last_name, " +
                "s.middle_name, " +
                "s.suffix, " +
                "c.or_number, " +
                "p.particular_name, " +
//                "f.fund_name, " +
                "COALESCE(f.fund_name, 'N/A') AS fund_name, " +
                "COALESCE(a.account_name, 'N/A') AS account_name, " + 
                "c.amount, " +
                "c.paid_at, " +
                "c.sms_status, " +
                "c.status, " +
                "c.semester_id " +
            "FROM collection c " +
            "JOIN student s ON c.student_id = s.id " +
            "JOIN particular p ON c.particular_id = p.id " +
            "LEFT JOIN fund f ON c.mfo_pap_id = f.id " +
            "LEFT JOIN account a ON c.account_id = a.id " +
            "LEFT JOIN semester sem ON c.semester_id = sem.semester_id " +
            "WHERE 1=1 "
        );

        if (startDate != null && endDate != null) {
            sql.append(" AND DATE(c.paid_at) BETWEEN '").append(startDate).append("' AND '").append(endDate).append("' ");
        } else if (startDate != null) {
            sql.append(" AND DATE(c.paid_at) >= '").append(startDate).append("' ");
        } else if (endDate != null) {
            sql.append(" AND DATE(c.paid_at) <= '").append(endDate).append("' ");
        }
        
        if (semesterId != null) {
            sql.append(" AND c.semester_id = ").append(semesterId).append(" ");
        }
        
        // Update displayDate label
        if (startDate != null && endDate != null) {
            displayDate.setText(startDate.format(displayFormatter) + " - " + endDate.format(displayFormatter));
        } else if (startDate != null) {
            displayDate.setText(startDate.format(displayFormatter) + " - Present");
        } else if (endDate != null) {
            displayDate.setText("Until " + endDate.format(displayFormatter));
        } else {
            displayDate.setText("All Dates");
        }


        sql.append(" ORDER BY c.id ASC");

        double totalCollected = 0;
        int totalTransactions = 0;
        int pending = 0;
        
        java.util.Map<String, Double> perParticular = new java.util.LinkedHashMap<>();

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
                        rs.getString("fund_name"),   
                        rs.getString("account_name"), 
                        rs.getDouble("amount"),
                        rs.getString("paid_at"),
                        rs.getString("sms_status"),
                        rs.getString("status"),
                        rs.getString("semester_id")
                );
                
                if (!"All".equalsIgnoreCase(statusFilter) &&
                    !statusFilter.equalsIgnoreCase(pr.getStatus())) {
                    continue;
                }
                

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
                
                String partName = pr.getParticular();
                perParticular.merge(partName, pr.getAmount(), Double::sum);
            }

            masterList.setAll(payments);
            updatePagination();
            showPage(0);

            lblTotalTransactions.setText(String.valueOf(totalTransactions));
            lblTotalCollected.setText("₱" + new DecimalFormat("#,###.00").format(totalCollected));
            
            summaryList.clear();
            double grandTotal = 0;
            
            for (java.util.Map.Entry<String, Double> e : perParticular.entrySet()) {
                summaryList.add(new ParticularSummary(e.getKey(), e.getValue()));
                grandTotal += e.getValue();
            }
            
            summaryList.add(new ParticularSummary("TOTAL", grandTotal));
            summaryTable.setItems(summaryList);

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
    
    private void setupActionColumn() {
        action.setCellFactory(column -> new TableCell<PaymentRecord, Void>() {

            private final Button editButton = new Button("Edit");
            private final Button toggleButton = new Button();
            private final HBox container = new HBox(10);

            {
                // EDIT BUTTON
                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-pref-width: 44px;");
                editButton.setOnAction(event -> {
                    PaymentRecord data = getTableView().getItems().get(getIndex());
                    openEditPaymentModal(data);
                });

                // DELETE / RESTORE BUTTON
                toggleButton.setOnAction(event -> {
                    PaymentRecord data = getTableView().getItems().get(getIndex());

                    if ("Active".equalsIgnoreCase(data.getStatus())) {
                        if (confirmDelete()) {
                            softDeleteRecord(data);
                            setButtonToRestore(toggleButton);
                        }
                    } else {
                        // Restore
                        if (confirmRestore()) {
                            restoreRecord(data);
                            setButtonToDelete(toggleButton);
                        }
                    }
                });

                container.getChildren().addAll(editButton, toggleButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                PaymentRecord data = getTableView().getItems().get(getIndex());

                if ("Active".equalsIgnoreCase(data.getStatus())) {
                    setButtonToDelete(toggleButton);
                } else {
                    setButtonToRestore(toggleButton);
                }

                setGraphic(container);
            }
        });
    }
    
    private void setButtonToDelete(Button button) {
        button.setText("Delete");
        button.setStyle("-fx-background-color: #FF5252; -fx-text-fill: white; -fx-pref-width: 64px;");
    }

    private void setButtonToRestore(Button button) {
        button.setText("Restore");
        button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-pref-width: 64px;");
    }
    
    private void openEditPaymentModal(PaymentRecord record) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/StudentPayment.fxml"));
            Parent root = loader.load();
            StudentPaymentController controller = loader.getController();
            controller.setParentController(this);
            controller.setPaymentRecord(record);
            Stage modal = new Stage();
            modal.setScene(new Scene(root));
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setResizable(false);
            modal.setTitle("Record Transaction");
            modal.showAndWait();
            
            loadPayments();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void softDeleteRecord(PaymentRecord record) {
        String sql = "UPDATE collection SET status = 'Inactive' WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, record.getId());
            ps.executeUpdate();
            loadPayments();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restoreRecord(PaymentRecord record) {
        String sql = "UPDATE collection SET status = 'Active' WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, record.getId());
            ps.executeUpdate();
            loadPayments();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean confirmDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Are you sure you want to delete this record?");
        alert.setContentText("This action cannot be undone.");

        ButtonType yesButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yesButton, cancelButton);

        return alert.showAndWait().orElse(cancelButton) == yesButton;
    }
    
    private boolean confirmSave() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Save");
        alert.setHeaderText("Save Payment Record");
        alert.setContentText("Do you want to save this record?");

        ButtonType yes = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yes, cancel);

        return alert.showAndWait().orElse(cancel) == yes;
    }

    private boolean confirmRestore() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Restore");
        alert.setHeaderText("Are you sure you want to restore this record?");
        alert.setContentText("The record will be moved back to active payments.");

        ButtonType yesButton = new ButtonType("Restore", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yesButton, cancelButton);

        return alert.showAndWait().orElse(cancelButton) == yesButton;
    }
    
    private void addClearButton(DatePicker datePicker) {
        Button clearBtn = new Button("X");
        clearBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: red;");
        clearBtn.setOnAction(e -> {
            datePicker.setValue(null);
            applyFilters();
        });

        HBox box = new HBox(datePicker, clearBtn);
        box.setSpacing(5);
        datePicker.getParent().getChildrenUnmodifiable().clear();
    }
    @FXML
    private void tableShowAction(ActionEvent event) {
    }

    @FXML
    private void ClearSearchBar(ActionEvent event) {
        if(event.getSource() == clearBtn){
            txtSearchStudent.setText("");
        }
    }
    
    private SemesterFilterItem getCurrentSemesterItem(ObservableList<SemesterFilterItem> items) {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();

        String targetSemKey;
        if (month >= 8 && month <= 12) {
            targetSemKey = "1";
        } else if (month >= 1 && month <= 5) {
            targetSemKey = "2";
        } else {
            targetSemKey = "summer";
        }

        int startYear = (month >= 8) ? now.getYear() : now.getYear() - 1;
        String targetAY = startYear + "-" + (startYear + 1);

        for (SemesterFilterItem item : items) {
            if (item.getId() == 0) continue;

            String label = item.getLabel() != null ? item.getLabel().toLowerCase() : "";
            boolean ayMatch = label.contains(targetAY.toLowerCase());

            boolean semMatch;
            if ("summer".equals(targetSemKey)) {
                semMatch = label.contains("summer") || label.contains("mid") || label.contains("midyear");
            } else if ("1".equals(targetSemKey)) {
                semMatch = label.contains("1st") || label.contains("first") || label.matches(".*\\b1\\b.*");
            } else {
                semMatch = label.contains("2nd") || label.contains("second") || label.matches(".*\\b2\\b.*");
            }

            if (ayMatch && semMatch) {
                return item;
            }
        }

        return null;
    }
   
}