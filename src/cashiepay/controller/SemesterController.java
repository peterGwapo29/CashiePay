package cashiepay.controller;

import cashiepay.model.DBConnection;
import cashiepay.model.Semester;
import cashiepay.DAO.AddNewSemesterController;
import cashiepay.DAO.EditSemesterController;
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
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SemesterController implements Initializable {

    @FXML private Button newSemesterBtn;

    @FXML private TableView<Semester> semesterTable;
    @FXML private TableColumn<Semester, Number> colId;
    @FXML private TableColumn<Semester, String> colSemester;
    @FXML private TableColumn<Semester, String> colSchoolYear;
    @FXML private TableColumn<Semester, String> colCreatedAt;
    @FXML private TableColumn<Semester, String> colUpdatedAt;
    @FXML private TableColumn<Semester, String> colStatus;
    @FXML private TableColumn<Semester, Void> colAction;

    @FXML private ComboBox<String> filterStatus;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        semesterTable.setColumnResizePolicy(semesterTable.CONSTRAINED_RESIZE_POLICY);

        colId.setCellValueFactory(data -> data.getValue().idProperty());
        colSemester.setCellValueFactory(data -> data.getValue().semesterNameProperty());
        colSchoolYear.setCellValueFactory(data -> data.getValue().schoolYearProperty());
        colCreatedAt.setCellValueFactory(data -> data.getValue().createdAtProperty());
        colUpdatedAt.setCellValueFactory(data -> data.getValue().updatedAtProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());

        filterStatus.getItems().addAll("All", "Active", "Inactive");
        filterStatus.setValue("Active");

        loadSemesterData();
        addActionButtons();
    }

    @FXML
    private void newSemesterBtnAction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/AddNewSemester.fxml"));
        Parent root = loader.load();

        Stage modal = new Stage();
        modal.setTitle("Add New Semester");
        modal.setScene(new Scene(root));
        modal.initModality(Modality.WINDOW_MODAL);
        modal.initOwner(newSemesterBtn.getScene().getWindow());
        modal.setResizable(false);
        modal.centerOnScreen();

        modal.showAndWait();
        loadSemesterData();
    }

    private void loadSemesterData() {
        ObservableList<Semester> list = FXCollections.observableArrayList();
        String selected = filterStatus.getValue();
        String sql;

        switch (selected) {
            case "Active":
                sql = "SELECT * FROM semester WHERE status = 'Active'";
                break;
            case "Inactive":
                sql = "SELECT * FROM semester WHERE status = 'Inactive'";
                break;
            default:
                sql = "SELECT * FROM semester";
                break;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Semester(
                        rs.getInt("semester_id"),
                        rs.getString("academic_year"),
                        rs.getString("semester"),
                        rs.getString("created_at"),
                        rs.getString("updated_at"),
                        rs.getString("status")
                ));
            }

            semesterTable.setItems(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addActionButtons() {
        colAction.setCellFactory(param -> new TableCell<Semester, Void>() {

            private final Button editBtn = new Button("Edit");
            private final Button actionBtn = new Button();

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

                editBtn.setOnAction(event -> {
                    Semester sem = getTableView().getItems().get(getIndex());
                    openEditModal(sem);
                });

                actionBtn.setOnAction(event -> {
                    Semester sem = getTableView().getItems().get(getIndex());
                    if ("Active".equals(sem.getStatus())) {
                        setSemesterInactive(sem);
                    } else {
                        restoreSemester(sem);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Semester sem = getTableView().getItems().get(getIndex());

                    if ("Active".equals(sem.getStatus())) {
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

    private void openEditModal(Semester sem) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/EditSemester.fxml"));
            Parent root = loader.load();

            EditSemesterController controller = loader.getController();
            controller.setSemester(sem);

            Stage modal = new Stage();
            modal.setTitle("Edit Semester");
            modal.setScene(new Scene(root));
            modal.initModality(Modality.WINDOW_MODAL);
            modal.initOwner(semesterTable.getScene().getWindow());
            modal.setResizable(false);
            modal.centerOnScreen();

            modal.showAndWait();
            loadSemesterData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setSemesterInactive(Semester sem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deactivation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to set this semester as Inactive?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "UPDATE semester SET status = 'Inactive', updated_at = NOW() WHERE semester_id  = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, sem.getId());
                stmt.executeUpdate();
                loadSemesterData();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void restoreSemester(Semester sem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Restore");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to restore this semester to Active?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "UPDATE semester SET status = 'Active', updated_at = NOW() WHERE semester_id  = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, sem.getId());
                stmt.executeUpdate();
                loadSemesterData();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void filterStatusChanged(ActionEvent event) {
        loadSemesterData();
    }
}
