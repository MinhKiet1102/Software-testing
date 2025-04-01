package com.milkyway.service;

import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.Target;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TargetService {

    public boolean isPlanExist(String planName) throws SQLException {
        String checkPlan = "SELECT targetName FROM target WHERE targetName = ?";
        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(checkPlan)) {
            prepare.setString(1, planName);
            ResultSet result = prepare.executeQuery();
            return result.next();
        }
    }

    public void addPlan(String planName, LocalDate startDate, LocalDate endDate, float targetValue, String unit, int userId) throws SQLException {
        String insertData = "INSERT INTO target (targetName, dateCreated, startDate, endDate, targetNumber, unit, progress, status, userId) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(insertData)) {
            Date date = new Date();
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            prepare.setString(1, planName);
            prepare.setDate(2, sqlDate);
            prepare.setString(3, String.valueOf(startDate));
            prepare.setString(4, String.valueOf(endDate));
            prepare.setFloat(5, targetValue);
            String unitBeforeSpace = unit.split(" ")[0];
            prepare.setString(6, unitBeforeSpace);
            prepare.setFloat(7, 0.0f);
            prepare.setString(8, "Not Started");
            prepare.setInt(9, userId);

            prepare.executeUpdate();
        }
    }

    public LocalDate getOldEndDate(int idTarget) throws SQLException {
        String checkData = "SELECT endDate FROM target WHERE idTarget = ?";
        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(checkData)) {
            prepare.setInt(1, idTarget);
            ResultSet result = prepare.executeQuery();
            if (result.next()) {
                return LocalDate.parse(result.getString("endDate").split(" ")[0]);
            }
        }
        return null;
    }

    public String getDateCreated(int idTarget) throws SQLException {
        String checkData = "SELECT dateCreated FROM target WHERE idTarget = ?";
        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(checkData)) {
            prepare.setInt(1, idTarget);
            ResultSet result = prepare.executeQuery();
            if (result.next()) {
                return result.getString("dateCreated");
            }
        }
        return null;
    }

    public void updatePlan(int idTarget, String planName, LocalDate startDate, LocalDate endDate, String dateCreated, float targetValue, String unit) throws SQLException {
        String updateData = "UPDATE target SET targetName = ?, startDate = ?, endDate = ?, dateCreated = ?, targetNumber = ?, unit = ? WHERE idTarget = ?";
        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(updateData)) {
            prepare.setString(1, planName);
            prepare.setString(2, String.valueOf(startDate));
            prepare.setString(3, String.valueOf(endDate));
            prepare.setString(4, dateCreated);
            prepare.setFloat(5, targetValue);
            String unitBeforeSpace = unit.split(" ")[0];
            prepare.setString(6, unitBeforeSpace);
            prepare.setInt(7, idTarget);

            prepare.executeUpdate();
        }
    }

    public void updatePlanProgress(int idTarget, float newProgress) throws SQLException {
        String sql = "UPDATE target SET progress = ? WHERE idTarget = ?";
        try (Connection connect = JdbcUtils.getConn(); PreparedStatement stmt = connect.prepareStatement(sql)) {
            stmt.setFloat(1, newProgress);
            stmt.setInt(2, idTarget);
            stmt.executeUpdate();
        }
    }

    public ObservableList<Target> getPlansForCurrentUser(int userId) throws SQLException {
        ObservableList<Target> listData = FXCollections.observableArrayList();
        String selectData = "SELECT * FROM target WHERE userId = ?";

        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(selectData)) {
            prepare.setInt(1, userId);
            ResultSet result = prepare.executeQuery();

            while (result.next()) {
                Target pData = new Target(
                        result.getInt("idTarget"),
                        result.getString("targetName"),
                        result.getDate("dateCreated"),
                        result.getDate("startDate"),
                        result.getDate("endDate"),
                        result.getFloat("targetNumber"),
                        result.getString("unit"),
                        result.getFloat("progress"),
                        result.getString("status")
                );

                listData.add(pData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return listData;
    }

    public boolean isPlanExist(int idTarget) throws SQLException {
        String checkData = "SELECT * FROM target WHERE idTarget = ?";
        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(checkData)) {
            prepare.setInt(1, idTarget);
            ResultSet result = prepare.executeQuery();
            return result.next();
        }
    }

    public void deletePlan(int idTarget) throws SQLException {
        String deleteData = "DELETE FROM target WHERE idTarget = ?";
        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(deleteData)) {
            prepare.setInt(1, idTarget);
            prepare.executeUpdate();
        }
    }

    public int countQuantityPlans(int userId) throws SQLException {
        String sql = "SELECT COUNT(idTarget) FROM target WHERE userId = ?";
        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(sql)) {
            prepare.setInt(1, userId);
            ResultSet result = prepare.executeQuery();

            if (result.next()) {
                return result.getInt(1); 
            }
        }
        return 0; 
    }

    public int countAchievedPlans(int userId) throws SQLException {
        String sql = "SELECT COUNT(idTarget) FROM target WHERE userId = ? AND status = 'Achieved'";
        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(sql)) {
            prepare.setInt(1, userId);
            ResultSet result = prepare.executeQuery();

            if (result.next()) {
                return result.getInt(1);
            }
        }
        return 0;
    }

    public Target getPlanById(int idTarget) throws SQLException {
        String selectData = "SELECT * FROM target WHERE idTarget = ?";
        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(selectData)) {
            prepare.setInt(1, idTarget);
            ResultSet result = prepare.executeQuery();

            if (result.next()) {
                return new Target(
                        result.getInt("idTarget"),
                        result.getString("targetName"),
                        result.getDate("dateCreated"),
                        result.getDate("startDate"),
                        result.getDate("endDate"),
                        result.getFloat("targetNumber"),
                        result.getString("unit"),
                        result.getFloat("progress"),
                        result.getString("status")
                );
            }
        }
        return null;
    }

    public void updatePlanStatus(int idTarget, String status) throws SQLException {
        String updateData = "UPDATE target SET status = ? WHERE idTarget = ?";
        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(updateData)) {
            prepare.setString(1, status);
            prepare.setInt(2, idTarget);
            prepare.executeUpdate();
        }
    }

    public List<String> getStatusList() throws SQLException {
        List<String> statusList = new ArrayList<>();
        String sql = "SHOW COLUMNS FROM target LIKE 'status'";

        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(sql)) {
            ResultSet result = prepare.executeQuery();

            if (result.next()) {
                String enumStr = result.getString("Type");
                enumStr = enumStr.replace("enum(", "").replace(")", "").replace("'", "");
                statusList.addAll(Arrays.asList(enumStr.split(",")));
            }
        }
        return statusList;
    }

    public ObservableList<Target> getFinishedPlansDataList(int userId) throws SQLException {
        ObservableList<Target> listData = FXCollections.observableArrayList();
        String selectData = "SELECT * FROM target WHERE userId = ?";

        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(selectData)) {
            prepare.setInt(1, userId);
            ResultSet result = prepare.executeQuery();

            while (result.next()) {
                Target pData = new Target(
                        result.getInt("idTarget"),
                        result.getString("targetName"),
                        result.getDate("dateCreated"),
                        result.getDate("startDate"),
                        result.getDate("endDate"),
                        result.getFloat("targetNumber"),
                        result.getString("unit"),
                        result.getFloat("progress"),
                        result.getString("status")
                );
                listData.add(pData);
            }
        }
        return listData;
    }
}
