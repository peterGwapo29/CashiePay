package cashiepay.controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import cashiepay.model.DBConnection;
import cashiepay.model.PaymentModel;
import cashiepay.model.PaymentRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;


public class StudentPaymentController implements Initializable {

    @FXML
    private TextField txtOrNumber, txtAmount;

    @FXML
    private ComboBox<ParticularItem> comboParticular;

    @FXML
    private ComboBox<FundItem> comboMfoPap;

    @FXML
    private ComboBox<String> comboSmsStatus;

    @FXML
    private DatePicker datePaidAt;

    @FXML
    private Button btnSave, btnCancel;

    @FXML
    private TextField txtStudentID;

    @FXML
    private TextField txtFirstName;

    @FXML
    private TextField txtLastName;

    @FXML
    private TextField txtMiddleName;

    @FXML
    private ComboBox<String> comboSuffix;

    private PaymentModel model;
    private Connection conn;
    private CollectionController parentController;
    private PaymentRecord currentRecord;
    @FXML
    private ComboBox<AccountItem> comboAccount;
    @FXML
    private Label paideAtLabel;
    @FXML
    private ComboBox<SemesterItem> comboSemester;

    public static class ParticularItem {
        private int id;
        private String name;
        private double amount;

        public ParticularItem(int id, String name, double amount) {
            this.id = id;
            this.name = name;
            this.amount = amount;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public double getAmount() { return amount; }

        @Override
        public String toString() { return name; }
    }

    public static class FundItem {
        private int id;
        private String name;

        public FundItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() { return name; }
    }
    
    public static class AccountItem {
        private int id;
        private String name;

        public AccountItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() {
            return name;
        }
    }
    
    public static class SemesterItem {
        private int id;       
        private String academicYear;
        private String semester;

        public SemesterItem(int id, String academicYear, String semester) {
            this.id = id;
            this.academicYear = academicYear;
            this.semester = semester;
        }

        public int getId() { return id; }
        public String getAcademicYear() { return academicYear; }
        public String getSemester() { return semester; }

        @Override
        public String toString() {
            return academicYear + " - " + semester;
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboSuffix.setItems(FXCollections.observableArrayList(
            "",
            "Jr.",
            "Sr.",
            "II",
            "III",
            "IV",
            "V"
        ));

        conn = DBConnection.getConnection();
        model = new PaymentModel(conn);

        loadParticulars();
        loadFund();
        loadSemesters();

        comboSmsStatus.getItems().addAll("iSMS", "eSMS", "IGP");
        comboParticular.setOnAction(e -> updateAmountFromParticular());
        btnSave.setOnAction(e -> savePayment());
        btnCancel.setOnAction(e -> closeModal());
        
        setupStudentIdLookup();
        
        comboMfoPap.setOnAction(e -> {
            FundItem selectedFund = comboMfoPap.getValue();
            if (selectedFund != null && selectedFund.getId() != 0) {
                loadAccountsForFund(selectedFund.getId());
            } else {
                comboAccount.getItems().clear();
                comboAccount.getSelectionModel().clearSelection();
            }
        });
        setStudentFieldsEditable(false);
        
        datePaidAt.setVisible(false);
        datePaidAt.setManaged(false);
        paideAtLabel.setVisible(false);
        paideAtLabel.setManaged(false);
    }

    public void setPaymentRecord(PaymentRecord record) {
        this.currentRecord = record;

        txtStudentID.setText(record.getStudentId());
        txtFirstName.setText(record.getFirstName());
        txtLastName.setText(record.getLastName());
        txtMiddleName.setText(record.getMiddleName());
        comboSuffix.setValue(record.getSuffix());
        txtOrNumber.setText(record.getOrNumber());
        txtAmount.setText(String.valueOf(record.getAmount()));

        if (record.getDatePaid() != null && !record.getDatePaid().isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(record.getDatePaid(), formatter);
            datePaidAt.setValue(dateTime.toLocalDate());
        }

        comboSmsStatus.setValue(record.getSmsStatus());

        comboParticular.getItems().stream()
            .filter(p -> p.getName().equals(record.getParticular()))
            .findFirst()
            .ifPresent(comboParticular::setValue);

        FundItem selectedFund = comboMfoPap.getItems().stream()
            .filter(f -> f.getName().equals(record.getMfoPap()))
            .findFirst()
            .orElse(null);

        if (selectedFund != null) {
            comboMfoPap.setValue(selectedFund);

            loadAccountsForFund(selectedFund.getId());

            if (record.getAccountName() != null) {
                comboAccount.getItems().stream()
                    .filter(a -> a.getName().equals(record.getAccountName()))
                    .findFirst()
                    .ifPresent(comboAccount::setValue);
            }
        } else {
            comboAccount.getItems().clear();
            comboAccount.getSelectionModel().clearSelection();
        }
        
            String semIdStr = record.getSemesterId();

            if (semIdStr != null && !semIdStr.isEmpty()) {
                try {
                    int semId = Integer.parseInt(semIdStr);

                    comboSemester.getItems().stream()
                        .filter(s -> s.getId() == semId)
                        .findFirst()
                        .ifPresent(comboSemester::setValue);

                } catch (NumberFormatException ex) {
                    // Optional: log or ignore if semesterId is not a valid number
                    ex.printStackTrace();
                }
            }

        setStudentFieldsEditable(true);
        datePaidAt.setVisible(true);
        datePaidAt.setManaged(true);
        paideAtLabel.setVisible(true);
        paideAtLabel.setManaged(true);
    }
    
    private void loadSemesters() {
        ObservableList<SemesterItem> list = FXCollections.observableArrayList();
        String sql = "SELECT semester_id, academic_year, semester " +
                     "FROM semester " +
                     "WHERE status = 'Active' " +
                     "ORDER BY academic_year DESC, semester";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new SemesterItem(
                        rs.getInt("semester_id"),
                        rs.getString("academic_year"),
                        rs.getString("semester")
                ));
            }

            // Optional empty item at the top (like Fund)
            list.add(0, new SemesterItem(0, "", ""));

            comboSemester.setItems(list);
            comboSemester.getSelectionModel().clearSelection();
            comboSemester.setPromptText("Select Semester");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    
    private void loadParticulars() {
        ObservableList<ParticularItem> particulars = FXCollections.observableArrayList();
        String sql = "SELECT id, particular_name, amount FROM particular WHERE status = 'Active'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                particulars.add(new ParticularItem(
                    rs.getInt("id"),
                    rs.getString("particular_name"),
                    rs.getDouble("amount")
                ));
            }
            comboParticular.setItems(particulars);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadFund() {
        ObservableList<FundItem> funds = FXCollections.observableArrayList();
        String sql = "SELECT id, fund_name FROM fund WHERE status = 'Active'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                funds.add(new FundItem(
                    rs.getInt("id"),
                    rs.getString("fund_name")
                ));
            }
            funds.add(0, new FundItem(0, ""));
//            comboMfoPap.setItems(funds);
//            comboMfoPap.getSelectionModel().selectFirst(); 
            comboMfoPap.setItems(funds);
            comboMfoPap.getSelectionModel().clearSelection();  
            comboMfoPap.setPromptText("Select Fund");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private void loadAccountsForFund(int fundId) {
        ObservableList<AccountItem> accounts = FXCollections.observableArrayList();
        String sql = "SELECT id, account_name FROM account " +
                     "WHERE fund_id = ? AND status = 'Active' " +
                     "ORDER BY account_name";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fundId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(new AccountItem(
                            rs.getInt("id"),
                            rs.getString("account_name")
                    ));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        accounts.add(0, new AccountItem(0, ""));
        comboAccount.setItems(accounts);
        comboAccount.getSelectionModel().clearSelection();
        comboAccount.getSelectionModel().selectFirst();
    }


    private void updateAmountFromParticular() {
        ParticularItem selected = comboParticular.getValue();
        if (selected != null) {
            txtAmount.setText(String.valueOf(selected.getAmount()));
        }
    }
    
    private void savePayment() {
    try {
        if (!validateRequiredFields() || !validateNames()) {
            return;
        }

        String studentId = txtStudentID.getText();
        String fname = txtFirstName.getText();
        String lname = txtLastName.getText();
        String mname = txtMiddleName.getText();
        String suffix = comboSuffix.getValue();

        Integer studentPk = getStudentPkIfExists(studentId);
        if (studentPk == null) {
            showAlert("Student Not Found",
                    "Student ID \"" + studentId + "\" does not exist in the system.");
            return;
        }

        String orNumber = txtOrNumber.getText().trim();
        ParticularItem selectedParticular = comboParticular.getValue();
        FundItem selectedFund = comboMfoPap.getValue();
        AccountItem selectedAccount = comboAccount.getValue();
        
        if (currentRecord != null) {
            if (isOrNumberDuplicate(orNumber, currentRecord.getId())) {
                showAlert("Duplicate OR Number",
                          "OR Number \"" + orNumber + "\" is already in use.");
                return;
            }
        } else {
            if (isOrNumberDuplicate(orNumber, null)) {
                showAlert("Duplicate OR Number",
                          "OR Number \"" + orNumber + "\" is already in use.");
                return;
            }
        }

        double amount = Double.parseDouble(txtAmount.getText());
        String smsStatus = comboSmsStatus.getValue();
        LocalDate paidAt = datePaidAt.getValue();

        if (selectedParticular == null) {
            showAlert("Error", "Please select a Particular.");
            return;
        }
        
        int particularId = selectedParticular.getId();

        Integer fundId = (selectedFund != null && selectedFund.getId() != 0)
                ? selectedFund.getId()
                : null;

        Integer accountId = (selectedAccount != null && selectedAccount.getId() != 0)
                ? selectedAccount.getId()
                : null;
        
        SemesterItem selectedSemester = comboSemester.getValue();
        Integer semesterId = (selectedSemester != null && selectedSemester.getId() != 0)
                ? selectedSemester.getId()
                : null;

        
        boolean success;

        if (currentRecord != null) {
            if (!currentRecord.getParticular().equals(selectedParticular.getName())) {
                String checkSql = "SELECT COUNT(*) FROM collection " +
                                  "WHERE student_id = ? AND particular_id = ? AND id != ?";
                try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                    psCheck.setInt(1, studentPk);
                    psCheck.setInt(2, particularId);
                    psCheck.setString(3, currentRecord.getId());
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            showAlert("Duplicate Payment",
                                    "This student has already paid for the selected Particular.");
                            return;
                        }
                    }
                }
            }

            success = model.updatePayment(
                currentRecord.getId(),
                studentPk,
                orNumber,
                particularId,
                fundId,
                accountId,
                amount,
                smsStatus,
                paidAt.toString(),
                semesterId
            );
        } else {
            String checkSql = "SELECT COUNT(*) FROM collection " +
                              "WHERE student_id = ? AND particular_id = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, studentPk);
                psCheck.setInt(2, particularId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        showAlert("Duplicate Payment",
                                "This student has already paid for the selected Particular.");
                        return;
                    }
                }
            }
                
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            success = model.insertPayment(
                studentPk,
                orNumber,
                particularId,
                fundId,
                accountId,
                amount,
                smsStatus,
                LocalDateTime.now().format(dtf),
                semesterId,
                "Active"
            );
        }

        if (success) {
            showAlert("Success", currentRecord != null ?
                    "Payment updated successfully." :
                    "Payment recorded successfully.");
            if (parentController != null) parentController.loadPayments();
            closeModal();
            clearFields();
            currentRecord = null;
        } else {
            showAlert("Error", "Failed to save payment.");
        }

    } catch (NumberFormatException e) {
        showAlert("Error", "Please enter a valid amount.");
    } catch (NullPointerException e) {
        showAlert("Error", "Please fill all required fields.");
    } catch (SQLException e) {
        e.printStackTrace();
        showAlert("Error", "Database error occurred.");
    }
}


    private void closeModal() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(String title, String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(title);
            alert.setContentText(msg);
            alert.show();
        });
    }

    private void clearFields(){
        txtStudentID.clear();
        txtFirstName.clear();
        txtLastName.clear();
        txtMiddleName.clear();
        comboSuffix.getSelectionModel().clearSelection();
        txtOrNumber.clear();
        txtAmount.clear();
        comboParticular.getSelectionModel().clearSelection();
        comboMfoPap.getSelectionModel().clearSelection();
        comboSmsStatus.getSelectionModel().clearSelection();
        datePaidAt.setValue(null);
    }

    private boolean isOrNumberExist(String orNumber) {
        String sql = "SELECT COUNT(*) FROM collection WHERE or_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private void setStudentFieldsEditable(boolean editable) {
        txtFirstName.setDisable(!editable);
        txtLastName.setDisable(!editable);
        txtMiddleName.setDisable(!editable);
        comboSuffix.setDisable(!editable);
    }


    private boolean validateNames() {
        String regex = ".*\\d.*";

        if (txtFirstName.getText().matches(regex)) {
            showAlert("Invalid Input", "First Name must not contain numbers.");
            return false;
        }

        if (txtLastName.getText().matches(regex)) {
            showAlert("Invalid Input", "Last Name must not contain numbers.");
            return false;
        }

        if (txtMiddleName.getText().matches(regex)) {
            showAlert("Invalid Input", "Middle Name must not contain numbers.");
            return false;
        }

        String suffix = comboSuffix.getValue();
        if (suffix != null && suffix.matches(".*\\d.*")) {
            showAlert("Invalid Input", "Suffix must not contain numbers.");
            return false;
        }
        
        return true;
    }

    public void setParentController(CollectionController controller) {
        this.parentController = controller;
    }

    private boolean validateRequiredFields() {
        StringBuilder errors = new StringBuilder();

        if (txtStudentID.getText() == null || txtStudentID.getText().trim().isEmpty())
            errors.append("• Student ID is required.\n");

        if (txtFirstName.getText() == null || txtFirstName.getText().trim().isEmpty())
            errors.append("• First Name is required.\n");

        if (txtLastName.getText() == null || txtLastName.getText().trim().isEmpty())
            errors.append("• Last Name is required.\n");

        if (comboSuffix.getValue() == null)
            errors.append("• Suffix must be selected.\n");

        if (comboParticular.getValue() == null)
            errors.append("• Particular must be selected.\n");

//        if (comboMfoPap.getValue() == null)
//            errors.append("• Fund must be selected.\n");
//        
//        if (comboAccount.getValue() == null)
//            errors.append("• Account must be selected.\n");

        if (txtAmount.getText() == null || txtAmount.getText().trim().isEmpty())
            errors.append("• Amount is required.\n");

        if (comboSmsStatus.getValue() == null)
            errors.append("• SMS Status must be selected.\n");
        
        if (comboSemester.getValue() == null)
            errors.append("• Semester / AY must be selected.\n");

        if (errors.length() > 0) {
            showAlert("Missing Required Fields", errors.toString());
            return false;
        }
        return true;
    }
    
    private Integer getStudentPkIfExists(String studentNumber) throws SQLException {
        String findSql = "SELECT id FROM student WHERE student_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setString(1, studentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return null;
    }

    private static class StudentData {
        String firstName;
        String lastName;
        String middleName;
        String suffix;

        StudentData(String firstName, String lastName, String middleName, String suffix) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.middleName = middleName;
            this.suffix = suffix;
        }
    }

private void setupStudentIdLookup() {
    PauseTransition debounce = new PauseTransition(Duration.seconds(2));

    txtStudentID.textProperty().addListener((obs, oldVal, newVal) -> {
        debounce.stop();
        debounce.setOnFinished(e -> handleStudentIdChanged(newVal));
        debounce.play();
    });
}

private void handleStudentIdChanged(String studentNumber) {
    String trimmed = (studentNumber == null) ? "" : studentNumber.trim();

    if (trimmed.isEmpty()) {
        clearStudentNameFields();
        return;
    }

    try {
        StudentData data = findStudentByStudentId(trimmed);

        if (data != null) {
            // Auto-fill name fields
            txtFirstName.setText(data.firstName);
            txtLastName.setText(data.lastName);
            txtMiddleName.setText(data.middleName);
            comboSuffix.setValue(data.suffix);
        } else {
            clearStudentNameFields();
            showAlert("Student Not Found",
                      "Student ID \"" + trimmed + "\" does not exist in the database.");
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
        showAlert("Error", "Failed to fetch student details from the database.");
    }
}

private StudentData findStudentByStudentId(String studentNumber) throws SQLException {
    String sql = "SELECT first_name, last_name, middle_name, suffix " +
                 "FROM student WHERE student_id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, studentNumber);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new StudentData(
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("middle_name"),
                    rs.getString("suffix")
                );
            }
        }
    }
    return null;
}

    private void clearStudentNameFields() {
        txtFirstName.clear();
        txtLastName.clear();
        txtMiddleName.clear();
        comboSuffix.getSelectionModel().clearSelection();
    }
    
    private boolean isOrNumberDuplicate(String orNumber, String excludeId) {
        String sql = "SELECT COUNT(*) FROM collection WHERE or_number = ?";

        boolean hasExclude = (excludeId != null && !excludeId.isEmpty());
        if (hasExclude) {
            sql += " AND id <> ?";
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orNumber);
            if (hasExclude) {
                ps.setString(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
