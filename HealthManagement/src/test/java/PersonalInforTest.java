/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Admin
 */
import com.milkyway.pojo.JdbcUtils;
import com.milkyway.service.PersonalInforService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PersonalInforTest {

    private PersonalInforService personalInforService;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    private MockedStatic<JdbcUtils> mockedStatic;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        personalInforService = new PersonalInforService();

        mockedStatic = Mockito.mockStatic(JdbcUtils.class);
        mockedStatic.when(JdbcUtils::getConn).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    }

    @Test
    void testUpdateUsername() throws SQLException {
        // Arrange
        int userId = 1;
        String newUsername = "thanhno123";
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        personalInforService.updateUsername(userId, newUsername);

        // Assert
        verify(mockPreparedStatement).setString(1, newUsername);
        verify(mockPreparedStatement).setInt(2, userId);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testUpdatePassword() throws SQLException {
        // Arrange
        int userId = 1;
        String newPassword = "123456789";
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        personalInforService.updatePassword(userId, newPassword);

        // Assert
        verify(mockPreparedStatement).setString(1, newPassword);
        verify(mockPreparedStatement).setInt(2, userId);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testUpdateWeight() throws SQLException {
        // Arrange
        int userId = 1;
        BigDecimal newWeight = new BigDecimal("70.5");
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        personalInforService.updateWeight(userId, newWeight);

        // Assert
        verify(mockPreparedStatement).setBigDecimal(1, newWeight);
        verify(mockPreparedStatement).setInt(2, userId);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testUpdateHeight() throws SQLException {
        // Arrange
        int userId = 1;
        int newHeight = 180;
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        personalInforService.updateHeight(userId, newHeight);

        // Assert
        verify(mockPreparedStatement).setInt(1, newHeight);
        verify(mockPreparedStatement).setInt(2, userId);
        verify(mockPreparedStatement).executeUpdate();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        mockedStatic.close();
    }
}
