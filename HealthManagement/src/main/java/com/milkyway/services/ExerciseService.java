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
            
            PreparedStatement stm = conn.prepareCall(sqlBuilder.toString());
            
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
        String sql = "INSERT INTO exercise (exerciseName, caloriesBurnedPerMin, userId) VALUES (?, ?, ?)";
        try (Connection conn = JdbcUtils.getConn(); PreparedStatement stm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stm.setString(1, exercise.getExerciseName());
            stm.setDouble(2, exercise.getCaloriesBurnedPerMin());
            
            if (exercise.getUserId() != null) {
                stm.setInt(3, exercise.getUserId().getId());
            } else {
                stm.setNull(3, java.sql.Types.INTEGER); // Bài tập mặc định
            }

            int affectedRows = stm.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stm.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        exercise.setIdExercise(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
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
