package db;

import java.sql.*;
import java.util.ArrayList;

public class StudentDAO {
    public static long addStud(long tutorId, String name, double rate) {
        String sql = "INSERT INTO students (tutor_id, name, rate) VALUES(?, ?, ?)";
        try(Connection conn = DataBase.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, tutorId);
            ps.setString(2, name);
            ps.setDouble(3, rate);
            ps.executeUpdate();
            try(ResultSet rs = ps.getGeneratedKeys()){
                if (rs.next()) return rs.getLong(1);
            }
            } catch (SQLException e) {
            System.out.println("Ошибка при добавлении студента " + e.getMessage());
        } return -1;
    }

    public static ArrayList<Student> findByTutor(long tutorId){
        ArrayList<Student> result = new ArrayList<>();
        String sql = "SELECT id, name, rate, active FROM students WHERE tutor_id = ? AND active = 1 ORDER BY name";
        try(Connection conn = DataBase.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setLong(1, tutorId);
            try(ResultSet rs = ps.executeQuery()){
                while (rs.next()){
                    result.add(new Student(
                            rs.getLong("id"),
                            tutorId,
                            rs.getString("name"),
                            rs.getDouble("rate"),
                            rs.getInt("active") == 1
                    ));
                }
            }
        } catch (SQLException e){
            System.out.println("Ошибка при попытке просмотра списка учеников: " + e.getMessage());
        } return result;
    }

    public static boolean lightDeleteStud(long tutorID, long studId) {
        String sql = "UPDATE students SET active = 0 where tutor_id = ? AND id = ? AND active = 1";
        try(Connection conn = DataBase.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setLong(1, tutorID);
            ps.setLong(2, studId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e){
            System.out.println("Ошибка при переводе студента в inactive: " + e.getMessage());
            return false;
        }
    }

    public static Student findById(long tutorId, long studId) {
        String sql = "SELECT id, tutor_id, name, rate, active FROM students WHERE id = ? AND tutor_id = ?";
        try(Connection conn = DataBase.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setLong(1, studId);
            ps.setLong(2, tutorId);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return new Student(
                            rs.getLong("id"),
                            rs.getLong("tutor_id"),
                            rs.getString("name"),
                            rs.getDouble("rate"),
                            rs.getInt("active") == 1
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при поиске студента: " + e.getMessage());
        } return null;
    }
}