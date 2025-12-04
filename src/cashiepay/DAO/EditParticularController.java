package cashiepay.DAO;

import cashiepay.model.DBConnection;
import cashiepay.model.Particular;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EditParticularController {

    @FXML private TextField nameField;
    @FXML private TextField amountField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private Particular particular;

    public void setParticular(Particular particular) {
        this.particular = particular;
        loadParticularData();
    }

    private void loadParticularData() {
        if (particular != null) {
            nameField.setText(particular.getParticularName());
            amountField.setText(particular.getAmount());
        }
    }

    @FXML
    private void saveAction(ActionEvent event) {
        String newName = nameField.getText().trim();
        String newAmount = amountField.getText().trim();

        if (newName.isEmpty() || newAmount.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill all fields.");
            return;
        }

        if (isDuplicate(newName, particular.getId())) {
            showAlert(Alert.AlertType.ERROR, "Duplicate Error",
                    "The particular name \"" + newName + "\" already exists.");
            return;
        }

        String sql = "UPDATE particular SET particular_name = ?, amount = ?, updated_at = NOW() WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newName);
            stmt.setString(2, newAmount);
            stmt.setInt(3, particular.getId());

            stmt.executeUpdate();

            particular.setParticularName(newName);
            particular.setAmount(newAmount);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Particular updated successfully!");

            ((Stage) saveBtn.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save the updated particular.");
        }
    }

    private boolean isDuplicate(String name, int currentId) {
        String sql = "SELECT COUNT(*) FROM particular WHERE particular_name = ? AND id != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, currentId);

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
    private void cancelAction(ActionEvent event) {
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
