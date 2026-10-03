package db;

import java.sql.*;

public class UserDao {
    public static User findById(long id) {
        String sql = "SELECT id, username, timezone FROM users WHERE id = ?";
        try (Connection conn = DataBase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getLong("id"),
                            rs.getString("username"),
                            rs.getString("timezone")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка " + e.getMessage());
        }
        return null;
    }

    public static void saveOrUpdate(User user) {
        String sql = """
                INSERT INTO users (id, username, timezone)
                VALUES (?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    username = excluded.username,
                    timezone = excluded.timezone 
                """;
        try (Connection conn = DataBase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getTimezone());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Ошибочка" + e.getMessage());
        }
    }
}
