/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.services;

import com.milkyway.pojo.JdbcUtils;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author Admin
 */
public class PersonalInforService {
    
    public void updateUsername(int userId, String newUsername) throws SQLException {
        Connection con = JdbcUtils.getConn();
        if (con == null) {
            throw new SQLException("Không thể thiết lập kết nối cơ sở dữ liệu");
        }

        String query = "UPDATE user SET username=? WHERE id=?;";

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setString(1, newUsername);
            preparedStatement.setInt(2, userId);
            int rowsAffected = preparedStatement.executeUpdate();
            
            // Ensure commit happened
            if (!con.getAutoCommit()) {
                con.commit();
            }
            
            if (rowsAffected == 0) {
                // No rows were affected, which might indicate an issue
                System.out.println("Warning: No rows updated when updating username for user ID " + userId);
            }
        } catch (SQLException e) {
            System.err.println("Error updating username: " + e.getMessage());
            throw e;
        } finally {
            // Only close the connection if it's not a test connection
            if (con != null && !con.isClosed() && !JdbcUtils.isTestConnection(con)) {
                con.close();
            }
        }
    }
    
    public void updatePassword(int userId, String newPassword) throws SQLException {
        Connection con = JdbcUtils.getConn();
        if (con == null) {
            throw new SQLException("Không thể thiết lập kết nối cơ sở dữ liệu");
        }

        String query = "UPDATE user SET password=? WHERE id=?;";

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setString(1, newPassword);
            preparedStatement.setInt(2, userId);
            int rowsAffected = preparedStatement.executeUpdate();
            
            // Ensure commit happened
            if (!con.getAutoCommit()) {
                con.commit();
            }
            
            if (rowsAffected == 0) {
                // No rows were affected, which might indicate an issue
                System.out.println("Warning: No rows updated when updating password for user ID " + userId);
            }
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            throw e;
        } finally {
            // Only close the connection if it's not a test connection
            if (con != null && !con.isClosed() && !JdbcUtils.isTestConnection(con)) {
                con.close();
            }
        }
    }
    
    public void updateWeight(int userId, BigDecimal newWeight) throws SQLException {
        Connection con = JdbcUtils.getConn();
        if (con == null) {
            throw new SQLException("Không thể thiết lập kết nối cơ sở dữ liệu");
        }

        String query = "UPDATE user SET current_weight=? WHERE id=?;";

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setBigDecimal(1, newWeight);
            preparedStatement.setInt(2, userId);
            int rowsAffected = preparedStatement.executeUpdate();
            
            // Ensure commit happened
            if (!con.getAutoCommit()) {
                con.commit();
            }
            
            if (rowsAffected == 0) {
                // No rows were affected, which might indicate an issue
                System.out.println("Warning: No rows updated when updating weight for user ID " + userId);
            }
        } catch (SQLException e) {
            System.err.println("Error updating weight: " + e.getMessage());
            throw e;
        } finally {
            // Only close the connection if it's not a test connection
            // This prevents closing the H2 in-memory database connection during tests
            if (con != null && !con.isClosed() && !JdbcUtils.isTestConnection(con)) {
                con.close();
            }
        }
    }

    public void updateHeight(int userId, int newHeight) throws SQLException {
        Connection con = JdbcUtils.getConn();
        if (con == null) {
            throw new SQLException("Không thể thiết lập kết nối cơ sở dữ liệu");
        }

        String query = "UPDATE user SET height=? WHERE id=?;";

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, newHeight);
            preparedStatement.setInt(2, userId);
            int rowsAffected = preparedStatement.executeUpdate();
            
            // Ensure commit happened
            if (!con.getAutoCommit()) {
                con.commit();
            }
            
            if (rowsAffected == 0) {
                // No rows were affected, which might indicate an issue
                System.out.println("Warning: No rows updated when updating height for user ID " + userId);
            }
        } catch (SQLException e) {
            System.err.println("Error updating height: " + e.getMessage());
            throw e;
        } finally {
            // Only close the connection if it's not a test connection
            if (con != null && !con.isClosed() && !JdbcUtils.isTestConnection(con)) {
                con.close();
            }
        }
    }
}
