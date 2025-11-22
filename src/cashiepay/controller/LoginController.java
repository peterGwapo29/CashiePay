package cashiepay.controller;

import java.net.URL;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import cashiepay.model.DBConnection;
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
               System.out.println("Please fill in all fields!");
               return;
           }
            try {
                if (conn == null || conn.isClosed()) {
                    conn = DBConnection.getConnection();
                }
                
                String sql = "SELECT * FROM admin WHERE username = ? AND password = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, email);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    System.out.println("Login");
                    
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
                    System.out.println("Invalid email or password.");
                }
                rs.close();
                ps.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
