package cashiepay.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Account {

    private final SimpleIntegerProperty id;
    private final SimpleIntegerProperty fundId;
    private final SimpleStringProperty fundName;
    private final SimpleStringProperty accountName;
    private final SimpleStringProperty createdAt;
    private final SimpleStringProperty updatedAt;
    private final SimpleStringProperty status;

    public Account(int id,
                   int fundId,
                   String fundName,
                   String accountName,
                   String createdAt,
                   String updatedAt,
                   String status) {
        this.id = new SimpleIntegerProperty(id);
        this.fundId = new SimpleIntegerProperty(fundId);
        this.fundName = new SimpleStringProperty(fundName);
        this.accountName = new SimpleStringProperty(accountName);
        this.createdAt = new SimpleStringProperty(createdAt);
        this.updatedAt = new SimpleStringProperty(updatedAt);
        this.status = new SimpleStringProperty(status);
    }

    // getters
    public int getId() { return id.get(); }
    public int getFundId() { return fundId.get(); }
    public String getFundName() { return fundName.get(); }
    public String getAccountName() { return accountName.get(); }
    public String getCreatedAt() { return createdAt.get(); }
    public String getUpdatedAt() { return updatedAt.get(); }
    public String getStatus() { return status.get(); }

    // properties
    public SimpleIntegerProperty idProperty() { return id; }
    public SimpleIntegerProperty fundIdProperty() { return fundId; }
    public SimpleStringProperty fundNameProperty() { return fundName; }
    public SimpleStringProperty accountNameProperty() { return accountName; }
    public SimpleStringProperty createdAtProperty() { return createdAt; }
    public SimpleStringProperty updatedAtProperty() { return updatedAt; }
    public SimpleStringProperty statusProperty() { return status; }

    // setters (for editing)
    public void setFundId(int v) { this.fundId.set(v); }
    public void setFundName(String v) { this.fundName.set(v); }
    public void setAccountName(String v) { this.accountName.set(v); }
    public void setUpdatedAt(String v) { this.updatedAt.set(v); }
    public void setStatus(String v) { this.status.set(v); }
}
