package cashiepay.controller;

import cashiepay.DAO.AddNewAdminController;
import cashiepay.DAO.EditAdminController;
import cashiepay.model.Admin;
import cashiepay.model.DBConnection;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AdminController implements Initializable {

    @FXML private TableView<Admin> adminTable;
    @FXML private TableColumn<Admin, Number> colId;
    @FXML private TableColumn<Admin, String> colAdminName, colEmail, colUsername, colStatus, colCreatedAt, colUpdatedAt;
    @FXML private TableColumn<Admin, Void> colAction;

    @FXML private ComboBox<String> filterStatus;
    @FXML private TextField txtSearchAdmin;
    @FXML private Button newAdminBtn;

    private final ObservableList<Admin> adminList = FXCollections.observableArrayList();
    @FXML
    private Button clearBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupFilter();
        setupSearch();
        loadAdmins();

        newAdminBtn.setOnAction(e -> openAddAdminModal());
    }

    private void setupTable() {
        colId.setCellValueFactory(data -> data.getValue().idProperty());
        colAdminName.setCellValueFactory(data -> data.getValue().adminNameProperty());
        colEmail.setCellValueFactory(data -> data.getValue().emailAddressProperty());
        colUsername.setCellValueFactory(data -> data.getValue().usernameProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colCreatedAt.setCellValueFactory(data -> data.getValue().createdAtProperty());
        colUpdatedAt.setCellValueFactory(data -> data.getValue().updatedAtProperty());

        colAction.setCellFactory(col -> new TableCell<Admin, Void>() {

            private final Button editBtn = new Button("Edit");
            private final Button actionBtn = new Button();

            {
                // Same look as Particular page
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

                editBtn.setOnAction(e -> {
                    Admin admin = getTableView().getItems().get(getIndex());
                    openEditAdminModal(admin);
                });

                actionBtn.setOnAction(e -> {
                    Admin admin = getTableView().getItems().get(getIndex());
                    toggleStatus(admin);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                Admin admin = getTableView().getItems().get(getIndex());

                if ("Active".equalsIgnoreCase(admin.getStatus())) {
                    actionBtn.setText("Deactivate");
                    actionBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                } else {
                    actionBtn.setText("Activate");
                    actionBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                }

                HBox box = new HBox(5, editBtn, actionBtn);
                box.setStyle("-fx-alignment: center;");
                setGraphic(box);
                
                editBtn.setPrefWidth(70);
                actionBtn.setPrefWidth(90);
                editBtn.setPrefHeight(28);
                actionBtn.setPrefHeight(28);
            }
        });
    }

    private void setupFilter() {
        filterStatus.getItems().setAll("All", "Active", "Inactive");
        filterStatus.setValue("Active");
    }

    private void setupSearch() {
        txtSearchAdmin.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    @FXML
    private void filterStatusChanged() {
        applyFilters();
    }

    @FXML
    private void clearSearch() {
        txtSearchAdmin.clear();
        filterStatus.setValue("All");
        applyFilters();
    }

    private void loadAdmins() {
        adminList.clear();

        String sql = "SELECT id, admin_name, email_address, username, password, status, " +
                     "DATE_FORMAT(created_at, '%Y-%m-%d %H:%i') created_at, " +
                     "DATE_FORMAT(updated_at, '%Y-%m-%d %H:%i') updated_at " +
                     "FROM admin ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                adminList.add(new Admin(
                        rs.getInt("id"),
                        rs.getString("admin_name"),
                        rs.getString("email_address"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                ));
            }

            applyFilters();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        String status = filterStatus.getValue();
        String search = txtSearchAdmin.getText() == null ? "" : txtSearchAdmin.getText().trim().toLowerCase();

        ObservableList<Admin> filtered = adminList.filtered(a -> {
            boolean matchStatus = status == null || status.equals("All") || a.getStatus().equalsIgnoreCase(status);
            boolean matchSearch = search.isEmpty() || a.getUsername().toLowerCase().contains(search);
            return matchStatus && matchSearch;
        });

        adminTable.setItems(filtered);
    }

    private void openAddAdminModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/AddNewAdmin.fxml"));
            AnchorPane root = loader.load();

            AddNewAdminController controller = loader.getController();
            controller.setOnSaved(this::loadAdmins);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Admin");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEditAdminModal(Admin admin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/EditAdmin.fxml"));
            AnchorPane root = loader.load();

            EditAdminController controller = loader.getController();
            controller.setAdmin(admin);
            controller.setOnSaved(this::loadAdmins);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Admin");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleStatus(Admin admin) {

        boolean isActive = "Active".equalsIgnoreCase(admin.getStatus());
        String newStatus = isActive ? "Inactive" : "Active";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Action");
        alert.setHeaderText(isActive ? "Deactivate Admin" : "Activate Admin");
        alert.setContentText(
                isActive
                ? "Are you sure you want to deactivate this admin?\n\nThey will NOT be able to log in."
                : "Are you sure you want to activate this admin?\n\nThey will be able to log in."
        );

        ButtonType confirmBtn = new ButtonType(
                isActive ? "Deactivate" : "Activate",
                ButtonBar.ButtonData.OK_DONE
        );
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(cancelBtn, confirmBtn);

        alert.showAndWait().ifPresent(result -> {
            if (result == confirmBtn) {
                updateAdminStatus(admin.getId(), newStatus);
            }
        });
    }
    
    private void updateAdminStatus(int adminId, String newStatus) {

        String sql = "UPDATE admin SET status = ?, updated_at = NOW() WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, adminId);
            ps.executeUpdate();

            loadAdmins();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to update admin status.");
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void newAdminBtnAction() {
        openAddAdminModal();
    }
}
