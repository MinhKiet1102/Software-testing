package com.milkyway.services;

import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.NutritionSummaryItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class NutritionSummaryItemService {

    public static void insertNutritionSummaryItems(List<NutritionSummaryItem> items) {
        String sql = "INSERT INTO nutrition_summary_item(type, current_value, goal_value, remaining_value) VALUES (?, ?, ?, ?)";

        try (Connection conn = JdbcUtils.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (NutritionSummaryItem item : items) {
                stmt.setString(1, item.getType());
                stmt.setDouble(2, item.getCurrent());
                stmt.setDouble(3, item.getGoal());
                stmt.setDouble(4, item.getRemaining());
                stmt.addBatch(); // gom nhiều câu lệnh
            }

            stmt.executeBatch(); // chạy hết 1 lần
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
