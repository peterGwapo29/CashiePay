package cashiepay.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Semester {

    private final SimpleIntegerProperty id;
    private final SimpleStringProperty semesterName;
    private final SimpleStringProperty schoolYear;
    private final SimpleStringProperty createdAt;
    private final SimpleStringProperty updatedAt;
    private final SimpleStringProperty status;

    public Semester(int id,
                    String semesterName,
                    String schoolYear,
                    String createdAt,
                    String updatedAt,
                    String status) {
        this.id = new SimpleIntegerProperty(id);
        this.semesterName = new SimpleStringProperty(semesterName);
        this.schoolYear = new SimpleStringProperty(schoolYear);
        this.createdAt = new SimpleStringProperty(createdAt);
        this.updatedAt = new SimpleStringProperty(updatedAt);
        this.status = new SimpleStringProperty(status);
    }

    // Getters
    public int getId() { return id.get(); }
    public String getSemesterName() { return semesterName.get(); }
    public String getSchoolYear() { return schoolYear.get(); }
    public String getCreatedAt() { return createdAt.get(); }
    public String getUpdatedAt() { return updatedAt.get(); }
    public String getStatus() { return status.get(); }

    // Property getters
    public SimpleIntegerProperty idProperty() { return id; }
    public SimpleStringProperty semesterNameProperty() { return semesterName; }
    public SimpleStringProperty schoolYearProperty() { return schoolYear; }
    public SimpleStringProperty createdAtProperty() { return createdAt; }
    public SimpleStringProperty updatedAtProperty() { return updatedAt; }
    public SimpleStringProperty statusProperty() { return status; }

    // Setters
    public void setSemesterName(String name) { this.semesterName.set(name); }
    public void setSchoolYear(String sy) { this.schoolYear.set(sy); }
    public void setUpdatedAt(String updatedAt) { this.updatedAt.set(updatedAt); }
    public void setStatus(String status) { this.status.set(status); }
}
