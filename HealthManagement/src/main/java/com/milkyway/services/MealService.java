package com.milkyway.services;

import com.milkyway.pojo.Food;
import com.milkyway.pojo.Meal;
import com.milkyway.pojo.MealFood;
import com.milkyway.pojo.MealFoodPK;
import com.milkyway.pojo.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MealService {
    
    private final Connection conn;
    
    public MealService(Connection conn) {
        this.conn = conn;
    }

    public List<Meal> getMealsByUserAndDate(int userId, Date date) throws SQLException {
        List<Meal> meals = new ArrayList<>();
        
        try {
            // Format the date to SQL date format
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = dateFormat.format(date);
            
            System.out.println("Searching meals for user ID: " + userId + " on date: " + dateStr);

            String sql = "SELECT idMeal, nameMeal, totalCalories, dateOfMeal " +
                        "FROM meal " +
                        "WHERE userId = ? AND DATE(dateOfMeal) = ?";
            
            System.out.println("SQL query: " + sql);
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, dateStr);
                
                System.out.println("Executing query with parameters: userId=" + userId + ", date=" + dateStr);
                
                ResultSet rs = stmt.executeQuery();
                int count = 0;
                
                while (rs.next()) {
                    count++;
                    Meal meal = new Meal();
                    meal.setIdMeal(rs.getInt("idMeal"));
                    meal.setNameMeal(rs.getString("nameMeal"));
                    meal.setTotalCalories(rs.getDouble("totalCalories"));
                    meal.setDateOfMeal(rs.getTimestamp("dateOfMeal"));
                    
                    System.out.println("Found meal: ID=" + meal.getIdMeal() + ", Name=" + meal.getNameMeal() + 
                                      ", Date=" + dateFormat.format(meal.getDateOfMeal()));
                    
                    // Set user ID
                    User user = new User();
                    user.setId(userId);
                    meal.setUserId(user);
                    
                    // Load food items for this meal
                    loadMealFoods(meal);
                    
                    meals.add(meal);
                }
                
                System.out.println("Total meals found: " + count);
            }
            
            return meals;
        } catch (Exception e) {
            System.err.println("Error in getMealsByUserAndDate: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Error retrieving meals: " + e.getMessage(), e);
        }
    }
    
    private void loadMealFoods(Meal meal) throws SQLException {
        try {
            System.out.println("Loading foods for meal ID: " + meal.getIdMeal());
            
            // Query to get meal_food entries for this meal - updated to include sodium and sugar columns
            String sql = "SELECT mf.mealId, mf.foodId, mf.quantity, mf.unit, " +
                         "f.foodName, f.calories, f.protein, f.carb, f.fat, f.sodium, f.sugar " +
                         "FROM meal_food mf " +
                         "JOIN food f ON mf.foodId = f.idFood " +
                         "WHERE mf.mealId = ?";
            
            System.out.println("SQL query for foods: " + sql);
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, meal.getIdMeal());
                ResultSet rs = stmt.executeQuery();
                
                Set<MealFood> mealFoods = new HashSet<>();
                int count = 0;
                
                while (rs.next()) {
                    count++;
                    int mealId = rs.getInt("mealId");
                    int foodId = rs.getInt("foodId");
                    int quantity = rs.getInt("quantity");
                    String unit = rs.getString("unit");
                    
                    // Create the food object
                    Food food = new Food();
                    food.setIdFood(foodId);
                    food.setFoodName(rs.getString("foodName"));
                    food.setCalories(rs.getDouble("calories"));
                    food.setProtein(rs.getObject("protein") != null ? rs.getDouble("protein") : null);
                    food.setCarb(rs.getObject("carb") != null ? rs.getDouble("carb") : null);
                    food.setFat(rs.getObject("fat") != null ? rs.getDouble("fat") : null);
                    food.setSodium(rs.getObject("sodium") != null ? rs.getDouble("sodium") : null);
                    food.setSugar(rs.getObject("sugar") != null ? rs.getDouble("sugar") : null);
                    
                    System.out.println("Found food: ID=" + food.getIdFood() + ", Name=" + food.getFoodName() +
                                      ", Sodium=" + food.getSodium() + ", Sugar=" + food.getSugar());
                    
                    // Create the meal food relationship
                    MealFoodPK pk = new MealFoodPK(mealId, foodId);
                    MealFood mealFood = new MealFood(pk, unit, quantity);
                    mealFood.setFood(food);
                    mealFood.setMeal(meal);
                    
                    mealFoods.add(mealFood);
                }
                
                System.out.println("Total foods found for meal: " + count);
                meal.setMealFoodSet(mealFoods);
            }
        } catch (Exception e) {
            System.err.println("Error in loadMealFoods: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Error loading meal foods: " + e.getMessage(), e);
        }
    }

    public Meal getMealById(int mealId) throws SQLException {
        Meal meal = null;
        
        try {
            String sql = "SELECT idMeal, nameMeal, totalCalories, dateOfMeal, userId " +
                         "FROM meal " +
                         "WHERE idMeal = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, mealId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    meal = new Meal();
                    meal.setIdMeal(rs.getInt("idMeal"));
                    meal.setNameMeal(rs.getString("nameMeal"));
                    meal.setTotalCalories(rs.getDouble("totalCalories"));
                    meal.setDateOfMeal(rs.getTimestamp("dateOfMeal"));
                    
                    // Set user ID
                    User user = new User();
                    user.setId(rs.getInt("userId"));
                    meal.setUserId(user);
                    
                    // Load food items for this meal
                    loadMealFoods(meal);
                }
            }
            
            return meal;
        } catch (Exception e) {
            System.err.println("Error in getMealById: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Error retrieving meal: " + e.getMessage(), e);
        }
    }
    

    public Meal saveMeal(Meal meal) throws SQLException {
        try {
            String sql = "INSERT INTO meal (nameMeal, totalCalories, dateOfMeal, userId) " +
                         "VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, meal.getNameMeal());
                stmt.setDouble(2, meal.getTotalCalories());
                stmt.setTimestamp(3, new java.sql.Timestamp(meal.getDateOfMeal().getTime()));
                stmt.setInt(4, meal.getUserId().getId());
                
                stmt.executeUpdate();
                
                // Get generated ID
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    meal.setIdMeal(generatedKeys.getInt(1));
                }
            }
            
            return meal;
        } catch (Exception e) {
            System.err.println("Error in saveMeal: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Error saving meal: " + e.getMessage(), e);
        }
    }

    public void addFoodToMeal(int mealId, int foodId, double quantity, String unit) throws SQLException {
        try {
            // First get the food to calculate calories
            Food food = getFoodById(foodId);
            if (food == null) {
                throw new SQLException("Food not found");
            }
            
            // Insert the meal-food relationship
            String sqlMealFood = "INSERT INTO meal_food (mealId, foodId, quantity, unit) " +
                               "VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlMealFood)) {
                stmt.setInt(1, mealId);
                stmt.setInt(2, foodId);
                stmt.setDouble(3, quantity);
                stmt.setString(4, unit);
                
                stmt.executeUpdate();
            }
            
            // Update the meal's total calories
            double foodCalories = food.getCalories() * quantity;
            
            String sqlUpdateCalories = "UPDATE meal " +
                                     "SET totalCalories = totalCalories + ? " +
                                     "WHERE idMeal = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateCalories)) {
                stmt.setDouble(1, foodCalories);
                stmt.setInt(2, mealId);
                
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Error in addFoodToMeal: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Error adding food to meal: " + e.getMessage(), e);
        }
    }
    

    public void deleteMealFood(int mealFoodId) throws SQLException {
        try {
            // Trước tiên, lấy thông tin về món ăn để tính toán lại calo
            String sqlGetMealFood = "SELECT mf.mealId, mf.quantity, f.calories " +
                                  "FROM meal_food mf " +
                                  "JOIN food f ON mf.foodId = f.idFood " +
                                  "WHERE mf.id = ?";
            
            int mealId = 0;
            double foodCalories = 0;
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlGetMealFood)) {
                stmt.setInt(1, mealFoodId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    mealId = rs.getInt("mealId");
                    int quantity = rs.getInt("quantity");
                    double calories = rs.getDouble("calories");
                    foodCalories = calories * quantity;
                } else {
                    // Không tìm thấy món ăn, không cần xóa
                    return;
                }
            }
            
            // Xóa món ăn khỏi bảng meal_food
            String sqlDeleteMealFood = "DELETE FROM meal_food WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteMealFood)) {
                stmt.setInt(1, mealFoodId);
                stmt.executeUpdate();
            }
            
            // Cập nhật tổng calories của bữa ăn
            String sqlUpdateCalories = "UPDATE meal " +
                                     "SET totalCalories = GREATEST(0, totalCalories - ?) " +
                                     "WHERE idMeal = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateCalories)) {
                stmt.setDouble(1, foodCalories);
                stmt.setInt(2, mealId);
                stmt.executeUpdate();
            }
            
            System.out.println("Đã xóa món ăn ID=" + mealFoodId + " khỏi bữa ăn ID=" + mealId);
            
        } catch (Exception e) {
            System.err.println("Lỗi trong deleteMealFood: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Lỗi khi xóa món ăn khỏi bữa ăn: " + e.getMessage(), e);
        }
    }
    

    public void deleteMealFood(int mealId, int foodId) throws SQLException {
        try {
            // Trước tiên, lấy thông tin về món ăn để tính toán lại calo
            String sqlGetMealFood = "SELECT mf.quantity, f.calories " +
                                  "FROM meal_food mf " +
                                  "JOIN food f ON mf.foodId = f.idFood " +
                                  "WHERE mf.mealId = ? AND mf.foodId = ?";
            
            double foodCalories = 0;
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlGetMealFood)) {
                stmt.setInt(1, mealId);
                stmt.setInt(2, foodId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    int quantity = rs.getInt("quantity");
                    double calories = rs.getDouble("calories");
                    foodCalories = calories * quantity;
                } else {
                    // Không tìm thấy món ăn, không cần xóa
                    return;
                }
            }
            
            // Xóa món ăn khỏi bảng meal_food
            String sqlDeleteMealFood = "DELETE FROM meal_food WHERE mealId = ? AND foodId = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteMealFood)) {
                stmt.setInt(1, mealId);
                stmt.setInt(2, foodId);
                stmt.executeUpdate();
            }
            
            // Cập nhật tổng calories của bữa ăn
            String sqlUpdateCalories = "UPDATE meal " +
                                     "SET totalCalories = GREATEST(0, totalCalories - ?) " +
                                     "WHERE idMeal = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateCalories)) {
                stmt.setDouble(1, foodCalories);
                stmt.setInt(2, mealId);
                stmt.executeUpdate();
            }
            
            System.out.println("Đã xóa món ăn foodId=" + foodId + " khỏi bữa ăn mealId=" + mealId);
            
        } catch (Exception e) {
            System.err.println("Lỗi trong deleteMealFood: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Lỗi khi xóa món ăn khỏi bữa ăn: " + e.getMessage(), e);
        }
    }
    

    public void updateMealFood(int mealId, int foodId, int quantity, String unit) throws SQLException {
        try {
            // Lấy thông tin món ăn để tính lại calories
            String foodSql = "SELECT calories FROM food WHERE idFood = ?";
            double foodCalories = 0;
            
            try (PreparedStatement foodStmt = conn.prepareStatement(foodSql)) {
                foodStmt.setInt(1, foodId);
                try (ResultSet foodRs = foodStmt.executeQuery()) {
                    if (foodRs.next()) {
                        foodCalories = foodRs.getDouble("calories");
                    }
                }
            }
            
            // Lấy số lượng cũ để tính sự chênh lệch calories
            String oldQuantitySql = "SELECT quantity FROM meal_food WHERE mealId = ? AND foodId = ?";
            int oldQuantity = 0;
            
            try (PreparedStatement oldStmt = conn.prepareStatement(oldQuantitySql)) {
                oldStmt.setInt(1, mealId);
                oldStmt.setInt(2, foodId);
                try (ResultSet oldRs = oldStmt.executeQuery()) {
                    if (oldRs.next()) {
                        oldQuantity = oldRs.getInt("quantity");
                    }
                }
            }
            
            // Tính sự chênh lệch calories
            double caloriesDifference = (quantity - oldQuantity) * foodCalories;
            
            // Cập nhật thông tin món ăn trong bữa ăn
            String updateSql = "UPDATE meal_food SET quantity = ?, unit = ? WHERE mealId = ? AND foodId = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setInt(1, quantity);
                stmt.setString(2, unit);
                stmt.setInt(3, mealId);
                stmt.setInt(4, foodId);
                
                stmt.executeUpdate();
            }
            
            // Cập nhật tổng calories của bữa ăn
            String updateCalSql = "UPDATE meal SET totalCalories = totalCalories + ? WHERE idMeal = ?";
            
            try (PreparedStatement calStmt = conn.prepareStatement(updateCalSql)) {
                calStmt.setDouble(1, caloriesDifference);
                calStmt.setInt(2, mealId);
                
                calStmt.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Error in updateMealFood: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Error updating meal food: " + e.getMessage(), e);
        }
    }
    
    public void updateMealFoodComplete(int mealId, int foodId, String foodName, double quantity, String unit,
                                     double calories, double carb, double protein, double fat, 
                                     double sodium, double sugar) throws SQLException {
        try {
            // Lấy thông tin món ăn để tính lại calories
            String oldQuantitySql = "SELECT quantity FROM meal_food WHERE mealId = ? AND foodId = ?";
            double oldQuantity = 0;
            double oldCaloriesPerUnit = 0;
            
            try (PreparedStatement oldStmt = conn.prepareStatement(oldQuantitySql)) {
                oldStmt.setInt(1, mealId);
                oldStmt.setInt(2, foodId);
                try (ResultSet oldRs = oldStmt.executeQuery()) {
                    if (oldRs.next()) {
                        oldQuantity = oldRs.getDouble("quantity");
                    }
                }
            }
            
            // Lấy lượng calo cũ trên một đơn vị
            String oldFoodSql = "SELECT calories FROM food WHERE idFood = ?";
            try (PreparedStatement oldFoodStmt = conn.prepareStatement(oldFoodSql)) {
                oldFoodStmt.setInt(1, foodId);
                try (ResultSet oldFoodRs = oldFoodStmt.executeQuery()) {
                    if (oldFoodRs.next()) {
                        oldCaloriesPerUnit = oldFoodRs.getDouble("calories");
                    }
                }
            }
            
            // Tính tổng lượng calo cũ
            double oldTotalCalories = oldQuantity * oldCaloriesPerUnit;
            // Tính tổng lượng calo mới
            double newTotalCalories = quantity * calories;
            // Tính sự chênh lệch calories
            double caloriesDifference = newTotalCalories - oldTotalCalories;
            
            // Cập nhật thông tin thức ăn
            String updateFoodSql = "UPDATE food SET foodName = ?, calories = ?, carb = ?, protein = ?, fat = ?, sodium = ?, sugar = ? WHERE idFood = ?";
            
            try (PreparedStatement updateFoodStmt = conn.prepareStatement(updateFoodSql)) {
                updateFoodStmt.setString(1, foodName);
                updateFoodStmt.setDouble(2, calories);
                updateFoodStmt.setDouble(3, carb);
                updateFoodStmt.setDouble(4, protein);
                updateFoodStmt.setDouble(5, fat);
                updateFoodStmt.setDouble(6, sodium);
                updateFoodStmt.setDouble(7, sugar);
                updateFoodStmt.setInt(8, foodId);
                
                updateFoodStmt.executeUpdate();
            }
            
            // Cập nhật thông tin món ăn trong bữa ăn
            String updateMealFoodSql = "UPDATE meal_food SET quantity = ?, unit = ? WHERE mealId = ? AND foodId = ?";
            
            try (PreparedStatement updateMealFoodStmt = conn.prepareStatement(updateMealFoodSql)) {
                updateMealFoodStmt.setDouble(1, quantity);
                updateMealFoodStmt.setString(2, unit);
                updateMealFoodStmt.setInt(3, mealId);
                updateMealFoodStmt.setInt(4, foodId);
                
                updateMealFoodStmt.executeUpdate();
            }
            
            // Cập nhật tổng calories của bữa ăn
            String updateCalSql = "UPDATE meal SET totalCalories = totalCalories + ? WHERE idMeal = ?";
            
            try (PreparedStatement calStmt = conn.prepareStatement(updateCalSql)) {
                calStmt.setDouble(1, caloriesDifference);
                calStmt.setInt(2, mealId);
                
                calStmt.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Error in updateMealFoodComplete: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Error updating meal food: " + e.getMessage(), e);
        }
    }
    

    private Food getFoodById(int foodId) throws SQLException {
        Food food = null;
        
        try {
            String sql = "SELECT idFood, foodName, calories, protein, carb, fat, sodium, sugar " +
                         "FROM food " +
                         "WHERE idFood = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, foodId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    food = new Food();
                    food.setIdFood(rs.getInt("idFood"));
                    food.setFoodName(rs.getString("foodName"));
                    food.setCalories(rs.getDouble("calories"));
                    food.setProtein(rs.getObject("protein") != null ? rs.getDouble("protein") : null);
                    food.setCarb(rs.getObject("carb") != null ? rs.getDouble("carb") : null);
                    food.setFat(rs.getObject("fat") != null ? rs.getDouble("fat") : null);
                    food.setSodium(rs.getObject("sodium") != null ? rs.getDouble("sodium") : null);
                    food.setSugar(rs.getObject("sugar") != null ? rs.getDouble("sugar") : null);
                    
                    System.out.println("Retrieved food: " + food.getFoodName() + 
                                       ", Sodium=" + food.getSodium() + 
                                       ", Sugar=" + food.getSugar());
                }
            }
            
            return food;
        } catch (Exception e) {
            System.err.println("Error in getFoodById: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Error retrieving food: " + e.getMessage(), e);
        }
    }

    public List<Meal> getMealsByUserAndToday(int userId, Date date) throws SQLException {
        List<Meal> meals = new ArrayList<>();
        
        try {
            // Format the date to SQL date format
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = dateFormat.format(date);
            
            System.out.println("Searching meals for user ID: " + userId + " on date: " + dateStr);
            
            // Query to get ALL meals for user UP TO the specified date - using <= instead of =
            String sql = "SELECT idMeal, nameMeal, totalCalories, dateOfMeal " +
                        "FROM meal " +
                        "WHERE userId = ? AND DATE(dateOfMeal) <= ?";
            
            System.out.println("SQL query: " + sql);
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, dateStr);
                
                System.out.println("Executing query with parameters: userId=" + userId + ", date=" + dateStr);
                
                ResultSet rs = stmt.executeQuery();
                int count = 0;
                
                while (rs.next()) {
                    count++;
                    Meal meal = new Meal();
                    meal.setIdMeal(rs.getInt("idMeal"));
                    meal.setNameMeal(rs.getString("nameMeal"));
                    meal.setTotalCalories(rs.getDouble("totalCalories"));
                    meal.setDateOfMeal(rs.getTimestamp("dateOfMeal"));
                    
                    System.out.println("Found meal: ID=" + meal.getIdMeal() + ", Name=" + meal.getNameMeal() + 
                                      ", Date=" + dateFormat.format(meal.getDateOfMeal()));
                    
                    // Set user ID
                    User user = new User();
                    user.setId(userId);
                    meal.setUserId(user);
                    
                    // Load food items for this meal
                    loadMealFoods(meal);
                    
                    meals.add(meal);
                }
                
                System.out.println("Total meals found: " + count);
            }
            
            return meals;
        } catch (Exception e) {
            System.err.println("Error in getMealsByUserAndDate: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Error retrieving meals: " + e.getMessage(), e);
        }
    }
}