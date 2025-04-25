package com.milkyway.services;

import com.milkyway.healthmanagement.SwitchSceneController;
import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.Target;
import com.milkyway.pojo.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

public class TargetService extends SwitchSceneController {
 

    public boolean isPlanExist(String planName, LocalDate startDate, int userId) throws SQLException {
        String checkPlan = "SELECT COUNT(*) FROM target WHERE targetName = ? AND startDate = ? AND userId = ?";
        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(checkPlan)) {
            prepare.setString(1, planName);
            prepare.setString(2, startDate.toString());
            prepare.setInt(3, userId); // Kiểm tra cùng userId
            ResultSet result = prepare.executeQuery();
            return result.next() && result.getInt(1) > 0;
        }
    }

    public void addPlan(String planName, LocalDate startDate, LocalDate endDate, float targetValue, String unit, int userId) throws SQLException {
        String insertData = "INSERT INTO target (targetName, dateCreated, startDate, endDate, targetNumber, unit, progress, status, userId) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(insertData)) {
            Date date = new Date();
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());

            // Tính toán trạng thái dựa trên ngày bắt đầu
            String status = calculateStatus(startDate, endDate, 0.0f, targetValue);

            prepare.setString(1, planName);
            prepare.setDate(2, sqlDate);
            prepare.setString(3, String.valueOf(startDate));
            prepare.setString(4, String.valueOf(endDate));
            prepare.setFloat(5, targetValue);
            String unitBeforeSpace = unit.split(" ")[0];
            prepare.setString(6, unitBeforeSpace);
            prepare.setFloat(7, 0.0f); // Tiến độ ban đầu là 0
            prepare.setString(8, status); // Trạng thái được tính toán
            prepare.setInt(9, userId);

            prepare.executeUpdate();
        }
    }

    public String calculateStatus(LocalDate startDate, LocalDate endDate, float progress, float targetValue) {
        LocalDate today = LocalDate.now();

        if (progress >= targetValue) {
            return "Achieved";
        } else if (today.isBefore(startDate)) {
            return "Not Started";
        } else if (today.isAfter(endDate)) {
            return "Failed";
        } else {
            return "In Progress";
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

    public ObservableList<Target> getPlansForCurrentUser(int userId, boolean finishedOnly) throws SQLException {
        ObservableList<Target> listData = FXCollections.observableArrayList();
        String selectData = finishedOnly ? "SELECT * FROM target WHERE userId = ? AND status = 'Achieved'" : "SELECT * FROM target WHERE userId = ?";

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

    private boolean sendEmail(String emailTo, String subject, String messageContent) {
        final String username = "kiet7784@gmail.com";
        final String password = "cipm rvrt rhso zhsr";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTo));
            message.setSubject(subject);
            message.setText(messageContent);

            Transport.send(message);
            System.out.println("Email đã được gửi đến " + emailTo);
            return true;
        } catch (MessagingException e) {
            System.err.println("Lỗi khi gửi email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkMidCycleProgressAndSendEmail(Target target, User user) {
        if (target == null || user == null) {
            return false;
        }

        String status = target.getStatus();
        if ("Failed".equals(status) || "Not Started".equals(status) || "Cancelled".equals(status)) {
            return false;
        }

        LocalDate startDate;
        startDate = LocalDate.parse(String.valueOf(target.getStartDate()));
        LocalDate endDate;
        endDate = LocalDate.parse(String.valueOf(target.getEndDate()));

        float progress = target.getProgress();
        float targetNumber = target.getTargetNumber();

        if (startDate == null || endDate == null || targetNumber <= 0) {
            return false;
        }

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate midCycleDate = startDate.plusDays(totalDays / 2);

        LocalDate today = LocalDate.now();

        if (!today.isBefore(midCycleDate)) {
            float progressPercentage = (progress / targetNumber) * 100;

            if (progressPercentage < 50) {
                String email = user.getEmail();
                boolean emailSent = false;
                
                if (email != null && !email.isEmpty()) {
                    // Định dạng ngày theo dd/MM/yyyy
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    String formattedStartDate = startDate.format(formatter);
                    String formattedEndDate = endDate.format(formatter);
                    String formattedToday = today.format(formatter);
                    
                    String subject = "Cảnh báo: Tiến độ mục tiêu thấp";
                    String content = "Kính gửi " + user.getUsername() + ",\n\n"
                            + "Hệ thống Health Management nhận thấy mục tiêu \"" + target.getTargetName() 
                            + "\" của bạn hiện mới đạt " + String.format("%.1f", progressPercentage) + "% ("
                            + progress + " " + target.getUnit() + " / " + targetNumber + " " + target.getUnit() + "), "
                            + "trong khi thời gian thực hiện đã trôi qua hơn 50%.\n\n"
                            + "Thời gian bắt đầu: " + formattedStartDate + "\n"
                            + "Thời gian kết thúc: " + formattedEndDate + "\n"
                            + "Thời gian hiện tại: " + formattedToday + "\n\n"
                            + "Vui lòng đăng nhập vào ứng dụng Health Management để kiểm tra và cập nhật tiến độ "
                            + "nhằm đảm bảo đạt được mục tiêu đã đề ra.\n\n"
                            + "Trân trọng,\n"
                            + "Đội ngũ Health Management";

                    emailSent = sendEmail(email, subject, content);
                }
                
                // Không hiển thị cảnh báo khi gửi email thành công
                return emailSent;
            }
        }
        return false;
    }

    public void checkMidCycleProgress(Target target) {
        if (target == null) {
            return;
        }

        String status = target.getStatus();
        if ("Failed".equals(status) || "Not Started".equals(status) || "Cancelled".equals(status)) {
            return;
        }

        LocalDate startDate;
        startDate = LocalDate.parse(String.valueOf(target.getStartDate()));
        LocalDate endDate;
        endDate = LocalDate.parse(String.valueOf(target.getEndDate()));

        float progress = target.getProgress();
        float targetNumber = target.getTargetNumber();

        if (startDate == null || endDate == null || targetNumber <= 0) {
            return;
        }

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate midCycleDate = startDate.plusDays(totalDays / 2);

        LocalDate today = LocalDate.now();

        if (!today.isBefore(midCycleDate)) {
            float progressPercentage = (progress / targetNumber) * 100;

            if (progressPercentage < 50) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo",
                        "Bạn chưa đạt 50% mục tiêu, hãy cố gắng hơn!");
            }
        }
    }

    public Target getPlanByName(String planName) throws SQLException {
        String selectData = "SELECT * FROM target WHERE targetName = ?";
        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(selectData)) {
            prepare.setString(1, planName);
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

    public String calculateStatus(Target target) {
        String status = target.getStatus();
        if ("Cancelled".equals(status)) {
            return "Cancelled";
        }

        LocalDate now = LocalDate.now();
        LocalDate startDate = LocalDate.parse(String.valueOf(target.getStartDate()), DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate endDate = LocalDate.parse(String.valueOf(target.getEndDate()), DateTimeFormatter.ISO_LOCAL_DATE);
        double progress = target.getProgress();
        double goal = target.getTargetNumber();

        if (progress >= goal) {
            return "Achieved";
        } else if (now.isAfter(endDate)) {
            return "Failed";
        } else if ((now.isEqual(startDate) || now.isAfter(startDate)) && (now.isEqual(endDate) || now.isBefore(endDate))) {
            return "In Progress";
        } else {
            return "Not Started";
        }
    }

    public List<Target> getTargetByUserId(int userId, String status) throws SQLException {
        List<Target> targets = new ArrayList<>();
        String sql = "SELECT * FROM target WHERE userId = ? AND status = ?";
        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(sql)) {
            prepare.setInt(1, userId);
            prepare.setString(2, status);
            ResultSet result = prepare.executeQuery();
            while (result.next()) {
                Target target = new Target(
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
                targets.add(target);
            }
        }
        return targets;
    }
}
