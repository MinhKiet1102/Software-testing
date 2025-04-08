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
            return;
        }

        String query = "UPDATE user SET username=? WHERE id=?;";

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setString(1, newUsername);
            preparedStatement.setInt(2, userId);
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
    
    public void updatePassword(int userId, String newPassword) throws SQLException {
        Connection con = JdbcUtils.getConn();
        if (con == null) {
            return;
        }

        String query = "UPDATE user SET password=? WHERE id=?;";

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setString(1, newPassword);
            preparedStatement.setInt(2, userId);
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
    
    public void updateWeight(int userId, BigDecimal newWeight) throws SQLException {
        Connection con = JdbcUtils.getConn();
        if (con == null) {
            return;
        }

        String query = "UPDATE user SET current_weight=? WHERE id=?;";

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setBigDecimal(1, newWeight);
            preparedStatement.setInt(2, userId);
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

    public void updateHeight(int userId, int newHeight) throws SQLException {
        Connection con = JdbcUtils.getConn();
        if (con == null) {
            return;
        }

        String query = "UPDATE user SET height=? WHERE id=?;";

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, newHeight);
            preparedStatement.setInt(2, userId);
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
}
