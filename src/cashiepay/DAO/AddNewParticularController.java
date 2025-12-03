package cashiepay.DAO;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import cashiepay.model.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class AddNewParticularController implements Initializable {

    @FXML
    private TextField particularNameField;
    @FXML
    private TextField amountField;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        saveButton.setOnAction(e -> saveParticular());
        cancelButton.setOnAction(e -> closeModal());
    }    
    
    private void closeModal() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void saveParticular() {

        String name = particularNameField.getText().trim();
        String amount = amountField.getText().trim();

        // -------------------------
        // VALIDATION
        // -------------------------
        if (name.isEmpty() || amount.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill out all fields.");
            return;
        }

        if (!amount.matches("\\d+(\\.\\d+)?")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Amount", "Amount must be a valid number.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            // -------------------------
            // CHECK FOR DUPLICATE NAME
            // -------------------------
            String checkSql = "SELECT COUNT(*) FROM particular WHERE particular_name = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, name);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {
                showAlert(Alert.AlertType.ERROR, "Duplicate Entry", "This particular already exists.");
                return;
            }

            // -------------------------
            // INSERT INTO DB
            // -------------------------
            String sql = "INSERT INTO particular (particular_name, amount) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, amount);

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Particular saved successfully!");

                // Close modal after saving
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
