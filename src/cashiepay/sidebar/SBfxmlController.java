package cashiepay.sidebar;

import cashiepay.controller.MainController;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class SBfxmlController {

    @FXML
    private AnchorPane sidebar;

    @FXML
    private Button dashboardBtn, collectionBtn, particularBtn, mfopapBtn, logoutBtn, profileBtn;
    
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
            logout();
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
        alert.setTitle("Logout");
        alert.setHeaderText("Logout from CASHIEPAY?");
        alert.setContentText("Your current session will be closed.");

        // Correct ButtonType creation
        ButtonType logoutBtnType = new ButtonType("Logout", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtnType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(cancelBtnType, logoutBtnType);

        // Apply CSS
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/cashiepay/sidebar/sbfxml.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("logout-dialog");

        // Style the buttons when dialog shows
        alert.setOnShown(e -> {
            Button logoutButton =
                (Button) alert.getDialogPane().lookupButton(logoutBtnType);
            Button cancelButton =
                (Button) alert.getDialogPane().lookupButton(cancelBtnType);

            if (logoutButton != null) logoutButton.getStyleClass().add("danger-button");
            if (cancelButton != null) cancelButton.getStyleClass().add("outline-button");
        });

        // Handle result
        alert.showAndWait().ifPresent(result -> {
            if (result == logoutBtnType) {
                try {
                    Stage stage = (Stage) sidebar.getScene().getWindow();
                    stage.close();

                    FXMLLoader loader =
                        new FXMLLoader(getClass().getResource("/cashiepay/view/Login.fxml"));
                    AnchorPane loginRoot = loader.load();

                    Stage loginStage = new Stage();
                    loginStage.setScene(new Scene(loginRoot));
                    loginStage.setTitle("Login");
                    loginStage.show();
                    setActiveNav(dashboardBtn);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
     
     private void setActiveNav(Button activeBtn) {
        Button[] buttons = { dashboardBtn, collectionBtn, particularBtn, mfopapBtn, profileBtn };

        for (Button btn : buttons) {
            btn.getStyleClass().remove("nav-item-active");
            if (!btn.getStyleClass().contains("nav-item")) {
                btn.getStyleClass().add("nav-item");
            }
        }

        if (!activeBtn.getStyleClass().contains("nav-item-active")) {
            activeBtn.getStyleClass().add("nav-item-active");
        }
    }
}
