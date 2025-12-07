package cashiepay.DAO;

import cashiepay.model.DBConnection;
import cashiepay.model.MfoPap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditMfoPapController {

    @FXML private TextField nameField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private MfoPap mfoPap;

    public void setMfoPap(MfoPap mfoPap) {
        this.mfoPap = mfoPap;
        loadData();
    }

    private void loadData() {
        if (mfoPap != null) {
            nameField.setText(mfoPap.getMfoPapName());
        }
    }

    @FXML
    private void saveAction(ActionEvent event) {
        String newName = nameField.getText().trim();

        if (newName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter MFO/PAP name.");
            return;
        }

        if (isDuplicate(newName, mfoPap.getId())) {
            showAlert(Alert.AlertType.ERROR, "Duplicate Error",
                    "The MFO/PAP name \"" + newName + "\" already exists.");
            return;
        }

        String sql = "UPDATE mfo_pap SET mfo_pap_name = ?, updated_at = NOW() WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newName);
            stmt.setInt(2, mfoPap.getId());
            stmt.executeUpdate();

            mfoPap.setMfoPapName(newName);

            showAlert(Alert.AlertType.INFORMATION, "Success", "MFO/PAP updated successfully!");
            ((Stage) saveBtn.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save the updated MFO/PAP.");
        }
    }

    private boolean isDuplicate(String name, int currentId) {
        String sql = "SELECT COUNT(*) FROM mfo_pap WHERE mfo_pap_name = ? AND id != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, currentId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

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
