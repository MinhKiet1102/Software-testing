/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.service;

import com.milkyway.pojo.History;
import com.milkyway.pojo.JdbcUtils;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Admin
 */
public class HistoryService {

    public void save(History history) throws SQLException {
        Connection con = JdbcUtils.getConn();
        if (con == null) {
            return;
        }

        // Kiểm tra xem bản ghi có cùng ngày và user_id đã tồn tại chưa
        if (recordExists(con, history.getUserId().getId(), history.getHistoryDate())) {
            updateWeight(con, history);
        } else {
            createRecord(con, history);
        }
    }

    private boolean recordExists(Connection con, int userId, Date historyDate) {
        String query = "SELECT COUNT(*) FROM history WHERE user_id=? AND history_date=?";
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setDate(2, new java.sql.Date(historyDate.getTime()));

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return false;
    }

    // Helper method to update the weight if a record with the same date and user_id exists
    private void updateWeight(Connection con, History history) {
        String query = "UPDATE history SET history_weight=? WHERE user_id=? AND history_date=?";
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setBigDecimal(1, history.getHistoryWeight());
            preparedStatement.setInt(2, history.getUserId().getId());
            preparedStatement.setDate(3, new java.sql.Date(history.getHistoryDate().getTime()));

            preparedStatement.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    private void createRecord(Connection con, History history) {
        String query = "INSERT INTO history (history_date, history_weight, user_id, history_height) VALUES (?, ?, ?, ?);";
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setDate(1, new java.sql.Date(history.getHistoryDate().getTime()));
            preparedStatement.setBigDecimal(2, history.getHistoryWeight());
            preparedStatement.setInt(3, history.getUserId().getId());
            preparedStatement.setInt(4, history.getHistoryHeight());
            preparedStatement.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public void updateWeight(int historyId, BigDecimal newWeight) throws SQLException {
        Connection con = JdbcUtils.getConn();
        if (con == null) {
            return;
        }

        String query = "UPDATE history SET history_weight=? WHERE history_id=?;";

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setBigDecimal(1, newWeight);
            preparedStatement.setInt(2, historyId);
            preparedStatement.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public List<History> findAllByUserId(int userId) throws SQLException {
        Connection con = JdbcUtils.getConn();
        if (con == null) {
            return null;
        }

        List<History> histories = new ArrayList<>();
        String query = "SELECT * FROM history WHERE user_id = ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                History history = new History(
                        resultSet.getInt("history_id"),
                        resultSet.getDate("history_date"),
                        resultSet.getBigDecimal("history_weight"),
                        resultSet.getInt("history_height") // Thêm height
                );
                histories.add(history);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return histories;
    }

    public History findLatestHistoryByUserId(int userId) throws SQLException {
        Connection con = JdbcUtils.getConn();
        if (con == null) {
            return null;
        }

        String query = "SELECT * FROM history WHERE user_id=? ORDER BY history_date DESC LIMIT 1;";
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new History(resultSet.getInt("history_id"),
                        resultSet.getDate("history_date"),
                        resultSet.getBigDecimal("history_weight"),
                        resultSet.getInt("history_height")
                );
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        return null;
    }
}
