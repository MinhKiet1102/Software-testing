package com.milkyway.service;

import com.milkyway.pojo.Exerciselog;
import com.milkyway.pojo.JdbcUtils;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 *
 * @author ASUS
 */
public class ExerciseLogService {

    public boolean saveLog(Exerciselog log) throws SQLException {
        if (log == null || log.getExerciseId() == null || log.getUserId() == null) {
            System.err.println("Lỗi: Dữ liệu Exerciselog không hợp lệ (thiếu thông tin cần thiết).");
            return false;
        }

        String sql = "INSERT INTO exerciselog (effortLevel, duration, datetime, energyBurn, exerciseId, userId) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = JdbcUtils.getConn(); PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, log.getEffortLevel());
            stm.setInt(2, log.getDuration());
            stm.setDate(3, new Date(log.getDatetime().getTime()));
            stm.setDouble(4, log.getEnergyBurn());
            stm.setInt(5, log.getExerciseId().getIdExercise());
            stm.setInt(6, log.getUserId().getId());

            int rowsAffected = stm.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public void deleteExerciseLog(int logId) throws SQLException {
        String sql = "DELETE FROM exerciselog WHERE idExLog=?";

        try (Connection conn = JdbcUtils.getConn(); PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, logId);
            stm.executeUpdate();
        }
    }

    public List<Exerciselog> getExercisesByUser(int userId, String keyword) throws SQLException {
        List<Exerciselog> list = new ArrayList<>();
//        String query = "SELECT e FROM Exerciselog e WHERE e.userId.id = :userId";
//
//        if (keyword != null && !keyword.isEmpty()) {
//            query += " AND e.exerciseId.exerciseName LIKE :keyword";
//        }
//
//        EntityManager em = JpaUtil.getEntityManager();
//        TypedQuery<Exerciselog> q = em.createQuery(query, Exerciselog.class);
//        q.setParameter("userId", userId);
//
//        if (keyword != null && !keyword.isEmpty()) {
//            q.setParameter("keyword", "%" + keyword + "%");
//        }
//
//        list = q.getResultList();
//        em.close();
        return list;
    }

}
