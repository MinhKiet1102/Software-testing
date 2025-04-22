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

    /**
     * Lưu lịch sử sức khỏe mới hoặc cập nhật dữ liệu đã tồn tại
     * @param history Đối tượng lịch sử cần lưu
     * @throws SQLException nếu có lỗi xảy ra khi tương tác với cơ sở dữ liệu
     */
    public void save(History history) throws SQLException {
        Connection con = null;
        
        try {
            con = JdbcUtils.getConn();
            if (con == null) {
                throw new SQLException("Không thể kết nối đến cơ sở dữ liệu");
            }

            // Kiểm tra xem bản ghi đã tồn tại chưa
            if (recordExists(con, history.getUserId().getId(), history.getHistoryDate())) {
                updateWeight(con, history); // Cập nhật bản ghi cũ
            } else {
                createRecord(con, history); // Tạo bản ghi mới
            }
        } catch (SQLException ex) {
            throw ex;
        }
        // Không đóng kết nối ở đây để tránh lỗi "object is already closed"
    }

    /**
     * Kiểm tra xem bản ghi lịch sử đã tồn tại chưa
     * @param con Kết nối cơ sở dữ liệu
     * @param userId ID người dùng
     * @param historyDate Ngày lịch sử
     * @return true nếu bản ghi đã tồn tại, false nếu chưa
     * @throws SQLException nếu có lỗi xảy ra khi tương tác với cơ sở dữ liệu
     */
    private boolean recordExists(Connection con, int userId, Date historyDate) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            String query = "SELECT COUNT(*) FROM history WHERE user_id=? AND history_date=?";
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            preparedStatement.setDate(2, new java.sql.Date(historyDate.getTime()));
            resultSet = preparedStatement.executeQuery();
            return resultSet.next() && resultSet.getInt(1) > 0;
        } finally {
            if (resultSet != null) try { resultSet.close(); } catch (SQLException e) { }
            if (preparedStatement != null) try { preparedStatement.close(); } catch (SQLException e) { }
        }
    }

    /**
     * Cập nhật thông tin lịch sử đã tồn tại
     * @param con Kết nối cơ sở dữ liệu
     * @param history Đối tượng lịch sử cần cập nhật
     * @throws SQLException nếu có lỗi xảy ra khi tương tác với cơ sở dữ liệu
     */
    private void updateWeight(Connection con, History history) throws SQLException {
        PreparedStatement preparedStatement = null;
        
        try {
            String query = "UPDATE history SET history_weight=?, history_height=? WHERE user_id=? AND history_date=?";
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setBigDecimal(1, history.getHistoryWeight());
            preparedStatement.setInt(2, history.getHistoryHeight());
            preparedStatement.setInt(3, history.getUserId().getId());
            preparedStatement.setDate(4, new java.sql.Date(history.getHistoryDate().getTime()));
            preparedStatement.executeUpdate();
        } finally {
            if (preparedStatement != null) try { preparedStatement.close(); } catch (SQLException e) { }
        }
    }

    /**
     * Tạo bản ghi lịch sử mới
     * @param con Kết nối cơ sở dữ liệu
     * @param history Đối tượng lịch sử cần tạo
     * @throws SQLException nếu có lỗi xảy ra khi tương tác với cơ sở dữ liệu
     */
    private void createRecord(Connection con, History history) throws SQLException {
        PreparedStatement preparedStatement = null;
        
        try {
            String query = "INSERT INTO history (history_date, history_weight, history_height, user_id) VALUES (?, ?, ?, ?)";
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setDate(1, new java.sql.Date(history.getHistoryDate().getTime()));
            preparedStatement.setBigDecimal(2, history.getHistoryWeight());
            preparedStatement.setInt(3, history.getHistoryHeight());
            preparedStatement.setInt(4, history.getUserId().getId());
            preparedStatement.executeUpdate();
        } finally {
            if (preparedStatement != null) try { preparedStatement.close(); } catch (SQLException e) { }
        }
    }

    /**
     * Lấy tất cả lịch sử sức khỏe của người dùng, sắp xếp theo ngày giảm dần
     * @param userId ID người dùng cần lấy lịch sử
     * @return Danh sách lịch sử sức khỏe
     * @throws SQLException nếu có lỗi xảy ra khi tương tác với cơ sở dữ liệu
     */
    public List<History> findAllByUserId(int userId) throws SQLException {
        Connection con = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<History> histories = new ArrayList<>();
        
        try {
            con = JdbcUtils.getConn();
            if (con == null) {
                throw new SQLException("Không thể kết nối đến cơ sở dữ liệu");
            }

            // Sắp xếp theo thứ tự giảm dần của ngày (mới nhất trước)
            String query = "SELECT * FROM history WHERE user_id = ? ORDER BY history_date DESC";
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                History history = new History(
                        resultSet.getInt("history_id"),
                        resultSet.getDate("history_date"),
                        resultSet.getBigDecimal("history_weight"),
                        resultSet.getInt("history_height")
                );
                histories.add(history);
            }
            
            return histories;
        } catch (SQLException ex) {
            throw ex;
        } finally {
            if (resultSet != null) try { resultSet.close(); } catch (SQLException e) { }
            if (preparedStatement != null) try { preparedStatement.close(); } catch (SQLException e) { }
            // Không đóng kết nối ở đây để tránh lỗi "object is already closed"
        }
    }

    /**
     * Lấy lịch sử sức khỏe mới nhất của người dùng
     * @param userId ID người dùng cần lấy lịch sử
     * @return Đối tượng lịch sử sức khỏe mới nhất, hoặc null nếu không tìm thấy
     * @throws SQLException nếu có lỗi xảy ra khi tương tác với cơ sở dữ liệu
     */
    public History findLatestHistoryByUserId(int userId) throws SQLException {
        Connection con = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            con = JdbcUtils.getConn();
            if (con == null) {
                throw new SQLException("Không thể kết nối đến cơ sở dữ liệu");
            }

            String query = "SELECT * FROM history WHERE user_id=? ORDER BY history_date DESC LIMIT 1";
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                return new History(
                    resultSet.getInt("history_id"),
                    resultSet.getDate("history_date"),
                    resultSet.getBigDecimal("history_weight"),
                    resultSet.getInt("history_height")
                );
            }
            
            return null;
        } catch (SQLException ex) {
            throw ex;
        } finally {
            if (resultSet != null) try { resultSet.close(); } catch (SQLException e) { }
            if (preparedStatement != null) try { preparedStatement.close(); } catch (SQLException e) { }
            // Không đóng kết nối ở đây để tránh lỗi "object is already closed"
        }
    }
}
