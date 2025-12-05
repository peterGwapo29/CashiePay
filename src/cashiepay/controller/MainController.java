package cashiepay.controller;

import cashiepay.sidebar.SidebarLoader;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class MainController implements Initializable {

    @FXML
    private VBox sidebarContainer;
    @FXML
    private AnchorPane contentContainer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        AnchorPane sidebar = SidebarLoader.getSidebar();
        if (sidebar != null) {
            sidebarContainer.getChildren().setAll(sidebar);
        } else {
            System.out.println("Sidebar failed to load!");
        }

        loadContent("/cashiepay/view/Dashboard.fxml");
    }

    public void loadContent(String fxmlPath) {
        try {
            AnchorPane content = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentContainer.getChildren().setAll(content);
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }
    
}
