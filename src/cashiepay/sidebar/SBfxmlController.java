package cashiepay.sidebar;

import cashiepay.controller.MainController;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class SBfxmlController {

    @FXML
    private AnchorPane sidebar;

    @FXML
    private Button dashboardBtn, collectionBtn, particularBtn, mfopapBtn, logoutBtn, profileBtn;

//    public void initController() {
//        dashboardBtn.setOnAction(e -> loadContent("/cashiepay/view/Dashboard.fxml"));
//        collectionBtn.setOnAction(e -> loadContent("/cashiepay/view/Collection.fxml"));
//        particularBtn.setOnAction(e -> loadContent("/cashiepay/view/particular.fxml"));
//        mfopapBtn.setOnAction(e -> loadContent("/cashiepay/view/Mfo_pap.fxml"));
//        profileBtn.setOnAction(e -> loadContent("/cashiepay/view/Profile.fxml"));
//        logoutBtn.setOnAction(e -> logout());
//    }
    
    public void initController() {
        setActiveNav(dashboardBtn);
        loadContent("/cashiepay/view/Dashboard.fxml");
    
        dashboardBtn.setOnAction(e -> {
            setActiveNav(dashboardBtn);
            loadContent("/cashiepay/view/Dashboard.fxml");
        });

        collectionBtn.setOnAction(e -> {
            setActiveNav(collectionBtn);
            loadContent("/cashiepay/view/Collection.fxml");
        });

        particularBtn.setOnAction(e -> {
            setActiveNav(particularBtn);
            loadContent("/cashiepay/view/particular.fxml");
        });

        mfopapBtn.setOnAction(e -> {
            setActiveNav(mfopapBtn);
            loadContent("/cashiepay/view/Mfo_pap.fxml");
        });

        profileBtn.setOnAction(e -> {
            setActiveNav(profileBtn);
            loadContent("/cashiepay/view/Profile.fxml");
        });

        logoutBtn.setOnAction(e -> {
            setActiveNav(logoutBtn);
            logout();
            setActiveNav(dashboardBtn);
        });
    }


    private void loadContent(String fxmlPath) {
        MainController mainController = getMainController();
        if (mainController != null) {
            mainController.loadContent(fxmlPath);
        } else {
            System.out.println("Error: MainController not found!");
        }
    }

    private MainController getMainController() {
        if (sidebar.getScene() != null && sidebar.getScene().getRoot().getUserData() instanceof MainController) {
            return (MainController) sidebar.getScene().getRoot().getUserData();
        }
        return null;
    }

    @FXML
    private void handleNavigation(ActionEvent event) {
        
    }
    
     private void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("Your current session will be closed.");

        ButtonType yesBtn = new ButtonType("Logout");
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());
        alert.getButtonTypes().setAll(yesBtn, cancelBtn);

        if (alert.showAndWait().orElse(cancelBtn) == yesBtn) {
            try {
                Stage stage = (Stage) sidebar.getScene().getWindow();
                stage.close();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/Login.fxml"));
                AnchorPane loginRoot = loader.load();

                Stage loginStage = new Stage();
                loginStage.setScene(new Scene(loginRoot));
                loginStage.setTitle("Login");
                loginStage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
     
     private void setActiveNav(Button activeBtn) {
        Button[] buttons = { dashboardBtn, collectionBtn, particularBtn, mfopapBtn, logoutBtn, profileBtn };

        for (Button btn : buttons) {
            btn.getStyleClass().remove("nav-item-active");
            if (!btn.getStyleClass().contains("nav-item")) {
                btn.getStyleClass().add("nav-item");
            }
        }

        // Add active style to clicked button
        if (!activeBtn.getStyleClass().contains("nav-item-active")) {
            activeBtn.getStyleClass().add("nav-item-active");
        }
    }

}
