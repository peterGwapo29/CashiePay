package cashiepay.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Student {

    private final SimpleIntegerProperty id;
    private final SimpleStringProperty studentId;
    private final SimpleStringProperty firstName;
    private final SimpleStringProperty lastName;
    private final SimpleStringProperty middleName;
    private final SimpleStringProperty suffix;
    private final SimpleStringProperty status;
    private final SimpleStringProperty createdAt;
    private final SimpleStringProperty updatedAt;

    public Student(
            int id,
            String studentId,
            String firstName,
            String lastName,
            String middleName,
            String suffix,
            String status,
            String createdAt,
            String updatedAt
    ) {
        this.id = new SimpleIntegerProperty(id);
        this.studentId = new SimpleStringProperty(studentId);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.middleName = new SimpleStringProperty(middleName);
        this.suffix = new SimpleStringProperty(suffix);
        this.status = new SimpleStringProperty(status);
        this.createdAt = new SimpleStringProperty(createdAt);
        this.updatedAt = new SimpleStringProperty(updatedAt);
    }

    // getters
    public int getId() { return id.get(); }
    public String getStudentId() { return studentId.get(); }
    public String getFirstName() { return firstName.get(); }
    public String getLastName() { return lastName.get(); }
    public String getMiddleName() { return middleName.get(); }
    public String getSuffix() { return suffix.get(); }
    public String getStatus() { return status.get(); }
    public String getCreatedAt() { return createdAt.get(); }
    public String getUpdatedAt() { return updatedAt.get(); }

    // properties
    public SimpleIntegerProperty idProperty() { return id; }
    public SimpleStringProperty studentIdProperty() { return studentId; }
    public SimpleStringProperty firstNameProperty() { return firstName; }
    public SimpleStringProperty lastNameProperty() { return lastName; }
    public SimpleStringProperty middleNameProperty() { return middleName; }
    public SimpleStringProperty suffixProperty() { return suffix; }
    public SimpleStringProperty statusProperty() { return status; }
    public SimpleStringProperty createdAtProperty() { return createdAt; }
    public SimpleStringProperty updatedAtProperty() { return updatedAt; }

    // setters (for editing)
    public void setStudentId(String value) { this.studentId.set(value); }
    public void setFirstName(String value) { this.firstName.set(value); }
    public void setLastName(String value) { this.lastName.set(value); }
    public void setMiddleName(String value) { this.middleName.set(value); }
    public void setSuffix(String value) { this.suffix.set(value); }
    public void setStatus(String value) { this.status.set(value); }
    public void setUpdatedAt(String value) { this.updatedAt.set(value); }
}
