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

public class AddNewMfoPapController implements Initializable {

    @FXML private TextField mfoPapNameField;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        saveButton.setOnAction(e -> saveMfoPap());
        cancelButton.setOnAction(e -> closeModal());
    }

    private void closeModal() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void saveMfoPap() {
        String name = mfoPapNameField.getText().trim();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter MFO/PAP name.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            // Duplicate check
            String checkSql = "SELECT COUNT(*) FROM mfo_pap WHERE mfo_pap_name = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, name);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {
                showAlert(Alert.AlertType.ERROR, "Duplicate Entry", "This MFO/PAP already exists.");
                return;
            }

            String sql = "INSERT INTO mfo_pap (mfo_pap_name) VALUES (?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "MFO/PAP saved successfully!");
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
