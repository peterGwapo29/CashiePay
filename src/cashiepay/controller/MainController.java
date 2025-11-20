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
import javafx.scene.layout.Pane;

public class MainController implements Initializable {

    @FXML
    private VBox sidebarContainer;
    @FXML
    private AnchorPane contentContainer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Set this controller as user data on root so sidebar can access it
            sidebarContainer.getScene().getRoot().setUserData(this);
        } catch (Exception e) {
            // Scene may not be ready yet; we'll fix this in MainApp
        }

        // Load sidebar
        AnchorPane sidebar = SidebarLoader.getSidebar();
        if (sidebar != null) {
            sidebarContainer.getChildren().setAll(sidebar);
        } else {
            System.out.println("Sidebar failed to load!");
        }

        // Load default content
        loadContent("/cashiepay/view/Dashboard.fxml");
    }

    /** Load content into the main content container */
    public void loadContent(String fxmlPath) {
        try {
            AnchorPane content = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentContainer.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
