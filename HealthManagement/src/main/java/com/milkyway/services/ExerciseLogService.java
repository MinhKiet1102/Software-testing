package com.milkyway.services;

import com.milkyway.healthmanagement.SwitchSceneController;
import com.milkyway.pojo.Exercise;
import com.milkyway.pojo.Exerciselog;
import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ExerciseLogService extends SwitchSceneController {

    public boolean saveLog(Exerciselog log) throws SQLException {
        if (log == null || log.getExerciseId() == null || log.getUserId() == null || log.getDatetime() == null || log.getDuration() <= 0) {
            return false;
        }

        String sql = "INSERT INTO exerciselog (effortLevel, duration, datetime, energyBurn, exerciseId, userId) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = JdbcUtils.getConn();
            PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, log.getEffortLevel());
            stm.setInt(2, (int)log.getDuration());
            stm.setDate(3, new java.sql.Date(log.getDatetime().getTime()));
            stm.setDouble(4, log.getEnergyBurn());
            stm.setInt(5, log.getExerciseId().getIdExercise()); 
            stm.setInt(6, log.getUserId().getId());           

            int rowsAffected = stm.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            Logger.getLogger(ExerciseLogService.class.getName()).log(Level.SEVERE, "Lỗi lưu ExerciseLog", ex);
            throw ex; 
        }
    }

    /**
     * Cập nhật thông tin nhật ký bài tập
     * @param log Đối tượng log chứa thông tin cần cập nhật
     * @return true nếu cập nhật thành công, false nếu thất bại
     * @throws SQLException nếu có lỗi xảy ra khi tương tác với CSDL
     */
    public boolean updateLog(Exerciselog log) throws SQLException {
        if (log == null || log.getIdExLog() == null || log.getExerciseId() == null 
                || log.getUserId() == null || log.getDatetime() == null || log.getDuration() <= 0) {
            return false;
        }

        String sql = "UPDATE exerciselog SET effortLevel = ?, duration = ?, datetime = ?, " +
                    "energyBurn = ?, exerciseId = ?, userId = ? WHERE idExLog = ?";

        try (Connection conn = JdbcUtils.getConn();
            PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, log.getEffortLevel());
            stm.setInt(2, (int)log.getDuration());
            stm.setDate(3, new java.sql.Date(log.getDatetime().getTime()));
            stm.setDouble(4, log.getEnergyBurn());
            stm.setInt(5, log.getExerciseId().getIdExercise()); 
            stm.setInt(6, log.getUserId().getId());           
            stm.setInt(7, log.getIdExLog());

            int rowsAffected = stm.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            Logger.getLogger(ExerciseLogService.class.getName()).log(Level.SEVERE, 
                    "Lỗi cập nhật ExerciseLog ID: " + log.getIdExLog(), ex);
            throw ex; 
        }
    }

    public void deleteExerciseLog(int logId) throws SQLException {
        String sql = "DELETE FROM exerciselog WHERE idExLog=?";
        try (Connection conn = JdbcUtils.getConn();
            PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, logId);
            stm.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ExerciseLogService.class.getName()).log(Level.SEVERE, "Lỗi xóa ExerciseLog ID: " + logId, ex);
            throw ex;
        }
    }

    public List<Exerciselog> getExerciseLogsByUserAndDate(int userId, Date filterDate) throws SQLException {
        List<Exerciselog> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stm = null;
        ResultSet rs = null;

        // Xây dựng SQL
        StringBuilder sqlBuilder = new StringBuilder("SELECT el.idExLog, el.effortLevel, el.duration, el.datetime, el.energyBurn, ");
        sqlBuilder.append("e.idExercise, e.exerciseName, e.caloriesBurnedPerMin, "); 
        sqlBuilder.append("u.id ");
        sqlBuilder.append("FROM exerciselog el ");
        sqlBuilder.append("JOIN exercise e ON el.exerciseId = e.idExercise ");
        sqlBuilder.append("JOIN user u ON el.userId = u.id ");
        sqlBuilder.append("WHERE el.userId = ? ");

        boolean hasDateFilter = filterDate != null;
        if (hasDateFilter) {
            // Hàm DATE() cho MySQL, điều chỉnh nếu dùng CSDL khác
            sqlBuilder.append("AND DATE(el.datetime) = ? ");
        }
        sqlBuilder.append("ORDER BY el.datetime DESC");
        String sql = sqlBuilder.toString();

        try {
            conn = JdbcUtils.getConn();
             if (conn == null) throw new SQLException("Không thể kết nối CSDL.");
            stm = conn.prepareStatement(sql);

            int paramIndex = 1;
            stm.setInt(paramIndex++, userId);

            if (hasDateFilter) {
                // Dùng java.sql.Date cho setDate
                stm.setDate(paramIndex++, new java.sql.Date(filterDate.getTime()));
            }

            rs = stm.executeQuery();

            while (rs.next()) {
                // Tạo Exercise
                Exercise ex = new Exercise();
                ex.setIdExercise(rs.getInt("idExercise"));
                ex.setExerciseName(rs.getString("exerciseName"));
                 if (hasColumn(rs, "caloriesBurnedPerMin")) {
                     ex.setCaloriesBurnedPerMin(rs.getDouble("caloriesBurnedPerMin"));
                 }
                // Tạo User
                User u = new User(rs.getInt("id"));
                // Tạo Exerciselog
                Exerciselog log = new Exerciselog();
                log.setIdExLog(rs.getInt("idExLog"));
                log.setEffortLevel(rs.getString("effortLevel"));
                log.setDuration(rs.getInt("duration"));
                // Lấy Timestamp và chuyển về java.util.Date cho POJO
                log.setDatetime(new Date(rs.getTimestamp("datetime").getTime()));
                log.setEnergyBurn(rs.getDouble("energyBurn"));
                log.setExerciseId(ex);
                log.setUserId(u);

                list.add(log);
            }
        } finally {
            // Đóng tài nguyên
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (stm != null) try { stm.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }
        return list;
    }

    /**
     * Lấy danh sách nhật ký bài tập của người dùng trong khoảng thời gian
     * @param userId ID của người dùng cần lấy nhật ký
     * @param startDate Ngày bắt đầu khoảng thời gian
     * @param endDate Ngày kết thúc khoảng thời gian
     * @return Danh sách các nhật ký bài tập trong khoảng thời gian
     */
    public List<Exerciselog> getExerciseLogsByDateRange(int userId, Date startDate, Date endDate) {
        List<Exerciselog> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stm = null;
        ResultSet rs = null;

        // Xây dựng SQL
        StringBuilder sqlBuilder = new StringBuilder("SELECT el.idExLog, el.effortLevel, el.duration, el.datetime, el.energyBurn, ");
        sqlBuilder.append("e.idExercise, e.exerciseName, e.caloriesBurnedPerMin, "); 
        sqlBuilder.append("u.id ");
        sqlBuilder.append("FROM exerciselog el ");
        sqlBuilder.append("JOIN exercise e ON el.exerciseId = e.idExercise ");
        sqlBuilder.append("JOIN user u ON el.userId = u.id ");
        sqlBuilder.append("WHERE el.userId = ? ");
        sqlBuilder.append("AND el.datetime >= ? ");
        sqlBuilder.append("AND el.datetime <= ? ");
        sqlBuilder.append("ORDER BY el.datetime");
        
        String sql = sqlBuilder.toString();

        try {
            conn = JdbcUtils.getConn();
            if (conn == null) throw new SQLException("Không thể kết nối CSDL.");
            stm = conn.prepareStatement(sql);

            stm.setInt(1, userId);
            stm.setDate(2, new java.sql.Date(startDate.getTime()));
            stm.setDate(3, new java.sql.Date(endDate.getTime()));

            rs = stm.executeQuery();

            while (rs.next()) {
                // Tạo Exercise
                Exercise ex = new Exercise();
                ex.setIdExercise(rs.getInt("idExercise"));
                ex.setExerciseName(rs.getString("exerciseName"));
                if (hasColumn(rs, "caloriesBurnedPerMin")) {
                    ex.setCaloriesBurnedPerMin(rs.getDouble("caloriesBurnedPerMin"));
                }
                
                // Tạo User
                User u = new User(rs.getInt("id"));
                
                // Tạo Exerciselog
                Exerciselog log = new Exerciselog();
                log.setIdExLog(rs.getInt("idExLog"));
                log.setEffortLevel(rs.getString("effortLevel"));
                log.setDuration(rs.getInt("duration"));
                // Lấy Timestamp và chuyển về java.util.Date cho POJO
                log.setDatetime(new Date(rs.getTimestamp("datetime").getTime()));
                log.setEnergyBurn(rs.getDouble("energyBurn"));
                log.setExerciseId(ex);
                log.setUserId(u);

                list.add(log);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ExerciseLogService.class.getName()).log(Level.SEVERE, 
                    "Lỗi lấy ExerciseLogs trong khoảng thời gian", ex);
        } finally {
            // Đóng tài nguyên
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (stm != null) try { stm.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }
        return list;
    }

     // Hàm tiện ích kiểm tra cột
     private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException sqlex) {
            return false;
        }
     }
}