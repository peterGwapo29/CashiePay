package cashiepay.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PaymentModel {

    private Connection conn;

    public PaymentModel(Connection conn) {
        this.conn = conn;
    }

    public boolean insertPayment(
        String studentId,
        String fname,
        String lname,
        String mname,
        String suffix,
        String course,
        String orNumber,
        int particular,
        int mfoPap,
        double amount,
        String paymentStatus,
        String smsStatus,
        String paidAt) {

        String sql = "INSERT INTO collection (student_id, first_name, last_name, middle_name, suffix, course, or_number, particular, `mfo_pap`, amount, payment_status, sms_status, paid_at) "
               + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, studentId);
            ps.setString(2, fname);
            ps.setString(3, lname);
            ps.setString(4, mname);
            ps.setString(5, suffix);
            ps.setString(6, course);
            ps.setString(7, orNumber);
            ps.setInt(8, particular);
            ps.setInt(9, mfoPap);
            ps.setDouble(10, amount);
            ps.setString(11, paymentStatus);
            ps.setString(12, smsStatus);
            ps.setString(13, paidAt);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
public static class ParticularItem {
    private int id;
    private String name;
    private double amount;

    public ParticularItem(int id, String name, double amount) {
        this.id = id;
        this.name = name;
        this.amount = amount;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getAmount() { return amount; }

    @Override
    public String toString() { return name; } // Display name in ComboBox
}

public static class MfoPapItem {
    private int id;
    private String name;

    public MfoPapItem(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() { return name; }
}

}
