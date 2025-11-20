package cashiepay.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class LoginController implements Initializable {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginBtn;
    
    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void handleLoginAction(ActionEvent event) throws IOException {
        if(event.getSource() == loginBtn) {
             try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/Main.fxml"));
                AnchorPane root = loader.load();

                stage = (Stage) loginBtn.getScene().getWindow();
                
                root.setUserData(loader.getController());

                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("CashiePay");
                stage.centerOnScreen();
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
