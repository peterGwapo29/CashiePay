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
import javafx.stage.Stage;

public class AddNewSemesterController implements Initializable {

    @FXML private TextField semesterNameField;
    @FXML private TextField schoolYearField;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        saveButton.setOnAction(e -> saveSemester());
        cancelButton.setOnAction(e -> closeModal());
    }

    private void closeModal() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void saveSemester() {

        String semName = semesterNameField.getText().trim();
        String schoolYear = schoolYearField.getText().trim();

        if (semName.isEmpty() || schoolYear.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill out all fields.");
            return;
        }

        // optional: simple school year pattern like 2024-2025
        if (!schoolYear.matches("\\d{4}-\\d{4}")) {
            showAlert(Alert.AlertType.ERROR, "Invalid School Year",
                    "School year must be in format YYYY-YYYY (e.g. 2024-2025).");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            // check duplicate (same semester + school year)
            String checkSql = "SELECT COUNT(*) FROM semester WHERE semester = ? AND academic_year  = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, semName);
            checkStmt.setString(2, schoolYear);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {
                showAlert(Alert.AlertType.ERROR, "Duplicate Entry",
                        "This semester and school year already exist.");
                return;
            }

            String sql = "INSERT INTO semester (academic_year, semester) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, schoolYear);
            stmt.setString(2, semName);

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Semester saved successfully!");
                closeModal();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save data: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
