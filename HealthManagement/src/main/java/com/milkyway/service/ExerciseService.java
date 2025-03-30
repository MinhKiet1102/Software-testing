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
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.events.MouseEvent;



/**
 *
 * @author ASUS
 */
public class ExerciseService {
    public List<Exercise> getExercise(String kw) throws SQLException {
        List<Exercise> results = new ArrayList<>();
        try (Connection conn = JdbcUtils.getConn()) {
            String sql = "SELECT * FROM exercise";
            if(kw!= null && !kw.isEmpty())
                sql += " WHERE exerciseName like concat ('%', ?, '%')";
            PreparedStatement stm = conn.prepareCall(sql);
            if(kw!= null && !kw.isEmpty())
                stm.setString(1, kw);
            
            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                Exercise e = new Exercise(rs.getInt("idExercise"), rs.getString("exerciseName"), rs.getString("imageExercise"), rs.getFloat("caloriesBurnedPerMin"));
                results.add(e);
            }
            
            return results;
        }
    }
   
}
