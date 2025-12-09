package cashiepay.DAO;

import cashiepay.model.Account;
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

public class EditAccountController implements Initializable {

    @FXML private ComboBox<String> fundCombo;
    @FXML private TextField accountNameField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private Account account;

    // fund name -> id
    private final Map<String, Integer> fundMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadFunds();
    }

    public void setAccount(Account account) {
        this.account = account;
        loadAccountData();
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

    private void loadAccountData() {
        if (account == null) return;

        accountNameField.setText(account.getAccountName());

        String fundName = account.getFundName();
        if (fundName != null && !fundCombo.getItems().contains(fundName)) {
            fundCombo.getItems().add(fundName);
        }
        fundCombo.setValue(fundName);
    }

    @FXML
    private void saveAction() {
        if (account == null) return;

        String fundName = fundCombo.getValue();
        String accountName = accountNameField.getText().trim();

        if (fundName == null || fundName.trim().isEmpty() || accountName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Please select a fund and enter an account name.");
            return;
        }

        Integer fundId = fundMap.get(fundName);
        if (fundId == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Invalid fund selected.");
            return;
        }

        if (isDuplicate(accountName, fundId, account.getId())) {
            showAlert(Alert.AlertType.ERROR, "Duplicate Error",
                    "Account name already exists for this fund.");
            return;
        }

        String sql = "UPDATE account SET fund_id = ?, account_name = ?, " +
                     "updated_at = NOW() WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fundId);
            stmt.setString(2, accountName);
            stmt.setInt(3, account.getId());

            stmt.executeUpdate();

            account.setFundId(fundId);
            account.setFundName(fundName);
            account.setAccountName(accountName);

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Account updated successfully!");
            close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to update account.");
        }
    }

    private boolean isDuplicate(String name, int fundId, int currentId) {
        String sql = "SELECT COUNT(*) FROM account " +
                     "WHERE account_name = ? AND fund_id = ? AND id != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, fundId);
            stmt.setInt(3, currentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    private void cancelAction() {
        close();
    }

    private void close() {
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
