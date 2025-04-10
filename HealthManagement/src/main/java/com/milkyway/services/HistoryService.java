/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.services;

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
        try (Connection con = JdbcUtils.getConn()) {
            if (con == null) {
                return;
            }

            // Kiểm tra xem bản ghi đã tồn tại chưa
            if (recordExists(con, history.getUserId().getId(), history.getHistoryDate())) {
                updateWeight(con, history); // Cập nhật bản ghi cũ
            } else {
                createRecord(con, history); // Tạo bản ghi mới
            }
        }
    }

    private boolean recordExists(Connection con, int userId, Date historyDate) throws SQLException {
        String query = "SELECT COUNT(*) FROM history WHERE user_id=? AND history_date=?";
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setDate(2, new java.sql.Date(historyDate.getTime()));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    private void updateWeight(Connection con, History history) throws SQLException {
        String query = "UPDATE history SET history_weight=?, history_height=? WHERE user_id=? AND history_date=?";
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setBigDecimal(1, history.getHistoryWeight());
            preparedStatement.setInt(2, history.getHistoryHeight());
            preparedStatement.setInt(3, history.getUserId().getId());
            preparedStatement.setDate(4, new java.sql.Date(history.getHistoryDate().getTime()));
            preparedStatement.executeUpdate();
        }
    }

    private void createRecord(Connection con, History history) throws SQLException {
        String query = "INSERT INTO history (history_date, history_weight, history_height, user_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setDate(1, new java.sql.Date(history.getHistoryDate().getTime()));
            preparedStatement.setBigDecimal(2, history.getHistoryWeight());
            preparedStatement.setInt(3, history.getHistoryHeight());
            preparedStatement.setInt(4, history.getUserId().getId());
            preparedStatement.executeUpdate();
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
