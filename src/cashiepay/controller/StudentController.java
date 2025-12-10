package cashiepay.controller;

import cashiepay.io.StudentExcelImporter;
import cashiepay.io.StudentExporter;
import cashiepay.model.DBConnection;
import cashiepay.model.Student;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class StudentController implements Initializable {

    @FXML private Button newStudentBtn;

    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, Number> colId;
    @FXML private TableColumn<Student, String> colStudentId;
    @FXML private TableColumn<Student, String> colFirstName;
    @FXML private TableColumn<Student, String> colLastName;
    @FXML private TableColumn<Student, String> colMiddleName;
    @FXML private TableColumn<Student, String> colSuffix;
    @FXML private TableColumn<Student, String> colStatus;
    @FXML private TableColumn<Student, String> colCreatedAt;
    @FXML private TableColumn<Student, String> colUpdatedAt;
    @FXML private TableColumn<Student, Void>   colAction;

    @FXML private ComboBox<String> filterStatus;
    @FXML
    private Button btnImportStudent;
    @FXML
    private Button clearBtn;
    @FXML
    private TextField txtSearchStudent;
    @FXML
    private Button btnExportStudent;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        btnImportStudent.setOnAction(e -> handleImportStudents());

        studentTable.setColumnResizePolicy(studentTable.CONSTRAINED_RESIZE_POLICY);

        colId.setCellValueFactory(data -> data.getValue().idProperty());
        colStudentId.setCellValueFactory(data -> data.getValue().studentIdProperty());
        colFirstName.setCellValueFactory(data -> data.getValue().firstNameProperty());
        colLastName.setCellValueFactory(data -> data.getValue().lastNameProperty());
        colMiddleName.setCellValueFactory(data -> data.getValue().middleNameProperty());
        colSuffix.setCellValueFactory(data -> data.getValue().suffixProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colCreatedAt.setCellValueFactory(data -> data.getValue().createdAtProperty());
        colUpdatedAt.setCellValueFactory(data -> data.getValue().updatedAtProperty());

        filterStatus.getItems().addAll("All", "Active", "Inactive");
        filterStatus.setValue("Active");
        
        setupSearchFeature();
        loadStudentData();
        addActionButtons();
        
    }

    @FXML
    private void newStudentBtnAction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/AddNewStudent.fxml"));
        Parent root = loader.load();

        Stage modal = new Stage();
        modal.setTitle("Register Student");
        modal.setScene(new Scene(root));
        modal.initModality(Modality.WINDOW_MODAL);
        modal.initOwner(newStudentBtn.getScene().getWindow());
        modal.setResizable(false);
        modal.centerOnScreen();

        modal.showAndWait();
        loadStudentData();
    }

    @FXML
    private void filterStatusChanged(ActionEvent event) {
        loadStudentData();
    }

//    private void loadStudentData() {
//        ObservableList<Student> list = FXCollections.observableArrayList();
//        String selected = filterStatus.getValue();
//        String sql;
//
//        switch (selected) {
//            case "Active":
//                sql = "SELECT * FROM student WHERE status = 'Active'";
//                break;
//            case "Inactive":
//                sql = "SELECT * FROM student WHERE status = 'Inactive'";
//                break;
//            default:
//                sql = "SELECT * FROM student";
//                break;
//        }
//
//        try (Connection conn = DBConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql);
//             ResultSet rs = stmt.executeQuery()) {
//
//            while (rs.next()) {
//                list.add(new Student(
//                        rs.getInt("id"),
//                        rs.getString("student_id"),
//                        rs.getString("first_name"),
//                        rs.getString("last_name"),
//                        rs.getString("middle_name"),
//                        rs.getString("suffix"),
//                        rs.getString("status"),
//                        rs.getString("created_at"),
//                        rs.getString("updated_at")
//                ));
//            }
//
//            studentTable.setItems(list);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    
    private void loadStudentData() {
    ObservableList<Student> list = FXCollections.observableArrayList();
    String selected = filterStatus.getValue();

    // read search text
    String search = (txtSearchStudent != null && txtSearchStudent.getText() != null)
            ? txtSearchStudent.getText().trim()
            : "";

    StringBuilder sql = new StringBuilder("SELECT * FROM student WHERE 1=1 ");

    // status filter
    if ("Active".equalsIgnoreCase(selected)) {
        sql.append(" AND status = 'Active'");
    } else if ("Inactive".equalsIgnoreCase(selected)) {
        sql.append(" AND status = 'Inactive'");
    }

    // search filter (by student_id, like Collection)
    if (!search.isEmpty()) {
        sql.append(" AND student_id LIKE '%").append(search).append("%'");
    }

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql.toString());
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            list.add(new Student(
                    rs.getInt("id"),
                    rs.getString("student_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("middle_name"),
                    rs.getString("suffix"),
                    rs.getString("status"),
                    rs.getString("created_at"),
                    rs.getString("updated_at")
            ));
        }

        studentTable.setItems(list);

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    
    
    private void addActionButtons() {
        colAction.setCellFactory(param -> new TableCell<Student, Void>() {

            private final Button editBtn = new Button("Edit");
            private final Button actionBtn = new Button();

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

                editBtn.setOnAction(event -> {
                    Student student = getTableView().getItems().get(getIndex());
                    openEditModal(student);
                });

                actionBtn.setOnAction(event -> {
                    Student student = getTableView().getItems().get(getIndex());
                    if ("Active".equalsIgnoreCase(student.getStatus())) {
                        setStudentInactive(student);
                    } else {
                        restoreStudent(student);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Student student = getTableView().getItems().get(getIndex());

                    if ("Active".equalsIgnoreCase(student.getStatus())) {
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

    private void openEditModal(Student student) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashiepay/view/EditStudent.fxml"));
            Parent root = loader.load();

            cashiepay.DAO.EditStudentController controller = loader.getController();
            controller.setStudent(student);

            Stage modal = new Stage();
            modal.setTitle("Edit Student");
            modal.setScene(new Scene(root));
            modal.initModality(Modality.WINDOW_MODAL);
            modal.initOwner(studentTable.getScene().getWindow());
            modal.setResizable(false);
            modal.centerOnScreen();

            modal.showAndWait();
            loadStudentData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setStudentInactive(Student student) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deactivation");
        alert.setHeaderText(null);
        alert.setContentText("Set this student as Inactive?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "UPDATE student SET status = 'Inactive', updated_at = NOW() WHERE id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, student.getId());
                stmt.executeUpdate();
                loadStudentData();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void restoreStudent(Student student) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Restore");
        alert.setHeaderText(null);
        alert.setContentText("Restore this student to Active?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "UPDATE student SET status = 'Active', updated_at = NOW() WHERE id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, student.getId());
                stmt.executeUpdate();
                loadStudentData();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void handleImportStudents() {
        try (Connection conn = DBConnection.getConnection()) {
            boolean imported = StudentExcelImporter.importStudents(conn);

            if (imported) {
                loadStudentData();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Import Complete");
                alert.setHeaderText(null);
                alert.setContentText("Student data imported successfully.");
                alert.showAndWait();
            } else {
                 Alert alert = new Alert(Alert.AlertType.INFORMATION);
                 alert.setHeaderText(null);
                 alert.setContentText("No students were imported.");
                 alert.showAndWait();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Import Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to import students: " + ex.getMessage());
            alert.showAndWait();
        }
    }
    
    private void setupSearchFeature() {
        PauseTransition debounce = new PauseTransition(Duration.seconds(2));

        txtSearchStudent.textProperty().addListener((obs, oldText, newText) -> {
            debounce.stop();
            debounce.setOnFinished(event -> loadStudentData()); // 👈 refresh after delay
            debounce.play();
        });
    }


    @FXML
    private void ClearSearchBar(ActionEvent event) {
        if (event.getSource() == clearBtn) {
            txtSearchStudent.setText("");
            loadStudentData();
        }
    }

    @FXML
    private void exportStudentAction(ActionEvent event) {
        StudentExporter.exportStudents();
    }

}
