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
import java.time.LocalDate;

public class StudentPaymentController implements Initializable {

    @FXML
    private TextField txtOrNumber, txtAmount;
    
    @FXML
    private ComboBox<ParticularItem> comboParticular;

    @FXML
    private ComboBox<MfoPapItem> comboMfoPap;

    @FXML
    private ComboBox<String> comboPaymentStatus, comboSmsStatus;
    
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

        comboPaymentStatus.getItems().addAll("Paid", "Not Paid");
        comboSmsStatus.getItems().addAll("iSMS", "eSMS");

        comboParticular.setOnAction(e -> updateAmountFromParticular());
        btnSave.setOnAction(e -> savePayment());
        btnCancel.setOnAction(e -> closeModal());
    }

    private void loadParticulars() {
        ObservableList<ParticularItem> particulars = FXCollections.observableArrayList();
        String sql = "SELECT id, particular_name, amount FROM particular";
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
            
            String orNumber = generateOrNumber();
            txtOrNumber.setText(orNumber);
            if (!validateRequiredFields()) {
                return;
            }
            if (!validateNames()) {
                return;
            }

            String studentId = txtStudentID.getText();
            String fname = txtFirstName.getText();
            String lname = txtLastName.getText();
            String mname = txtMiddleName.getText();
            String suffix = comboSuffix.getValue();
            txtOrNumber.setText(orNumber);

            ParticularItem selectedParticular = comboParticular.getValue();
            MfoPapItem selectedMfoPap = comboMfoPap.getValue();

            if (selectedParticular == null || selectedMfoPap == null) {
                showAlert("Error", "Please select Particular and MFO/PAP.");
                return;
            }

            int particularId = selectedParticular.getId();
            int mfoPapId = selectedMfoPap.getId();
            double amount = Double.parseDouble(txtAmount.getText());
            String paymentStatus = comboPaymentStatus.getValue();
            String smsStatus = comboSmsStatus.getValue();
            LocalDate paidAt = datePaidAt.getValue();

            String checkSql = "SELECT COUNT(*) FROM collection WHERE student_id = ? AND particular = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setString(1, studentId);
                psCheck.setInt(2, particularId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        showAlert("Duplicate Payment", "This student has already paid for the selected particular.");
                        return;
                    }
                }
            }

            if (model.insertPayment(
                    studentId,
                    fname,
                    lname,
                    mname,
                    suffix,
                    orNumber,
                    particularId,
                    mfoPapId,  
                    amount,
                    paymentStatus,
                    smsStatus,
                    paidAt.toString()
            )) {
                showAlert("Success", "Payment recorded successfully.");
                if (parentController != null) {
                    parentController.loadPayments();
                }
                clearFields();
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
        comboPaymentStatus.getSelectionModel().clearSelection();
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

        if (comboPaymentStatus.getValue() == null)
            errors.append("• Payment Status must be selected.\n");

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
