/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.services;

import com.milkyway.pojo.Exercise;
import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ExerciseService {

    public List<Exercise> getExercise(String kw) throws SQLException {
        List<Exercise> results = new ArrayList<>();
        try (Connection conn = JdbcUtils.getConn()) {
            // Cập nhật SQL để lấy bài tập mặc định (userId IS NULL), bài tập của người dùng hiện tại
            // và bài tập của người dùng có role là "Admin"
            StringBuilder sqlBuilder = new StringBuilder("SELECT e.* FROM exercise e LEFT JOIN user u ON e.userId = u.id WHERE (e.userId IS NULL");            
            // Nếu có người dùng đăng nhập, thêm điều kiện để lấy bài tập của họ
            if (User.getCurrentUser() != null) {
                sqlBuilder.append(" OR e.userId = ?");
            }
            
            // Thêm điều kiện để lấy bài tập của các admin
            sqlBuilder.append(" OR u.role = 'ADMIN')");
            
            // Thêm điều kiện tìm kiếm theo tên nếu có
            if (kw != null && !kw.isEmpty()) {
                sqlBuilder.append(" AND e.exerciseName LIKE CONCAT('%', ?, '%')");
            }
            
            PreparedStatement stm = conn.prepareStatement(sqlBuilder.toString());
            
            int paramIndex = 1;
            
            // Nếu có người dùng đăng nhập, thiết lập tham số userId
            if (User.getCurrentUser() != null) {
                stm.setInt(paramIndex++, User.getCurrentUser().getId());
            }
            
            // Nếu có từ khóa tìm kiếm, thiết lập tham số
            if (kw != null && !kw.isEmpty()) {
                stm.setString(paramIndex, kw);
            }
            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                Exercise e = new Exercise(rs.getInt("idExercise"), rs.getString("exerciseName"), rs.getString("imageExercise"), rs.getFloat("caloriesBurnedPerMin"));
                
                // Thiết lập userId nếu có
                if (rs.getObject("userId") != null) {
                    User user = new User(rs.getInt("userId"));
                    e.setUserId(user);
                }
                
                results.add(e);
            }

            return results;
        }
    }

    public boolean saveExercise(Exercise exercise) {
        String sql = "INSERT INTO exercise (exerciseName, caloriesBurnedPerMin, imageExercise, userId) VALUES (?, ?, ?, ?)";
        
        // Kiểm tra kết nối cơ sở dữ liệu trước
        Connection conn = null;
        PreparedStatement stm = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = JdbcUtils.getConn();
            if (conn == null) {
                System.err.println("Không thể kết nối đến cơ sở dữ liệu");
                return false;
            }
            
            stm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            // Set exerciseName parameter
            stm.setString(1, exercise.getExerciseName());
            
            // Set caloriesBurnedPerMin parameter
            stm.setDouble(2, exercise.getCaloriesBurnedPerMin());
            
            // Set imageExercise parameter
            if (exercise.getImageExercise() != null && !exercise.getImageExercise().isEmpty()) {
                stm.setString(3, exercise.getImageExercise());
            } else {
                stm.setNull(3, java.sql.Types.VARCHAR);
            }
            
            // Set userId parameter
            if (exercise.getUserId() != null) {
                stm.setInt(4, exercise.getUserId().getId());
            } else {
                stm.setNull(4, java.sql.Types.INTEGER); // Bài tập mặc định
            }
            
            // In thông tin SQL và tham số cho mục đích debug
            System.out.println("SQL thực thi: " + sql);
            System.out.println("Tham số: exerciseName=" + exercise.getExerciseName() + 
                             ", caloriesBurnedPerMin=" + exercise.getCaloriesBurnedPerMin() + 
                             ", imageExercise=" + exercise.getImageExercise() + 
                             ", userId=" + (exercise.getUserId() != null ? exercise.getUserId().getId() : "NULL"));

            int affectedRows = stm.executeUpdate();
            System.out.println("Số dòng được thêm vào: " + affectedRows);
            
            if (affectedRows > 0) {
                // Trước tiên, thử lấy ID được tạo thông qua getGeneratedKeys
                generatedKeys = stm.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    exercise.setIdExercise(id);
                    System.out.println("Đã lấy được ID tự động: " + id);
                    return true;
                } else {
                    System.out.println("Không thể lấy ID tự động, thử tìm bằng tên bài tập");
                    
                    // Phương án dự phòng: Tìm ID dựa trên tên bài tập
                    String checkSql = "SELECT idExercise FROM exercise WHERE exerciseName = ?";
                    try (PreparedStatement checkStm = conn.prepareStatement(checkSql)) {
                        checkStm.setString(1, exercise.getExerciseName());
                        ResultSet rs = checkStm.executeQuery();
                        if (rs.next()) {
                            int id = rs.getInt("idExercise");
                            exercise.setIdExercise(id);
                            System.out.println("ID tìm thấy qua truy vấn: " + id);
                            return true;
                        } else {
                            System.out.println("Không tìm thấy bài tập sau khi thêm!");
                        }
                    }
                }
                
                // Nếu không thể xác định ID nhưng chắc chắn đã thêm bản ghi, vẫn trả về true
                System.out.println("Đã thêm bản ghi nhưng không lấy được ID");
                return true;
            } else {
                System.out.println("Không thêm được bản ghi vào cơ sở dữ liệu");
            }
        } catch (SQLException ex) {
            Logger.getLogger(ExerciseService.class.getName()).log(Level.SEVERE, "Lỗi lưu bài tập", ex);
            ex.printStackTrace();
        } finally {
            // Đóng các tài nguyên theo thứ tự ngược lại
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (stm != null) stm.close();
                // Không đóng kết nối ở đây vì có thể tái sử dụng
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng tài nguyên: " + e.getMessage());
            }
        }
        return false;
    }

    public Exercise getExerciseByName(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT * FROM exercise WHERE exerciseName = ?";
        try (Connection conn = JdbcUtils.getConn(); PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, name.trim());
            ResultSet rs = stm.executeQuery();

            if (rs.next()) {
                Exercise ex = new Exercise();
                ex.setIdExercise(rs.getInt("idExercise"));
                ex.setExerciseName(rs.getString("exerciseName"));
                ex.setCaloriesBurnedPerMin(rs.getDouble("caloriesBurnedPerMin"));
                if (hasColumn(rs, "Lượng calo tiêu thụ:")) {
                    ex.setCaloriesBurnedPerMin(rs.getDouble("caloPerMin"));
                }
                return ex;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ExerciseService.class.getName()).log(Level.SEVERE, "Lỗi lấy bài tập theo tên", ex);
            throw ex;
        }
        return null; // Không tìm thấy
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException sqlex) {
            return false;
        }
    }
}
