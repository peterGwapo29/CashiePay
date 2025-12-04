package cashiepay.util;


import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoadingDialog {

    private final Stage stage = new Stage();

    public LoadingDialog(String message) {
        ProgressIndicator spinner = new ProgressIndicator();
        Label lblMessage = new Label(message);

        VBox root = new VBox(15, spinner, lblMessage);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-border-radius: 10;");

        Scene scene = new Scene(root);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        
        stage.setOnShown(ev -> centerOnScreen());
    }
    
    private void centerOnScreen() {
        javafx.geometry.Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }

    public void show() {
        stage.show();
    }

    public void close() {
        stage.close();
    }
}