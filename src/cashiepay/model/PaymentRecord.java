//package cashiepay.model;
//
//import javafx.beans.property.*;
//
//public class PaymentRecord {
//    private StringProperty id;
//    private StringProperty studentId;
//    private StringProperty firstName;
//    private StringProperty lastName;
//    private StringProperty middleName;
//    private StringProperty suffix;
//    private StringProperty orNumber;
//    private StringProperty particular;
//    private StringProperty mfoPap;
//    private DoubleProperty amount;
//    private StringProperty datePaid;
//    private StringProperty smsStatus;
//
//    public PaymentRecord(String id, String studentId, String firstName, String lastName,
//                         String middleName, String suffix, String orNumber,
//                         String particular, String mfoPap, double amount,
//                         String datePaid, String smsStatus) {
//        this.id = new SimpleStringProperty(id);
//        this.studentId = new SimpleStringProperty(studentId);
//        this.firstName = new SimpleStringProperty(firstName);
//        this.lastName = new SimpleStringProperty(lastName);
//        this.middleName = new SimpleStringProperty(middleName);
//        this.suffix = new SimpleStringProperty(suffix);
//        this.orNumber = new SimpleStringProperty(orNumber);
//        this.particular = new SimpleStringProperty(particular);
//        this.mfoPap = new SimpleStringProperty(mfoPap);
//        this.amount = new SimpleDoubleProperty(amount);
//        this.datePaid = new SimpleStringProperty(datePaid);
//        this.smsStatus = new SimpleStringProperty(smsStatus);
//    }
//
//    public StringProperty idProperty() { return id; }
//    public StringProperty studentIdProperty() { return studentId; }
//    public StringProperty firstNameProperty() { return firstName; }
//    public StringProperty lastNameProperty() { return lastName; }
//    public StringProperty middleNameProperty() { return middleName; }
//    public StringProperty suffixProperty() { return suffix; }
//    public StringProperty orNumberProperty() { return orNumber; }
//    public StringProperty particularProperty() { return particular; }
//    public StringProperty mfoPapProperty() { return mfoPap; }
//    public DoubleProperty amountProperty() { return amount; }
//    public StringProperty datePaidProperty() { return datePaid; }
//    public StringProperty smsStatusProperty() { return smsStatus; }
//    
//}


package cashiepay.model;

import javafx.beans.property.*;

public class PaymentRecord {
    private StringProperty id;
    private StringProperty studentId;
    private StringProperty firstName;
    private StringProperty lastName;
    private StringProperty middleName;
    private StringProperty suffix;
    private StringProperty orNumber;
    private StringProperty particular;
    private StringProperty mfoPap;
    private DoubleProperty amount;
    private StringProperty datePaid;
    private StringProperty smsStatus;

    public PaymentRecord(String id, String studentId, String firstName, String lastName,
                         String middleName, String suffix, String orNumber,
                         String particular, String mfoPap, double amount,
                         String datePaid, String smsStatus) {
        this.id = new SimpleStringProperty(id);
        this.studentId = new SimpleStringProperty(studentId);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.middleName = new SimpleStringProperty(middleName);
        this.suffix = new SimpleStringProperty(suffix);
        this.orNumber = new SimpleStringProperty(orNumber);
        this.particular = new SimpleStringProperty(particular);
        this.mfoPap = new SimpleStringProperty(mfoPap);
        this.amount = new SimpleDoubleProperty(amount);
        this.datePaid = new SimpleStringProperty(datePaid);
        this.smsStatus = new SimpleStringProperty(smsStatus);
    }

    // JavaFX Properties (for TableView)
    public StringProperty idProperty() { return id; }
    public StringProperty studentIdProperty() { return studentId; }
    public StringProperty firstNameProperty() { return firstName; }
    public StringProperty lastNameProperty() { return lastName; }
    public StringProperty middleNameProperty() { return middleName; }
    public StringProperty suffixProperty() { return suffix; }
    public StringProperty orNumberProperty() { return orNumber; }
    public StringProperty particularProperty() { return particular; }
    public StringProperty mfoPapProperty() { return mfoPap; }
    public DoubleProperty amountProperty() { return amount; }
    public StringProperty datePaidProperty() { return datePaid; }
    public StringProperty smsStatusProperty() { return smsStatus; }

    // Normal Getters (For Excel Export)
    public String getId() { return id.get(); }
    public String getStudentId() { return studentId.get(); }
    public String getFirstName() { return firstName.get(); }
    public String getLastName() { return lastName.get(); }
    public String getMiddleName() { return middleName.get(); }
    public String getSuffix() { return suffix.get(); }
    public String getOrNumber() { return orNumber.get(); }
    public String getParticular() { return particular.get(); }
    public String getMfoPap() { return mfoPap.get(); }
    public double getAmount() { return amount.get(); }
    public String getDatePaid() { return datePaid.get(); }
    public String getSmsStatus() { return smsStatus.get(); }
}
