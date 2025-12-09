package cashiepay.DAO;

import cashiepay.model.DBConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

public class AddNewStudentController implements Initializable {

    @FXML private TextField studentIdField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField middleNameField;
    @FXML private ComboBox<String> suffixCombo;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // suffix choices
        suffixCombo.getItems().addAll(
                "",      // no suffix
                "Jr.",
                "Sr.",
                "II",
                "III",
                "IV",
                "V"
        );
        suffixCombo.setValue("");  // default: none

        saveButton.setOnAction(e -> saveStudent());
        cancelButton.setOnAction(e -> closeModal());
    }

    private void closeModal() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private void saveStudent() {
        String studentId  = studentIdField.getText().trim();
        String firstName  = firstNameField.getText().trim();
        String lastName   = lastNameField.getText().trim();
        String middleName = middleNameField.getText().trim();
        String suffix     = suffixCombo.getValue() == null ? "" : suffixCombo.getValue().trim();

        if (studentId.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Student ID, First Name and Last Name are required.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            String checkSql = "SELECT COUNT(*) FROM student WHERE student_id = ?";
            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setString(1, studentId);
                ResultSet rs = check.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    showAlert(Alert.AlertType.ERROR, "Duplicate Entry",
                            "Student ID already exists.");
                    return;
                }
            }

            String sql = "INSERT INTO student " +
                    "(student_id, first_name, last_name, middle_name, suffix, status) " +
                    "VALUES (?, ?, ?, ?, ?, 'Active')";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                stmt.setString(2, firstName);
                stmt.setString(3, lastName);
                stmt.setString(4, middleName);
                stmt.setString(5, suffix);
                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Student saved successfully.");
                    closeModal();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Failed to save student: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
