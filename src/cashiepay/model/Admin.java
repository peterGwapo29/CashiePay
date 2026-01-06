package cashiepay.model;

import javafx.beans.property.*;

public class Admin {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty adminName = new SimpleStringProperty();
    private final StringProperty emailAddress = new SimpleStringProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty createdAt = new SimpleStringProperty();
    private final StringProperty updatedAt = new SimpleStringProperty();

    public Admin(int id, String adminName, String email, String username,
                 String password, String status, String createdAt, String updatedAt) {
        this.id.set(id);
        this.adminName.set(adminName);
        this.emailAddress.set(email);
        this.username.set(username);
        this.password.set(password);
        this.status.set(status);
        this.createdAt.set(createdAt);
        this.updatedAt.set(updatedAt);
    }

    public int getId() { return id.get(); }
    public String getAdminName() { return adminName.get(); }
    public String getEmailAddress() { return emailAddress.get(); }
    public String getUsername() { return username.get(); }
    public String getPassword() { return password.get(); }
    public String getStatus() { return status.get(); }
    public String getCreatedAt() { return createdAt.get(); }
    public String getUpdatedAt() { return updatedAt.get(); }

    public IntegerProperty idProperty() { return id; }
    public StringProperty adminNameProperty() { return adminName; }
    public StringProperty emailAddressProperty() { return emailAddress; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty statusProperty() { return status; }
    public StringProperty createdAtProperty() { return createdAt; }
    public StringProperty updatedAtProperty() { return updatedAt; }

    public void setAdminName(String v){ adminName.set(v); }
    public void setEmailAddress(String v){ emailAddress.set(v); }
    public void setUsername(String v){ username.set(v); }
    public void setStatus(String v){ status.set(v); }
    public void setUpdatedAt(String v){ updatedAt.set(v); }
}
