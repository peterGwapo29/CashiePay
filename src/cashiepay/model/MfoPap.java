package cashiepay.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class MfoPap {

    private final SimpleIntegerProperty id;
    private final SimpleStringProperty mfoPapName;
    private final SimpleStringProperty createdAt;
    private final SimpleStringProperty updatedAt;
    private final SimpleStringProperty status;

    public MfoPap(int id, String mfoPapName, String createdAt, String updatedAt, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.mfoPapName = new SimpleStringProperty(mfoPapName);
        this.createdAt = new SimpleStringProperty(createdAt);
        this.updatedAt = new SimpleStringProperty(updatedAt);
        this.status = new SimpleStringProperty(status);
    }

    // getters
    public int getId() { return id.get(); }
    public String getMfoPapName() { return mfoPapName.get(); }
    public String getCreatedAt() { return createdAt.get(); }
    public String getUpdatedAt() { return updatedAt.get(); }
    public String getStatus() { return status.get(); }

    // properties
    public SimpleIntegerProperty idProperty() { return id; }
    public SimpleStringProperty mfoPapNameProperty() { return mfoPapName; }
    public SimpleStringProperty createdAtProperty() { return createdAt; }
    public SimpleStringProperty updatedAtProperty() { return updatedAt; }
    public SimpleStringProperty statusProperty() { return status; }

    // setters
    public void setMfoPapName(String name) { this.mfoPapName.set(name); }
    public void setUpdatedAt(String updatedAt) { this.updatedAt.set(updatedAt); }
    public void setStatus(String status) { this.status.set(status); }
}
