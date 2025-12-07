package cashiepay.controller;

import cashiepay.DAO.EditMfoPapController;
import cashiepay.model.DBConnection;
import cashiepay.model.MfoPap;
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

public class Mfo_papController implements Initializable {

    @FXML private Button newMfoPapBtn;

    @FXML private TableView<MfoPap> mfoPapTable;
    @FXML private TableColumn<MfoPap, Number> colId;
    @FXML private TableColumn<MfoPap, String> colName;
    @FXML private TableColumn<MfoPap, String> colCreatedAt;
    @FXML private TableColumn<MfoPap, String> colUpdatedAt;
    @FXML private TableColumn<MfoPap, String> colStatus;
    @FXML private TableColumn<MfoPap, Void> colAction;

    @FXML private ComboBox<String> showStatus;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(data -> data.getValue().idProperty());
        colName.setCellValueFactory(data -> data.getValue().mfoPapNameProperty());
        colCreatedAt.setCellValueFactory(data -> data.getValue().createdAtProperty());
        colUpdatedAt.setCellValueFactory(data -> data.getValue().updatedAtProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());

        showStatus.getItems().addAll("All", "Active", "Inactive");
        showStatus.setValue("Active");
        showStatus.setOnAction(this::filterStatusChanged);

        loadMfoPapData();
        addActionButtons();
    }

    @FXML
    private void newMfoPapBtnAction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/AddNewMfoPap.fxml"));
        Parent root = loader.load();

        Stage modal = new Stage();
        modal.setTitle("Register MFO/PAP");
        modal.setScene(new Scene(root));
        modal.initModality(Modality.WINDOW_MODAL);
        modal.initOwner(newMfoPapBtn.getScene().getWindow());
        modal.setResizable(false);
        modal.centerOnScreen();

        modal.showAndWait();
        loadMfoPapData();
    }

    private void loadMfoPapData() {
        ObservableList<MfoPap> list = FXCollections.observableArrayList();
        String selected = showStatus.getValue();
        String sql;

        switch (selected) {
            case "Active":
                sql = "SELECT * FROM mfo_pap WHERE status = 'Active'";
                break;
            case "Inactive":
                sql = "SELECT * FROM mfo_pap WHERE status = 'Inactive'";
                break;
            default:
                sql = "SELECT * FROM mfo_pap";
                break;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new MfoPap(
                        rs.getInt("id"),
                        rs.getString("mfo_pap_name"),
                        rs.getString("created_at"),
                        rs.getString("updated_at"),
                        rs.getString("status")
                ));
            }

            mfoPapTable.setItems(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addActionButtons() {
        colAction.setCellFactory(param -> new TableCell<MfoPap, Void>() {

            private final Button editBtn = new Button("Edit");
            private final Button actionBtn = new Button();

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

                editBtn.setOnAction(event -> {
                    MfoPap mfoPap = getTableView().getItems().get(getIndex());
                    openEditModal(mfoPap);
                });

                actionBtn.setOnAction(event -> {
                    MfoPap mfoPap = getTableView().getItems().get(getIndex());
                    if ("Active".equals(mfoPap.getStatus())) {
                        setInactive(mfoPap);
                    } else {
                        restoreMfoPap(mfoPap);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    MfoPap mfoPap = getTableView().getItems().get(getIndex());

                    if ("Active".equals(mfoPap.getStatus())) {
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

    private void openEditModal(MfoPap mfoPap) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/EditMfoPap.fxml"));
            Parent root = loader.load();

            EditMfoPapController controller = loader.getController();
            controller.setMfoPap(mfoPap);

            Stage modal = new Stage();
            modal.setTitle("Edit MFO/PAP");
            modal.setScene(new Scene(root));
            modal.initModality(Modality.WINDOW_MODAL);
            modal.initOwner(mfoPapTable.getScene().getWindow());
            modal.setResizable(false);
            modal.centerOnScreen();

            modal.showAndWait();
            loadMfoPapData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterStatusChanged(ActionEvent event) {
        loadMfoPapData();
    }

    private void setInactive(MfoPap mfoPap) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deactivation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to set this MFO/PAP as Inactive?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "UPDATE mfo_pap SET status = 'Inactive', updated_at = NOW() WHERE id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, mfoPap.getId());
                stmt.executeUpdate();
                loadMfoPapData();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void restoreMfoPap(MfoPap mfoPap) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Restore");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to restore this MFO/PAP to Active?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "UPDATE mfo_pap SET status = 'Active', updated_at = NOW() WHERE id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, mfoPap.getId());
                stmt.executeUpdate();
                loadMfoPapData();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
