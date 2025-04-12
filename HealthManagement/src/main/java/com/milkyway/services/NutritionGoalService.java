package com.milkyway.services;

import com.milkyway.pojo.NutritionGoal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * Service class để quản lý thao tác với bảng nutrition_goals trong database
 */
public class NutritionGoalService {
    private final Connection conn;
    
    public NutritionGoalService(Connection conn) {
        this.conn = conn;
    }
    
    /**
     * Thêm mục tiêu dinh dưỡng mới cho người dùng
     * @param goal Đối tượng NutritionGoal cần thêm
     * @return ID của mục tiêu vừa được thêm
     * @throws SQLException Nếu có lỗi khi thực hiện thao tác SQL
     */
    public int addNutritionGoal(NutritionGoal goal) throws SQLException {
        String sql = "INSERT INTO nutrition_goals (user_id, nutrition_type, goal_value, unit) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stm.setInt(1, goal.getUser().getId());
            stm.setString(2, goal.getNutritionType());
            stm.setDouble(3, goal.getGoalValue());
            stm.setString(4, goal.getUnit());
            
            stm.executeUpdate();
            
            // Lấy ID vừa được tạo
            try (ResultSet rs = stm.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return -1;
    }
    
    /**
     * Cập nhật mục tiêu dinh dưỡng hiện có
     * @param goal Đối tượng NutritionGoal cần cập nhật
     * @throws SQLException Nếu có lỗi khi thực hiện thao tác SQL
     */
    public void updateNutritionGoal(NutritionGoal goal) throws SQLException {
        String sql = "UPDATE nutrition_goals SET goal_value = ?, unit = ? WHERE id = ?";
        
        try (PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setDouble(1, goal.getGoalValue());
            stm.setString(2, goal.getUnit());
            stm.setInt(3, goal.getId());
            
            stm.executeUpdate();
        }
    }
    
    /**
     * Xóa mục tiêu dinh dưỡng theo ID
     * @param goalId ID của mục tiêu cần xóa
     * @throws SQLException Nếu có lỗi khi thực hiện thao tác SQL
     */
    public void deleteNutritionGoal(int goalId) throws SQLException {
        String sql = "DELETE FROM nutrition_goals WHERE id = ?";
        
        try (PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, goalId);
            stm.executeUpdate();
        }
    }
    
    /**
     * Lấy mục tiêu dinh dưỡng theo ID
     * @param goalId ID của mục tiêu cần lấy
     * @return Đối tượng NutritionGoal tương ứng hoặc null nếu không tìm thấy
     * @throws SQLException Nếu có lỗi khi thực hiện thao tác SQL
     */
    public NutritionGoal getGoalById(int goalId) throws SQLException {
        String sql = "SELECT * FROM nutrition_goals WHERE id = ?";
        
        try (PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, goalId);
            
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return extractNutritionGoalFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Lấy tất cả mục tiêu dinh dưỡng của một người dùng
     * @param userId ID của người dùng
     * @return Danh sách các mục tiêu dinh dưỡng của người dùng
     * @throws SQLException Nếu có lỗi khi thực hiện thao tác SQL
     */
    public List<NutritionGoal> getGoalsByUserId(int userId) throws SQLException {
        List<NutritionGoal> goals = new ArrayList<>();
        String sql = "SELECT * FROM nutrition_goals WHERE user_id = ?";
        
        try (PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, userId);
            
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    goals.add(extractNutritionGoalFromResultSet(rs));
                }
            }
        }
        
        return goals;
    }
    
    /**
     * Lấy mục tiêu dinh dưỡng cụ thể của một người dùng
     * @param userId ID của người dùng
     * @param nutritionType Loại dinh dưỡng (Calories, Protein, etc.)
     * @return Đối tượng NutritionGoal tương ứng hoặc null nếu không tìm thấy
     * @throws SQLException Nếu có lỗi khi thực hiện thao tác SQL
     */
    public NutritionGoal getGoalByUserIdAndType(int userId, String nutritionType) throws SQLException {
        String sql = "SELECT * FROM nutrition_goals WHERE user_id = ? AND nutrition_type = ?";
        
        try (PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, userId);
            stm.setString(2, nutritionType);
            
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return extractNutritionGoalFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Lưu hoặc cập nhật mục tiêu dinh dưỡng
     * @param userId ID của người dùng
     * @param nutritionType Loại dinh dưỡng
     * @param goalValue Giá trị mục tiêu
     * @param unit Đơn vị
     * @throws SQLException Nếu có lỗi khi thực hiện thao tác SQL
     */
    public void saveOrUpdateGoal(Integer userId, String nutritionType, double goalValue, String unit) throws SQLException {
        NutritionGoal existingGoal = getGoalByUserIdAndType(userId, nutritionType);
        
        if (existingGoal == null) {
            // Create new goal
            NutritionGoal newGoal = new NutritionGoal();
            newGoal.setUserId(userId);
            newGoal.setNutritionType(nutritionType);
            newGoal.setGoalValue(goalValue);
            newGoal.setUnit(unit);
            newGoal.setCreatedDate(new Date());
            newGoal.setModifiedDate(new Date());
            addNutritionGoal(newGoal);
        } else {
            // Update existing goal
            existingGoal.setGoalValue(goalValue);
            existingGoal.setUnit(unit);
            existingGoal.setModifiedDate(new Date());
            updateNutritionGoal(existingGoal);
        }
    }
    
    /**
     * Helper method để chuyển đổi từ ResultSet sang đối tượng NutritionGoal
     */
    private NutritionGoal extractNutritionGoalFromResultSet(ResultSet rs) throws SQLException {
        NutritionGoal goal = new NutritionGoal();
        goal.setId(rs.getInt("id"));
        goal.setUserId(rs.getInt("user_id"));
        goal.setNutritionType(rs.getString("nutrition_type"));
        goal.setGoalValue(rs.getDouble("goal_value"));
        goal.setUnit(rs.getString("unit"));
        goal.setCreatedDate(rs.getTimestamp("created_date"));
        goal.setModifiedDate(rs.getTimestamp("modified_date"));
        return goal;
    }
}