package cashiepay.DAO;

import cashiepay.model.DBConnection;
import cashiepay.model.Student;
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

public class EditStudentController implements Initializable {

    @FXML private TextField studentIdField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField middleNameField;
    @FXML private ComboBox<String> suffixCombo;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private Student student;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        suffixCombo.getItems().addAll(
                "",
                "Jr.",
                "Sr.",
                "II",
                "III",
                "IV",
                "V"
        );
    }

    public void setStudent(Student s) {
        this.student = s;
        loadStudentData();
    }

    private void loadStudentData() {
        if (student == null) return;

        studentIdField.setText(student.getStudentId());
        firstNameField.setText(student.getFirstName());
        lastNameField.setText(student.getLastName());
        middleNameField.setText(student.getMiddleName());

        String currentSuffix = student.getSuffix() == null ? "" : student.getSuffix();
        if (!suffixCombo.getItems().contains(currentSuffix)) {
            suffixCombo.getItems().add(currentSuffix);
        }
        suffixCombo.setValue(currentSuffix);
    }

    @FXML
    private void saveAction() {
        String studentId  = studentIdField.getText().trim();
        String firstName  = firstNameField.getText().trim();
        String lastName   = lastNameField.getText().trim();
        String middleName = middleNameField.getText().trim();
        String suffix     = suffixCombo.getValue() == null ? "" : suffixCombo.getValue().trim();

        if (studentId.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Student ID, First Name and Last Name are required.");
            return;
        }

        if (isDuplicateStudentId(studentId, student.getId())) {
            showAlert(Alert.AlertType.ERROR, "Duplicate Error",
                    "Student ID \"" + studentId + "\" already exists.");
            return;
        }

        String sql = "UPDATE student SET student_id = ?, first_name = ?, " +
                "last_name = ?, middle_name = ?, suffix = ?, " +
                "updated_at = NOW() WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, middleName);
            stmt.setString(5, suffix);
            stmt.setInt(6, student.getId());

            stmt.executeUpdate();

            student.setStudentId(studentId);
            student.setFirstName(firstName);
            student.setLastName(lastName);
            student.setMiddleName(middleName);
            student.setSuffix(suffix);

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Student updated successfully.");
            close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to update student.");
        }
    }

    private boolean isDuplicateStudentId(String studentId, int currentId) {
        String sql = "SELECT COUNT(*) FROM student WHERE student_id = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            stmt.setInt(2, currentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    private void cancelAction() {
        close();
    }

    private void close() {
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
