package cashiepay.DAO;

import cashiepay.model.DBConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddNewAccountController implements Initializable {

    @FXML private ComboBox<String> fundCombo;
    @FXML private TextField accountNameField;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    // map fund name -> fund id
    private final Map<String, Integer> fundMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadFunds();

        saveButton.setOnAction(e -> saveAccount());
        cancelButton.setOnAction(e -> closeModal());
    }

    private void loadFunds() {
        String sql = "SELECT id, fund_name FROM fund WHERE status = 'Active'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("fund_name");
                fundMap.put(name, id);
                fundCombo.getItems().add(name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeModal() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private void saveAccount() {
        String fundName = fundCombo.getValue();
        String accountName = accountNameField.getText().trim();

        if (fundName == null || fundName.trim().isEmpty() || accountName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Please select a fund and enter an account name.");
            return;
        }

        Integer fundId = fundMap.get(fundName);
        if (fundId == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Invalid fund selected.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            // duplicate check
            String checkSql = "SELECT COUNT(*) FROM account WHERE account_name = ? AND fund_id = ?";
            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setString(1, accountName);
                check.setInt(2, fundId);
                ResultSet rs = check.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    showAlert(Alert.AlertType.ERROR, "Duplicate Entry",
                            "This account already exists for the selected fund.");
                    return;
                }
            }

            String sql = "INSERT INTO account (fund_id, account_name) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, fundId);
                stmt.setString(2, accountName);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Account saved successfully!");
                    closeModal();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Failed to save account: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
