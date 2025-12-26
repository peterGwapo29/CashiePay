package cashiepay.DAO;

import cashiepay.model.DBConnection;
import cashiepay.util.PasswordUtil;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddNewAdminController implements Initializable {

    @FXML private TextField adminNameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Runnable onSaved; // callback to reload table

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        saveButton.setOnAction(e -> saveAdmin());
        cancelButton.setOnAction(e -> closeModal());
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    private void closeModal() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private void saveAdmin() {
        String adminName = adminNameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (adminName.isEmpty() || email.isEmpty() || username.isEmpty() || pass.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields are required.");
            return;
        }

        if (!pass.equals(confirm)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Password does not match.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            // check duplicate username
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT COUNT(*) FROM admin WHERE username = ?")) {
                check.setString(1, username);
                ResultSet rs = check.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    showAlert(Alert.AlertType.ERROR, "Duplicate Entry", "Username already exists.");
                    return;
                }
            }

            String sql = "INSERT INTO admin (admin_name, email_address, username, password, status) " +
                         "VALUES (?, ?, ?, ?, 'Active')";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, adminName);
                ps.setString(2, email);
                ps.setString(3, username);
                ps.setString(4, PasswordUtil.hashPassword(pass));

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Admin created successfully.");
                    if (onSaved != null) onSaved.run();
                    closeModal();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save admin.");
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
