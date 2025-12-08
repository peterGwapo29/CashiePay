package cashiepay;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Cashiepay extends Application{

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/Login.fxml"));
        AnchorPane root = loader.load();

        root.setUserData(loader.getController());

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("CashiePay");
        stage.show();
    }
}
