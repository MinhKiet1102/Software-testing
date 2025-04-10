/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.service;

import com.milkyway.pojo.FoodItem;
import com.milkyway.pojo.JdbcUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author votra
 */
public class FoodItemService {
    public static void insertFoodItem(FoodItem item) throws SQLException {
        Connection conn = JdbcUtils.getConn(); // Kết nối tới MySQL
        String sql = "INSERT INTO food_items (name, calo, carb, fat, protein, sodium, sugar) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, item.getName());
        stmt.setInt(2, item.getCalo());
        stmt.setDouble(3, item.getCarb());
        stmt.setDouble(4, item.getFat());
        stmt.setDouble(5, item.getProtein());
        stmt.setInt(6, item.getSodium());
        stmt.setDouble(7, item.getSugar());

        stmt.executeUpdate();
    }
}
