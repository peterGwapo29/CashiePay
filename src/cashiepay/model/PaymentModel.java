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
            int studentPk,
            String orNumber,
            int particularId,
            Integer fundId,
            Integer accountId,
            double amount,
            String smsStatus,
            String paidAt,
            Integer semesterId,
            String status
    ) {

        String sql = "INSERT INTO collection " +
                     "(student_id, or_number, particular_id, mfo_pap_id, account_id, amount, sms_status, paid_at, semester_id, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentPk);
            ps.setString(2, orNumber);
            ps.setInt(3, particularId);

            // fundId can be null
            if (fundId != null) {
                ps.setInt(4, fundId);
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }

            // accountId can be null
            if (accountId != null) {
                ps.setInt(5, accountId);
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }

            ps.setDouble(6, amount);
            ps.setString(7, smsStatus);
            ps.setString(8, paidAt);
            ps.setInt(9, semesterId);
            ps.setString(10, status);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // UPDATE collection
    public boolean updatePayment(
            String id,
            int studentPk,
            String orNumber,
            int particularId,
            Integer fundId,
            Integer accountId,
            double amount,
            String smsStatus,
            String paidAt,
            Integer semesterId
    ) throws SQLException {

        String sql = "UPDATE collection SET " +
                     "student_id = ?, " +
                     "or_number = ?, " +
                     "particular_id = ?, " +
                     "mfo_pap_id = ?, " +
                     "account_id = ?, " +
                     "amount = ?, " +
                     "sms_status = ?, " +
                     "paid_at = ?, " +
                     "semester_id = ? " +
                     "WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentPk);
            ps.setString(2, orNumber);
            ps.setInt(3, particularId);

            if (fundId != null) {
                ps.setInt(4, fundId);
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }

            if (accountId != null) {
                ps.setInt(5, accountId);
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }

            ps.setDouble(6, amount);
            ps.setString(7, smsStatus);
            ps.setString(8, paidAt);
            ps.setInt(9, semesterId);
            ps.setString(10, id);

            return ps.executeUpdate() > 0;
        }
    }
}
