package cashiepay.controller;

import cashiepay.DAO.EditAccountController;
import cashiepay.DAO.AddNewAccountController;
import cashiepay.model.Account;
import cashiepay.model.DBConnection;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AccountController implements Initializable {

    @FXML private Button newAccountBtn;

    @FXML private TableView<Account> accountTable;
    @FXML private TableColumn<Account, Number> colId;
    @FXML private TableColumn<Account, String> colFund;
    @FXML private TableColumn<Account, String> colAccountName;
    @FXML private TableColumn<Account, String> colCreatedAt;
    @FXML private TableColumn<Account, String> colUpdatedAt;
    @FXML private TableColumn<Account, String> colStatus;
    @FXML private TableColumn<Account, Void> colAction;

    @FXML private ComboBox<String> filterStatus;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        accountTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colId.setCellValueFactory(data -> data.getValue().idProperty());
        colFund.setCellValueFactory(data -> data.getValue().fundNameProperty());
        colAccountName.setCellValueFactory(data -> data.getValue().accountNameProperty());
        colCreatedAt.setCellValueFactory(data -> data.getValue().createdAtProperty());
        colUpdatedAt.setCellValueFactory(data -> data.getValue().updatedAtProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());

        filterStatus.getItems().addAll("All", "Active", "Inactive");
        filterStatus.setValue("Active");

        loadAccountData();
        addActionButtons();
    }

    @FXML
    private void newAccountBtnAction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/AddNewAccount.fxml"));
        Parent root = loader.load();

        Stage modal = new Stage();
        modal.setTitle("Register Account");
        modal.setScene(new Scene(root));
        modal.initModality(Modality.WINDOW_MODAL);
        modal.initOwner(newAccountBtn.getScene().getWindow());
        modal.setResizable(false);
        modal.centerOnScreen();

        modal.showAndWait();
        loadAccountData();
    }

    private void loadAccountData() {
        ObservableList<Account> list = FXCollections.observableArrayList();
        String selected = filterStatus.getValue();
        StringBuilder sql = new StringBuilder(
                "SELECT a.id, a.fund_id, f.fund_name, a.account_name, " +
                "a.created_at, a.updated_at, a.status " +
                "FROM account a " +
                "JOIN fund f ON a.fund_id = f.id "
        );

        switch (selected) {
            case "Active":
                sql.append("WHERE a.status = 'Active'");
                break;
            case "Inactive":
                sql.append("WHERE a.status = 'Inactive'");
                break;
            default:
                // all – no extra WHERE
                break;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString());
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Account(
                        rs.getInt("id"),
                        rs.getInt("fund_id"),
                        rs.getString("fund_name"),
                        rs.getString("account_name"),
                        rs.getString("created_at"),
                        rs.getString("updated_at"),
                        rs.getString("status")
                ));
            }

            accountTable.setItems(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addActionButtons() {
        colAction.setCellFactory(param -> new TableCell<Account, Void>() {

            private final Button editBtn = new Button("Edit");
            private final Button actionBtn = new Button();

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

                editBtn.setOnAction(event -> {
                    Account acc = getTableView().getItems().get(getIndex());
                    openEditModal(acc);
                });

                actionBtn.setOnAction(event -> {
                    Account acc = getTableView().getItems().get(getIndex());
                    if ("Active".equals(acc.getStatus())) {
                        setAccountInactive(acc);
                    } else {
                        restoreAccount(acc);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Account acc = getTableView().getItems().get(getIndex());

                    if ("Active".equals(acc.getStatus())) {
                        actionBtn.setText("Delete");
                        actionBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                    } else {
                        actionBtn.setText("Restore");
                        actionBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                    }

                    HBox box = new HBox(5, editBtn, actionBtn);
                    setGraphic(box);
                }
            }
        });
    }

    private void openEditModal(Account account) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/EditAccount.fxml"));
            Parent root = loader.load();

            EditAccountController controller = loader.getController();
            controller.setAccount(account);

            Stage modal = new Stage();
            modal.setTitle("Edit Account");
            modal.setScene(new Scene(root));
            modal.initModality(Modality.WINDOW_MODAL);
            modal.initOwner(accountTable.getScene().getWindow());
            modal.setResizable(false);
            modal.centerOnScreen();

            modal.showAndWait();
            loadAccountData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void filterStatusChanged(ActionEvent event) {
        loadAccountData();
    }

    private void setAccountInactive(Account account) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deactivation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to set this account as Inactive?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "UPDATE account SET status = 'Inactive', updated_at = NOW() WHERE id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, account.getId());
                stmt.executeUpdate();
                loadAccountData();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void restoreAccount(Account account) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Restore");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to restore this account to Active?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "UPDATE account SET status = 'Active', updated_at = NOW() WHERE id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, account.getId());
                stmt.executeUpdate();
                loadAccountData();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
