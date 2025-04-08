/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Admin
 */
import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.User;
import com.milkyway.service.LoginService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class LoginTest {

    private LoginService loginService;

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
        loginService = new LoginService();

        mockedStatic = Mockito.mockStatic(JdbcUtils.class);
        mockedStatic.when(JdbcUtils::getConn).thenReturn(mockConnection);

        // Mock prepareStatement để trả về mockPreparedStatement
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockPreparedStatement);
    }

    @Test
    void testLoginSuccess() throws SQLException {
        // Arrange
        String username = "thanhno";
        String password = "12345678";
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("username")).thenReturn(username);
        when(mockResultSet.getString("password")).thenReturn(password);
        when(mockResultSet.getString("email")).thenReturn("thanhno@gmail.com");
        when(mockResultSet.getString("gender")).thenReturn("Nam");
        when(mockResultSet.getBigDecimal("current_weight")).thenReturn(new BigDecimal("56.5"));
        when(mockResultSet.getInt("age")).thenReturn(21);
        when(mockResultSet.getInt("height")).thenReturn(165);
        when(mockResultSet.getDate("registration_date")).thenReturn(new java.sql.Date(new Date().getTime()));

        // Act
        User user = loginService.login(username, password);

        // Assert
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword());
        verify(mockPreparedStatement).setString(1, username);
        verify(mockPreparedStatement).setString(2, password);
    }

    @Test
    void testLoginFailure() throws SQLException {
        // Arrange
        String username = "thanhno";
        String password = "1234567";
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        User user = loginService.login(username, password);

        // Assert
        assertNull(user);
        verify(mockPreparedStatement).setString(1, username);
        verify(mockPreparedStatement).setString(2, password);
    }

    @Test
    void testRegister() throws SQLException {
        // Arrange
        User newUser = new User(0, "thanhno", "12345678", "thanhno@gmail.com", "Nam", new BigDecimal("56.5"), 21, 165, new Date());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        // Act
        loginService.register(newUser);

        // Assert
        verify(mockPreparedStatement).setString(1, newUser.getUsername());
        verify(mockPreparedStatement).setString(2, newUser.getPassword());
        verify(mockPreparedStatement).setString(3, newUser.getEmail());
        verify(mockPreparedStatement).setString(4, newUser.getGender());
        verify(mockPreparedStatement).setBigDecimal(5, newUser.getCurrentWeight());
        verify(mockPreparedStatement).setInt(6, newUser.getAge());
        verify(mockPreparedStatement).setInt(7, newUser.getHeight());
        verify(mockPreparedStatement).setDate(8, new java.sql.Date(newUser.getRegistrationDate().getTime()));
    }

    @Test
    void testGetUserByUsernameSuccess() throws SQLException {
        // Arrange
        String username = "thanhno";
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("username")).thenReturn(username);
        when(mockResultSet.getString("password")).thenReturn("12345678");
        when(mockResultSet.getString("email")).thenReturn("thanhno@gmail.com");
        when(mockResultSet.getString("gender")).thenReturn("Nam");
        when(mockResultSet.getBigDecimal("current_weight")).thenReturn(new BigDecimal("56.5"));
        when(mockResultSet.getInt("age")).thenReturn(21);
        when(mockResultSet.getInt("height")).thenReturn(165);
        when(mockResultSet.getDate("registration_date")).thenReturn(new java.sql.Date(new Date().getTime()));

        // Act
        User user = loginService.getUserByUsername(username);

        // Assert
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        verify(mockPreparedStatement).setString(1, username);
    }

    @Test
    void testGetUserByUsernameFailure() throws SQLException {
        // Arrange
        String username = "admin";
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        User user = loginService.getUserByUsername(username);

        // Assert
        assertNull(user);
        verify(mockPreparedStatement).setString(1, username);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        mockedStatic.close();
    }
}
