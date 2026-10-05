package db;

import java.sql.*;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class LessonDAO {
    private static final DateTimeFormatter FMT =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static long addLesson(long tutorId, long studentID, LocalDateTime startAt, int durationMin, double price, String status, String note, LocalDateTime remindAt) {
        String sql = "INSERT INTO lessons (tutor_id, student_id, start_at, duration_min, price, status, note, remind_at) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try(Connection conn = DataBase.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            ps.setLong(1, tutorId);
            ps.setLong(2, studentID);
            ps.setString(3, startAt.format(FMT));
            ps.setInt(4, durationMin);
            ps.setDouble(5, price);
            ps.setString(6, status);
            ps.setString(7, note);
            ps.setString(8, remindAt.format(FMT));
            ps.executeUpdate();
            try(ResultSet rs = ps.getGeneratedKeys()){
                if (rs.next()) return rs.getLong(1);
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении урока" + e.getMessage());
        } return -1;
    }

    public static Lesson findById(long lessonId, long tutorId) {
        String sql = "SELECT id, tutor_id, student_id, start_at, duration_min, price, status, note, remind_at FROM lessons WHERE id = ? AND tutor_id = ?";
        try(Connection conn = DataBase.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, lessonId);
            ps.setLong(2, tutorId);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e){
            System.out.println("Ошибка при попытке найти урок по айди: " + e.getMessage());
        }return null;
    }

    public static List<Lesson> findByDate(long tutorId, LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();
        String fromStr = from.format(FMT);
        String toStr = to.format(FMT);
        List<Lesson> result = new ArrayList<>();
        String sql = "SELECT id, tutor_id, student_id, start_at, duration_min, price, status, note, remind_at " +
                "FROM lessons " +
                "WHERE tutor_id = ? " +
                "AND start_at >= ? " +
                "AND start_at < ? " +
                "ORDER BY start_at";
        try(Connection conn = DataBase.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setLong(1, tutorId);
            ps.setString(2, fromStr);
            ps.setString(3, toStr);
            try(ResultSet rs = ps.executeQuery()){
                while (rs.next()){
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при поиске уроков в определённый день: " + e.getMessage());
        } return result;
    }

    public static List<Lesson> findByRange(long tutorId, LocalDateTime from, LocalDateTime to){
        if (from.isAfter(to)) return List.of();
        List<Lesson> result = new ArrayList<>();
        String fromStr = from.format(FMT);
        String toStr = to.format(FMT);
        String sql = "SELECT id, tutor_id, student_id, start_at, duration_min, price, status, note, remind_at " +
                "FROM lessons " +
                "WHERE tutor_id = ? " +
                "AND start_at >= ? " +
                "AND start_at < ? " +
                "ORDER BY start_at";
        try(Connection conn = DataBase.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tutorId);
            ps.setString(2, fromStr);
            ps.setString(3, toStr);
            try(ResultSet rs = ps.executeQuery()){
                while (rs.next()){
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e){
            System.out.println("Ошибка при попытке найти урок по периоду: " + e.getMessage());
        }return result;
    }

    public static boolean updateStatus (long id, long tutorId, LessonsStatus status) {
        String sql = "UPDATE lessons SET status = ? WHERE id = ? AND tutor_id = ?";
        try(Connection conn = DataBase.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, status.name());
            ps.setLong(2, id);
            ps.setLong(3, tutorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e){
            System.out.println("Ошибка при попытке обновления статуса урока: " + e.getMessage());
            return false;
        }
    }

    public static double moneyEarned(long tutorId, LocalDateTime from, LocalDateTime to) {
        String sql = """
            SELECT COALESCE(SUM(price), 0) AS total
            FROM lessons
            WHERE tutor_id = ?
              AND status = 'DONE'
              AND start_at >= ?
              AND start_at <  ?
            """;
        try (Connection conn = DataBase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tutorId);
            ps.setString(2, from.format(FMT));
            ps.setString(3, to.format(FMT));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка moneyEarned: " + e.getMessage());
        }
        return 0;
    }

    public static List<Lesson> showLessons (long tutorId){
        List<Lesson> result = new ArrayList<>();
        String sql = "SELECT id, tutor_id, student_id, start_at, duration_min, price, status, note, remind_at FROM lessons " +
                "WHERE tutor_id = ? " +
                "ORDER BY start_at";
        try(Connection conn = DataBase.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setLong(1, tutorId);
            try(ResultSet rs = ps.executeQuery()){
                while (rs.next()){
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при попытке просмотра списка уроков");
        } return result;
    }

    private static Lesson mapRow(ResultSet rs) throws SQLException {
        return new Lesson(
                rs.getLong("id"),
                rs.getLong("tutor_id"),
                rs.getLong("student_id"),
                rs.getString("start_at"),
                rs.getString("status"),
                rs.getString("note"),
                rs.getString("remind_at"),
                rs.getDouble("price"),
                rs.getInt("duration_min")
        );
    }

    public static void markReminded(long lessonId){
        String sql = "UPDATE lessons SET remind_at = NULL WHERE id = ?";
        try(Connection conn = DataBase.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setLong(1, lessonId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Ошибка при обновлении статуса урока на упомянутый: " + e.getMessage());
        }
    }

    public static List<Lesson> findUpcomingWithoutReminder(LocalDateTime time){
        String sql = """
                SELECT id, tutor_id, student_id, start_at, duration_min, price, status, note, remind_at
                FROM lessons
                WHERE status = 'PLANNED'
                AND remind_at IS NOT NULL
                AND remind_at < ?
                """;
        List<Lesson> result = new ArrayList<>();
        try(Connection conn = DataBase.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, time.format(FMT));
            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()){
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e){
            System.out.println("Ошибка при поиске уроков для напоминания: " + e.getMessage());
        } return result;
    }

}