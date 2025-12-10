package cashiepay.DAO;

import cashiepay.model.DBConnection;
import cashiepay.model.Semester;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditSemesterController {

    @FXML private TextField semesterNameField;
    @FXML private TextField schoolYearField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private Semester semester;

    public void setSemester(Semester semester) {
        this.semester = semester;
        loadSemesterData();
    }

    private void loadSemesterData() {
        if (semester != null) {
            semesterNameField.setText(semester.getSemesterName());
            schoolYearField.setText(semester.getSchoolYear());
        }
    }

    @FXML
    private void saveAction(javafx.event.ActionEvent event) {
        String newName = semesterNameField.getText().trim();
        String newSY = schoolYearField.getText().trim();

        if (newName.isEmpty() || newSY.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill all fields.");
            return;
        }

        if (!newSY.matches("\\d{4}-\\d{4}")) {
            showAlert(Alert.AlertType.ERROR, "Invalid School Year",
                    "School year must be in format YYYY-YYYY.");
            return;
        }

        if (isDuplicate(newName, newSY, semester.getId())) {
            showAlert(Alert.AlertType.ERROR, "Duplicate Error",
                    "This semester and school year already exist.");
            return;
        }

        String sql = "UPDATE semester SET semester = ?, academic_year = ?, updated_at = NOW() WHERE semester_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newName);
            stmt.setString(2, newSY);
            stmt.setInt(3, semester.getId());

            stmt.executeUpdate();

            semester.setSemesterName(newName);
            semester.setSchoolYear(newSY);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Semester updated successfully!");

            ((Stage) saveBtn.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save the updated semester.");
        }
    }

    private boolean isDuplicate(String name, String sy, int currentId) {
        String sql = "SELECT COUNT(*) FROM semester WHERE semester = ? AND academic_year = ? AND semester_id != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, sy);
            stmt.setInt(3, currentId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @FXML
    private void cancelAction(javafx.event.ActionEvent event) {
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
