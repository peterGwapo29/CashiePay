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
        String orNumber,
        int particular,
        int mfoPap,
        double amount,
        String smsStatus,
        String paidAt) {

        String sql = "INSERT INTO collection (student_id, first_name, last_name, middle_name, suffix, or_number, particular, `mfo_pap`, amount, sms_status, paid_at) "
               + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, studentId);
            ps.setString(2, fname);
            ps.setString(3, lname);
            ps.setString(4, mname);
            ps.setString(5, suffix);
            ps.setString(6, orNumber);
            ps.setInt(7, particular);
            ps.setInt(8, mfoPap);
            ps.setDouble(9, amount);
            ps.setString(10, smsStatus);
            ps.setString(11, paidAt);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updatePayment(String id, String studentId, String fname, String lname, String mname,
                             String suffix, int particularId, int mfoPapId, double amount,
                             String smsStatus, String paidAt) throws SQLException {
    String sql = "UPDATE collection SET student_id=?, first_name=?, last_name=?, middle_name=?, suffix=?, " +
                 "particular=?, mfo_pap=?, amount=?, sms_status=?, paid_at=? WHERE id=?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, studentId);
        ps.setString(2, fname);
        ps.setString(3, lname);
        ps.setString(4, mname);
        ps.setString(5, suffix);
        ps.setInt(6, particularId);
        ps.setInt(7, mfoPapId);
        ps.setDouble(8, amount);
        ps.setString(9, smsStatus);
        ps.setString(10, paidAt);
        ps.setString(11, id);
        return ps.executeUpdate() > 0;
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
    public String toString() { return name; }
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
