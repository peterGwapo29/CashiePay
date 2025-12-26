package cashiepay.controller;

import cashiepay.model.Auth.AdminSession;
import cashiepay.util.PasswordUtil;
import cashiepay.model.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button updateInfoButton;

    private String originalName;
    private String originalEmail;
    private String originalUsername;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadAdminInfo();
        updateInfoButton.setOnAction(e -> updateAdminInfo());
    }

    private void loadAdminInfo() {
        originalName = AdminSession.getAdminName();
        originalEmail = AdminSession.getEmail();
        originalUsername = AdminSession.getUsername();

        fullNameField.setText(originalName);
        emailField.setText(originalEmail);
        usernameField.setText(originalUsername);

        updateInfoButton.setDisable(true);
        addChangeListeners();
    }

    private void updateAdminInfo() {

        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String adminName = fullNameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (email.isEmpty() || username.isEmpty() || adminName.isEmpty()) {
            showAlert("Missing Fields", "Please fill all required fields.");
            return;
        }

        int adminId = AdminSession.getId(); // <-- use session ID

        boolean passwordChanged = !password.isEmpty() || !confirmPassword.isEmpty();

        if (passwordChanged) {

            if (!password.equals(confirmPassword)) {
                showAlert("Password Error", "Passwords do not match.");
                return;
            }

            if (password.length() < 8 ||
                !password.matches(".*[!@#$%^&*()_+=<>?/\\\\|{}\\[\\]-].*")) {
                showAlert("Weak Password",
                        "Password must be at least 8 characters long and contain at least one symbol.");
                return;
            }
        }

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement stmt;

            if (passwordChanged) {
                String hashedPassword = PasswordUtil.hashPassword(password);

                String sql = "UPDATE admin SET admin_name=?, email_address=?, username=?, password=?, updated_at=NOW() WHERE id=?";
                stmt = conn.prepareStatement(sql);

                stmt.setString(1, adminName);
                stmt.setString(2, email);
                stmt.setString(3, username);
                stmt.setString(4, hashedPassword);
                stmt.setInt(5, adminId);

            } else {

                String sql = "UPDATE admin SET admin_name=?, email_address=?, username=?, updated_at=NOW() WHERE id=?";
                stmt = conn.prepareStatement(sql);

                stmt.setString(1, adminName);
                stmt.setString(2, email);
                stmt.setString(3, username);
                stmt.setInt(4, adminId);
            }

            int updated = stmt.executeUpdate();

            if (updated > 0) {

                AdminSession.setSession(
                        adminId,
                        adminName,
                        email,
                        username,
                        AdminSession.getRole()
                );

                if (passwordChanged) {
                    showAlert("Password Updated",
                        "Your password has been successfully updated.\nYou will be logged out for security.");
                    logoutAndReturnToLogin();
                } else {
                    showAlert("Profile Updated",
                        "Your profile information has been successfully updated.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred while updating your profile.");
        }
    }

    private void checkForChanges() {
        boolean textChanged =
                !fullNameField.getText().equals(originalName) ||
                !emailField.getText().equals(originalEmail) ||
                !usernameField.getText().equals(originalUsername);

        boolean passwordChanged =
                !passwordField.getText().isEmpty() ||
                !confirmPasswordField.getText().isEmpty();

        updateInfoButton.setDisable(!(textChanged || passwordChanged));
    }

    private void addChangeListeners() {
        fullNameField.textProperty().addListener((obs, oldVal, newVal) -> checkForChanges());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> checkForChanges());
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> checkForChanges());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> checkForChanges());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> checkForChanges());
    }

    private void logoutAndReturnToLogin() {
        try {
            AdminSession.clear();

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/cashiepay/view/Login.fxml")
            );

            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) updateInfoButton.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("CashiePay - Login");
            stage.centerOnScreen();
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Failed to return to login screen.");
        }
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
