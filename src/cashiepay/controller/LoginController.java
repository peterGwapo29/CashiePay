package cashiepay.controller;

import cashiepay.model.Auth.AdminSession;
import java.net.URL;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import cashiepay.model.DBConnection;
import cashiepay.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import java.io.IOException;
import java.sql.SQLException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;

public class LoginController implements Initializable {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginBtn;
    
    private Stage stage;
    private Connection conn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conn = DBConnection.getConnection();
    }

    @FXML
    private void handleLoginAction(ActionEvent event) throws SQLException, IOException {
        if(event.getSource() == loginBtn) {
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            if (email.isEmpty() || password.isEmpty()) {
                showAlert("Missing Fields", "Please fill in all fields!");
                return;
            }

            try {
                if (conn == null || conn.isClosed()) {
                    conn = DBConnection.getConnection();
                }

                String sql = "SELECT * FROM admin WHERE username = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    String storedHashedPassword = rs.getString("password");

                    boolean isPasswordCorrect = PasswordUtil.checkPassword(password, storedHashedPassword);

                    if (!isPasswordCorrect) {
                        showAlert("Login Failed", "Incorrect username or password.");
                        return;
                    }

                    AdminSession.setSession(
                        rs.getInt("id"),
                        rs.getString("admin_name"),
                        rs.getString("email_address"),
                        rs.getString("username")
                    );

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/Main.fxml"));
                    AnchorPane root = loader.load();

                    stage = (Stage) loginBtn.getScene().getWindow();

                    root.setUserData(loader.getController());

                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.setTitle("CashiePay");
                    stage.centerOnScreen();
                    stage.show();

                } else {
                    showAlert("Login Failed", "Incorrect username or password.");
                }

                rs.close();
                ps.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
