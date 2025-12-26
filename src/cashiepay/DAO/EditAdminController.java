package cashiepay.DAO;

import cashiepay.model.Admin;
import cashiepay.model.DBConnection;
import cashiepay.util.PasswordUtil;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditAdminController implements Initializable {

    @FXML private TextField adminNameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Admin admin;
    private Runnable onSaved;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        saveButton.setOnAction(e -> saveAction());
        cancelButton.setOnAction(e -> close());
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
        loadAdminData();
    }

    private void loadAdminData() {
        if (admin == null) return;
        adminNameField.setText(admin.getAdminName());
        emailField.setText(admin.getEmailAddress());
        usernameField.setText(admin.getUsername());
    }

    private void saveAction() {
        if (admin == null) return;

        String adminName = adminNameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();

        String newPass = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (adminName.isEmpty() || email.isEmpty() || username.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Admin Name, Email and Username are required.");
            return;
        }

        boolean updatePassword = newPass != null && !newPass.isEmpty();
        if (updatePassword && !newPass.equals(confirm)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Password does not match.");
            return;
        }

        if (isDuplicateUsername(username, admin.getId())) {
            showAlert(Alert.AlertType.ERROR, "Duplicate Error",
                    "Username \"" + username + "\" already exists.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            if (updatePassword) {
                String sql = "UPDATE admin SET admin_name=?, email_address=?, username=?, password=?, updated_at=NOW() WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, adminName);
                    ps.setString(2, email);
                    ps.setString(3, username);
                    ps.setString(4, PasswordUtil.hashPassword(newPass));
                    ps.setInt(5, admin.getId());
                    ps.executeUpdate();
                }
            } else {
                String sql = "UPDATE admin SET admin_name=?, email_address=?, username=?, updated_at=NOW() WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, adminName);
                    ps.setString(2, email);
                    ps.setString(3, username);
                    ps.setInt(4, admin.getId());
                    ps.executeUpdate();
                }
            }

            // update local object (optional)
            admin.setAdminName(adminName);
            admin.setEmailAddress(email);
            admin.setUsername(username);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Admin updated successfully.");

            if (onSaved != null) onSaved.run();
            close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update admin.");
        }
    }

    private boolean isDuplicateUsername(String username, int currentId) {
        String sql = "SELECT COUNT(*) FROM admin WHERE username = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setInt(2, currentId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void close() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
