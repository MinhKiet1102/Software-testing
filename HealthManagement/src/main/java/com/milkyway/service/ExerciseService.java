/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.service;

import com.milkyway.pojo.Exercise;
import com.milkyway.pojo.JdbcUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author ASUS
 */
public class ExerciseService {

    public List<Exercise> getExercise(String kw) throws SQLException {
        List<Exercise> results = new ArrayList<>();
        try (Connection conn = JdbcUtils.getConn()) {
            String sql = "SELECT * FROM exercise";
            if (kw != null && !kw.isEmpty()) {
                sql += " WHERE exerciseName like concat ('%', ?, '%')";
            }
            PreparedStatement stm = conn.prepareCall(sql);
            if (kw != null && !kw.isEmpty()) {
                stm.setString(1, kw);
            }

            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                Exercise e = new Exercise(rs.getInt("idExercise"), rs.getString("exerciseName"), rs.getString("imageExercise"), rs.getFloat("caloriesBurnedPerMin"));
                results.add(e);
            }

            return results;
        }
    }

    public boolean saveExercise(Exercise exercise) {
        String sql = "INSERT INTO exercise (exerciseName, caloriesBurnedPerMin) VALUES (?, ?)";
        try (Connection conn = JdbcUtils.getConn(); PreparedStatement stm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stm.setString(1, exercise.getExerciseName());
            stm.setDouble(2, exercise.getCaloriesBurnedPerMin());

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
