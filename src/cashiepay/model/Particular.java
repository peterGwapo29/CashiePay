package cashiepay.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Particular {

    private final SimpleIntegerProperty id;
    private final SimpleStringProperty particularName;
    private final SimpleStringProperty amount;
    private final SimpleStringProperty createdAt;
    private final SimpleStringProperty updatedAt;
    private final SimpleStringProperty status;
    

    public Particular(int id, String particularName, String amount, String createdAt, String updatedAt, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.particularName = new SimpleStringProperty(particularName);
        this.amount = new SimpleStringProperty(amount);
        this.createdAt = new SimpleStringProperty(createdAt);
        this.updatedAt = new SimpleStringProperty(updatedAt);
        this.status = new SimpleStringProperty(status);
    }
    
    //getters
    public int getId() {
        return id.get();
    }

    public String getParticularName() {
        return particularName.get();
    }

    public String getAmount() {
        return amount.get();
    }

    public String getCreatedAt() {
        return createdAt.get();
    }

    public String getUpdatedAt() {
        return updatedAt.get();
    }
    
    public String getStatus() {
        return status.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public SimpleStringProperty particularNameProperty() {
        return particularName;
    }

    public SimpleStringProperty amountProperty() {
        return amount;
    }

    public SimpleStringProperty createdAtProperty() {
        return createdAt;
    }

    public SimpleStringProperty updatedAtProperty() {
        return updatedAt;
    }
    
    public SimpleStringProperty statusProperty() {
        return status;
    }
    
    //Setters
    public void setParticularName(String name) { this.particularName.set(name); }
    public void setAmount(String amount) { this.amount.set(amount); }
    public void setUpdatedAt(String updatedAt) { this.updatedAt.set(updatedAt); }
    public void setStatus(String status) { this.status.set(status); }
}
