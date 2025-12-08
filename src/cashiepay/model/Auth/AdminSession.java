package cashiepay.model.Auth;

public class AdminSession {

    private static int id;
    private static String adminName;
    private static String email;
    private static String username;

    public static void setSession(int adminId, String name, String emailAddress, String user) {
        id = adminId;
        adminName = name;
        email = emailAddress;
        username = user;
    }

    public static int getId() { return id; }
    public static String getAdminName() { return adminName; }
    public static String getEmail() { return email; }
    public static String getUsername() { return username; }

    public static void clear() {
        id = 0;
        adminName = null;
        email = null;
        username = null;
    }
}
