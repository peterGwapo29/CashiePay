package cashiepay.controller;

import cashiepay.model.DBConnection;
import cashiepay.model.Particular;
import cashiepay.DAO.EditParticularController;
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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ParticularController implements Initializable {

    @FXML private Button newParticularBtn;

    @FXML private TableView<Particular> particularTable;
    @FXML private TableColumn<Particular, Number> colId;
    @FXML private TableColumn<Particular, String> colName;
    @FXML private TableColumn<Particular, String> colAmount;
    @FXML private TableColumn<Particular, String> colCreatedAt;
    @FXML private TableColumn<Particular, String> colUpdatedAt;
    @FXML private TableColumn<Particular, String> colStatus;
    @FXML private TableColumn<Particular, Void> colAction;
    @FXML private ComboBox<String> filterStatus;
    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(data -> data.getValue().idProperty());
        colName.setCellValueFactory(data -> data.getValue().particularNameProperty());
        colAmount.setCellValueFactory(data -> data.getValue().amountProperty());
        colCreatedAt.setCellValueFactory(data -> data.getValue().createdAtProperty());
        colUpdatedAt.setCellValueFactory(data -> data.getValue().updatedAtProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        
        filterStatus.getItems().addAll("All", "Active", "Inactive");
        filterStatus.setValue("Active"); 

        loadParticularData();
        addActionButtons();
    }

    @FXML
    private void newParticularBtnAction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/AddNewParticular.fxml"));
        Parent root = loader.load();

        Stage modal = new Stage();
        modal.setTitle("Add New Particular");
        modal.setScene(new Scene(root));
        modal.initModality(Modality.WINDOW_MODAL);
        modal.initOwner(newParticularBtn.getScene().getWindow());
        modal.setResizable(false);
        modal.centerOnScreen();

        modal.showAndWait();
        loadParticularData();
    }

    private void loadParticularData() {
        ObservableList<Particular> list = FXCollections.observableArrayList();
        String selected = (String) filterStatus.getValue();
        String sql;

        switch (selected) {
            case "Active":
                sql = "SELECT * FROM particular WHERE status = 'Active'";
                break;
            case "Inactive":
                sql = "SELECT * FROM particular WHERE status = 'Inactive'";
                break;
            default:
                sql = "SELECT * FROM particular";
                break;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Particular(
                        rs.getInt("id"),
                        rs.getString("particular_name"),
                        rs.getString("amount"),
                        rs.getString("created_at"),
                        rs.getString("updated_at"),
                        rs.getString("status")
                ));
            }

            particularTable.setItems(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void addActionButtons() {
        colAction.setCellFactory(param -> new TableCell<Particular, Void>() {

            private final Button editBtn = new Button("Edit");
            private final Button actionBtn = new Button();

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

                editBtn.setOnAction(event -> {
                    Particular particular = getTableView().getItems().get(getIndex());
                    openEditModal(particular);
                });

                actionBtn.setOnAction(event -> {
                    Particular particular = getTableView().getItems().get(getIndex());
                    if ("Active".equals(particular.getStatus())) {
                        setParticularInactive(particular);
                    } else {
                        restoreParticular(particular);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Particular particular = getTableView().getItems().get(getIndex());

                    if ("Active".equals(particular.getStatus())) {
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


    private void openEditModal(Particular particular) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/EditParticular.fxml"));
            Parent root = loader.load();

            EditParticularController controller = loader.getController();
            controller.setParticular(particular);

            Stage modal = new Stage();
            modal.setTitle("Edit Particular");
            modal.setScene(new Scene(root));
            modal.initModality(Modality.WINDOW_MODAL);
            modal.initOwner(particularTable.getScene().getWindow());
            modal.setResizable(false);
            modal.centerOnScreen();

            modal.showAndWait();
            loadParticularData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteParticular(Particular particular) {
        // Confirmation dialog
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deactivation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to set this particular as Inactive?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "UPDATE particular SET status = 'Inactive', updated_at = NOW() WHERE id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, particular.getId());
                stmt.executeUpdate();
                loadParticularData();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void filterStatusChanged(ActionEvent event) {
        loadParticularData();
    }
    
    private void setParticularInactive(Particular particular) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deactivation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to set this particular as Inactive?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "UPDATE particular SET status = 'Inactive', updated_at = NOW() WHERE id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, particular.getId());
                stmt.executeUpdate();
                loadParticularData();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void restoreParticular(Particular particular) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Restore");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to restore this particular to Active?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "UPDATE particular SET status = 'Active', updated_at = NOW() WHERE id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, particular.getId());
                stmt.executeUpdate();
                loadParticularData();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
