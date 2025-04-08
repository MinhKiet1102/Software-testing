/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Admin
 */
import com.milkyway.pojo.History;
import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.User;
import com.milkyway.services.HistoryService;
import com.milkyway.services.HistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class HistoryTest {

    private HistoryService historyService;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private MockedStatic<JdbcUtils> mockedStatic;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        historyService = new HistoryService();

        mockedStatic = Mockito.mockStatic(JdbcUtils.class);
        mockedStatic.when(JdbcUtils::getConn).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    }

    @Test
    void testSaveNewRecord() throws SQLException {
        // Arrange
        User user = new User(1, "thanhno", "12345678", "thanhno@gmail.com");
        History history = new History(0, new Date(), new BigDecimal("70.5"), 180);
        history.setUserId(user);

        // Mock ResultSet để recordExists trả về false (bản ghi chưa tồn tại)
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // Bản ghi chưa tồn tại

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        historyService.save(history);

        // Assert
        verify(mockPreparedStatement).setDate(1, new java.sql.Date(history.getHistoryDate().getTime()));
        verify(mockPreparedStatement).setBigDecimal(2, history.getHistoryWeight());
        verify(mockPreparedStatement).setInt(3, history.getHistoryHeight());
        verify(mockPreparedStatement).setInt(4, history.getUserId().getId());
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testSaveUpdateRecord() throws SQLException {
        // Arrange
        User user = new User(1, "thanhno", "12345678", "thanhno@gmail.com");
        History history = new History(0, new Date(), new BigDecimal("56.5"), 165);
        history.setUserId(user);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        historyService.save(history);

        // Assert
        verify(mockPreparedStatement).setBigDecimal(1, history.getHistoryWeight());
        verify(mockPreparedStatement).setInt(2, history.getHistoryHeight());
        verify(mockPreparedStatement).setInt(3, history.getUserId().getId());
        verify(mockPreparedStatement).setDate(4, new java.sql.Date(history.getHistoryDate().getTime()));
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testFindAllByUserId() throws SQLException {
        // Arrange
        int userId = 1;
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("history_id")).thenReturn(1);
        when(mockResultSet.getDate("history_date")).thenReturn(new java.sql.Date(new Date().getTime()));
        when(mockResultSet.getBigDecimal("history_weight")).thenReturn(new BigDecimal("56.5"));
        when(mockResultSet.getInt("history_height")).thenReturn(165);

        // Act
        List<History> histories = historyService.findAllByUserId(userId);

        // Assert
        assertNotNull(histories);
        assertEquals(1, histories.size());
        verify(mockPreparedStatement).setInt(1, userId);
    }

    @Test
    void testFindLatestHistoryByUserId() throws SQLException {
        // Arrange
        int userId = 1;
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("history_id")).thenReturn(1);
        when(mockResultSet.getDate("history_date")).thenReturn(new java.sql.Date(new Date().getTime()));
        when(mockResultSet.getBigDecimal("history_weight")).thenReturn(new BigDecimal("56.5"));
        when(mockResultSet.getInt("history_height")).thenReturn(165);

        // Act
        History history = historyService.findLatestHistoryByUserId(userId);

        // Assert
        assertNotNull(history);
        assertEquals(1, history.getHistoryId());
        verify(mockPreparedStatement).setInt(1, userId);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        mockedStatic.close();
    }
}
