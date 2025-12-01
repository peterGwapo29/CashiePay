package cashiepay.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CollectionController implements Initializable {

    @FXML
    private Button btnAddNew;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnAddNew.setOnAction(e -> openStudentPaymentModal());
    }

    private void openStudentPaymentModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/StudentPayment.fxml"));
            Parent root = loader.load();

            Stage modal = new Stage();
            modal.setTitle("Student Payment Transaction");
            modal.setScene(new Scene(root));
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setResizable(false);
            modal.showAndWait();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
