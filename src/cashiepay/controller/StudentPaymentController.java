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

public class StudentPaymentController implements Initializable {

    @FXML
    private TextField txtOrNumber, txtAmount;
    
    @FXML
    private ComboBox<ParticularItem> comboParticular;

    @FXML
    private ComboBox<MfoPapItem> comboMfoPap;

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

    public static class MfoPapItem {
        private int id;
        private String name;

        public MfoPapItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() { return name; }
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
        loadMfoPap();

        comboSmsStatus.getItems().addAll("iSMS", "eSMS");

        comboParticular.setOnAction(e -> updateAmountFromParticular());
        btnSave.setOnAction(e -> savePayment());
        btnCancel.setOnAction(e -> closeModal());
    }
    
    public void setPaymentRecord(PaymentRecord record) {
        this.currentRecord = record;

        // Pre-fill form fields with the record data
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

        // Select Particular in ComboBox
        comboParticular.getItems().stream()
            .filter(p -> p.getName().equals(record.getParticular()))
            .findFirst()
            .ifPresent(comboParticular::setValue);

        // Select MFO/PAP in ComboBox
        comboMfoPap.getItems().stream()
            .filter(m -> m.getName().equals(record.getMfoPap()))
            .findFirst()
            .ifPresent(comboMfoPap::setValue);

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

    private void loadMfoPap() {
        ObservableList<MfoPapItem> mfoPaps = FXCollections.observableArrayList();
        String sql = "SELECT id, mfo_pap_name FROM mfo_pap";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mfoPaps.add(new MfoPapItem(
                    rs.getInt("id"),
                    rs.getString("mfo_pap_name")
                ));
            }
            comboMfoPap.setItems(mfoPaps);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
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
            ParticularItem selectedParticular = comboParticular.getValue();
            MfoPapItem selectedMfoPap = comboMfoPap.getValue();
            double amount = Double.parseDouble(txtAmount.getText());
            String smsStatus = comboSmsStatus.getValue();
            LocalDate paidAt = datePaidAt.getValue();

            if (selectedParticular == null || selectedMfoPap == null) {
                showAlert("Error", "Please select Particular and MFO/PAP.");
                return;
            }

            int particularId = selectedParticular.getId();
            int mfoPapId = selectedMfoPap.getId();

            boolean success = false;

            if (currentRecord != null) {
                if (!currentRecord.getParticular().equals(selectedParticular.getName())) {
                    String checkSql = "SELECT COUNT(*) FROM collection WHERE student_id = ? AND particular = ? AND id != ?";
                    try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                        psCheck.setString(1, studentId);
                        psCheck.setInt(2, particularId);
                        psCheck.setString(3, currentRecord.getId());
                        try (ResultSet rs = psCheck.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                showAlert("Duplicate Payment", "This student has already paid for the selected Particular.");
                                return; // stop saving
                            }
                        }
                    }
                }

                success = model.updatePayment(
                    currentRecord.getId(),
                    studentId,
                    fname,
                    lname,
                    mname,
                    suffix,
                    particularId,
                    mfoPapId,
                    amount,
                    smsStatus,
                    paidAt.toString()
                );
            } else {
                String checkSql = "SELECT COUNT(*) FROM collection WHERE student_id = ? AND particular = ?";
                try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                    psCheck.setString(1, studentId);
                    psCheck.setInt(2, particularId);
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            showAlert("Duplicate Payment", "This student has already paid for the selected Particular.");
                            return;
                        }
                    }
                }

                String orNumber = generateOrNumber();
                txtOrNumber.setText(orNumber);

                success = model.insertPayment(
                    studentId,
                    fname,
                    lname,
                    mname,
                    suffix,
                    orNumber,
                    particularId,
                    mfoPapId,
                    amount,
                    smsStatus,
                    paidAt.toString()
                );
            }

            if (success) {
                showAlert("Success", currentRecord != null ? "Payment updated successfully." : "Payment recorded successfully.");
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        alert.showAndWait();
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
    
    private String generateOrNumber() {
        String sql = "SELECT or_number FROM collection ORDER BY CAST(or_number AS UNSIGNED) DESC LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            String lastOr = null;

            if (rs.next()) {
                lastOr = rs.getString("or_number");
            }

            // If the DB has no records or the value is invalid
            if (lastOr == null || !lastOr.matches("\\d+")) {
                lastOr = "00000000";
            }

            int nextOrInt = Integer.parseInt(lastOr) + 1;

            // pad to 8 digits
            String nextOrNumber = String.format("%08d", nextOrInt);

            // ensure OR number is unique
            while (isOrNumberExist(nextOrNumber)) {
                nextOrInt++;
                nextOrNumber = String.format("%08d", nextOrInt);
            }

            return nextOrNumber;

        } catch (Exception e) {
            e.printStackTrace();
            return "00000001";
        }
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

        if (txtMiddleName.getText() == null || txtMiddleName.getText().trim().isEmpty())
            errors.append("• Middle Name is required.\n");

        if (comboSuffix.getValue() == null)
            errors.append("• Suffix must be selected.\n");

        if (comboParticular.getValue() == null)
            errors.append("• Particular must be selected.\n");

        if (comboMfoPap.getValue() == null)
            errors.append("• MFO/PAP must be selected.\n");

        if (txtAmount.getText() == null || txtAmount.getText().trim().isEmpty())
            errors.append("• Amount is required.\n");

        if (comboSmsStatus.getValue() == null)
            errors.append("• SMS Status must be selected.\n");

        if (datePaidAt.getValue() == null)
            errors.append("• Payment Date is required.\n");

        if (errors.length() > 0) {
            showAlert("Missing Required Fields", errors.toString());
            return false;
        }
        return true;
    }
}
