package cashiepay.model.Auth;

public class AdminSession {

    private static int id;
    private static String adminName;
    private static String email;
    private static String username;
    private static String role;

    public static void setSession(int adminId, String name, String emailAddress, String user, String userRole) {
        id = adminId;
        adminName = name;
        email = emailAddress;
        username = user;
        role = userRole;
    }

    public static int getId() { return id; }
    public static String getAdminName() { return adminName; }
    public static String getEmail() { return email; }
    public static String getUsername() { return username; }
    public static String getRole() { return role; }
    
    public static boolean isSuperAdmin() {
        return "super_admin".equalsIgnoreCase(role);
    }

    public static boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    public static void clear() {
        id = 0;
        adminName = null;
        email = null;
        username = null;
        role = null;
    }
}
