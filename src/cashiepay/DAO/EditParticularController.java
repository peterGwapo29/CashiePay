package cashiepay.DAO;

import cashiepay.model.DBConnection;
import cashiepay.model.Particular;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class EditParticularController {

    @FXML private TextField nameField;
    @FXML private TextField amountField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private Particular particular;

    public void setParticular(Particular particular) {
        this.particular = particular;
        loadParticularData();
    }

    private void loadParticularData() {
        if (particular != null) {
            nameField.setText(particular.getParticularName());
            amountField.setText(particular.getAmount());
        }
    }

    @FXML
    private void saveAction(ActionEvent event) {
        String newName = nameField.getText().trim();
        String newAmount = amountField.getText().trim();

        if (newName.isEmpty() || newAmount.isEmpty()) {
            System.out.println("Please fill all fields!");
            return;
        }

        String sql = "UPDATE particular SET particular_name = ?, amount = ?, updated_at = NOW() WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newName);
            stmt.setString(2, newAmount);
            stmt.setInt(3, particular.getId());

            stmt.executeUpdate();
            ((Stage) saveBtn.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelAction(ActionEvent event) {
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }
}
