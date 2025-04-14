package test;


import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.Target;
import com.milkyway.services.TargetService;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Assertions;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TargetTest {

    private TargetService targetService;

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
        targetService = new TargetService();

        mockedStatic = Mockito.mockStatic(JdbcUtils.class);
        mockedStatic.when(JdbcUtils::getConn).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    }

    @Test
    void testIsPlanExist() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        // Act
        boolean result = targetService.isPlanExist("Test Plan", LocalDate.now(), 1);

        // Assert
        assertTrue(result);
        verify(mockPreparedStatement).setString(1, "Test Plan");
        verify(mockPreparedStatement).setString(2, LocalDate.now().toString());
        verify(mockPreparedStatement).setInt(3, 1);
    }

    @Test
    void testAddPlan() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        String actualStatus = targetService.calculateStatus(LocalDate.of(2025, 4, 7), LocalDate.of(2025, 4, 7).plusDays(7), 0.0f, 100.0f);

        // Act
        targetService.addPlan("Test Plan", LocalDate.of(2025, 4, 7), LocalDate.of(2025, 4, 7).plusDays(7), 100.0f, "kg", 1);

        // Assert
        verify(mockPreparedStatement).setString(1, "Test Plan");
        verify(mockPreparedStatement).setDate(eq(2), any(java.sql.Date.class));
        verify(mockPreparedStatement).setString(eq(3), eq(LocalDate.now().toString()));
        verify(mockPreparedStatement).setString(eq(4), eq(LocalDate.now().plusDays(7).toString()));
        verify(mockPreparedStatement).setFloat(5, 100.0f);
        verify(mockPreparedStatement).setString(6, "kg");
        verify(mockPreparedStatement).setFloat(7, 0.0f);
        verify(mockPreparedStatement).setString(8, actualStatus);
        verify(mockPreparedStatement).setInt(9, 1);
    }

    @Test
    void testCalculateStatus() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(7);

        // Act & Assert
        assertEquals("Not Started", targetService.calculateStatus(startDate.plusDays(7), endDate, 0.0f, 100.0f));
        assertEquals("In Progress", targetService.calculateStatus(startDate.minusDays(1), endDate, 50.0f, 100.0f));
        assertEquals("Achieved", targetService.calculateStatus(startDate.minusDays(1), endDate, 100.0f, 100.0f));
        assertEquals("Failed", targetService.calculateStatus(startDate.minusDays(8), startDate.minusDays(1), 50.0f, 100.0f));
    }

    @Test
    void testGetOldEndDate() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("endDate")).thenReturn(LocalDate.now().toString());

        // Act
        LocalDate result = targetService.getOldEndDate(1);

        // Assert
        assertNotNull(result);
        verify(mockPreparedStatement).setInt(1, 1);
    }

    @Test
    void testGetDateCreated() throws SQLException {
        // Arrange
        String expectedDateCreated = "2025-10-01 10:00:00";
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("dateCreated")).thenReturn(expectedDateCreated);

        // Act
        String result = targetService.getDateCreated(1);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDateCreated, result);
        verify(mockPreparedStatement).setInt(1, 1);
    }

    @Test
    void testUpdatePlan() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        targetService.updatePlan(1, "Updated Plan", LocalDate.now(), LocalDate.now().plusDays(7), LocalDate.now().toString(), 150.0f, "kg");

        // Assert
        verify(mockPreparedStatement).setString(1, "Updated Plan");
        verify(mockPreparedStatement).setString(2, LocalDate.now().toString());
        verify(mockPreparedStatement).setString(3, LocalDate.now().plusDays(7).toString());
        verify(mockPreparedStatement).setString(4, LocalDate.now().toString());
        verify(mockPreparedStatement).setFloat(5, 150.0f);
        verify(mockPreparedStatement).setString(6, "kg");
        verify(mockPreparedStatement).setInt(7, 1);
    }

    @Test
    void testUpdatePlanProgress() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        targetService.updatePlanProgress(1, 75.0f);

        // Assert
        verify(mockPreparedStatement).setFloat(1, 75.0f);
        verify(mockPreparedStatement).setInt(2, 1);
    }

    @Test
    void testGetPlansForCurrentUser() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("idTarget")).thenReturn(1);
        when(mockResultSet.getString("targetName")).thenReturn("Test Plan");
        when(mockResultSet.getDate("dateCreated")).thenReturn(new java.sql.Date(System.currentTimeMillis()));
        when(mockResultSet.getDate("startDate")).thenReturn(new java.sql.Date(System.currentTimeMillis()));
        when(mockResultSet.getDate("endDate")).thenReturn(new java.sql.Date(System.currentTimeMillis()));
        when(mockResultSet.getFloat("targetNumber")).thenReturn(100.0f);
        when(mockResultSet.getString("unit")).thenReturn("kg");
        when(mockResultSet.getFloat("progress")).thenReturn(50.0f);
        when(mockResultSet.getString("status")).thenReturn("In Progress");

        // Act
        List<Target> result = targetService.getPlansForCurrentUser(1, false);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mockPreparedStatement).setInt(1, 1);
    }

    @Test
    void testIsPlanExistById() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        // Act
        boolean result = targetService.isPlanExist(1);

        // Assert
        assertTrue(result);
        verify(mockPreparedStatement).setInt(1, 1);
    }

    @Test
    void testDeletePlan() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        targetService.deletePlan(1);

        // Assert
        verify(mockPreparedStatement).setInt(1, 1);
    }

    @Test
    void testCountQuantityPlans() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(5);

        // Act
        int result = targetService.countQuantityPlans(1);

        // Assert
        assertEquals(5, result);
        verify(mockPreparedStatement).setInt(1, 1);
    }

    @Test
    void testCountAchievedPlans() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(3);

        // Act
        int result = targetService.countAchievedPlans(1);

        // Assert
        assertEquals(3, result);
        verify(mockPreparedStatement).setInt(1, 1);
    }

    @Test
    void testGetPlanById() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("idTarget")).thenReturn(1);
        when(mockResultSet.getString("targetName")).thenReturn("Test Plan");
        when(mockResultSet.getDate("dateCreated")).thenReturn(new java.sql.Date(System.currentTimeMillis()));
        when(mockResultSet.getDate("startDate")).thenReturn(new java.sql.Date(System.currentTimeMillis()));
        when(mockResultSet.getDate("endDate")).thenReturn(new java.sql.Date(System.currentTimeMillis()));
        when(mockResultSet.getFloat("targetNumber")).thenReturn(100.0f);
        when(mockResultSet.getString("unit")).thenReturn("kg");
        when(mockResultSet.getFloat("progress")).thenReturn(50.0f);
        when(mockResultSet.getString("status")).thenReturn("In Progress");

        // Act
        Target result = targetService.getPlanById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getIdTarget().intValue());
        verify(mockPreparedStatement).setInt(1, 1);
    }

    @Test
    void testUpdatePlanStatus() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        targetService.updatePlanStatus(1, "Achieved");

        // Assert
        verify(mockPreparedStatement).setString(1, "Achieved");
        verify(mockPreparedStatement).setInt(2, 1);
    }

    @Test
    void testGetStatusList() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("Type")).thenReturn("enum('Not Started','In Progress','Achieved','Failed','Cancelled')");

        // Act
        List<String> result = targetService.getStatusList();

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
        assertTrue(result.contains("Not Started"));
        assertTrue(result.contains("In Progress"));
        assertTrue(result.contains("Achieved"));
        assertTrue(result.contains("Failed"));
        assertTrue(result.contains("Cancelled"));
    }

    @Test
    void testCheckMidCycleProgress() {
        // Arrange
        Target target = new Target();
        target.setStartDate(new java.sql.Date(System.currentTimeMillis()));
        target.setEndDate(new java.sql.Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000));
        target.setTargetNumber(100.0f);
        target.setProgress(40.0f);
        target.setStatus("In Progress");

        // Act
        targetService.checkMidCycleProgress(target);
    }

    @Test
    void testGetPlanByName() throws SQLException {
        // Arrange
        String planName = "Test Plan";
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("idTarget")).thenReturn(1);
        when(mockResultSet.getString("targetName")).thenReturn(planName);
        when(mockResultSet.getDate("dateCreated")).thenReturn(new java.sql.Date(System.currentTimeMillis()));
        when(mockResultSet.getDate("startDate")).thenReturn(new java.sql.Date(System.currentTimeMillis()));
        when(mockResultSet.getDate("endDate")).thenReturn(new java.sql.Date(System.currentTimeMillis()));
        when(mockResultSet.getFloat("targetNumber")).thenReturn(100.0f);
        when(mockResultSet.getString("unit")).thenReturn("kg");
        when(mockResultSet.getFloat("progress")).thenReturn(50.0f);
        when(mockResultSet.getString("status")).thenReturn("In Progress");

        // Act
        Target result = targetService.getPlanByName(planName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getIdTarget().intValue());
        assertEquals(planName, result.getTargetName());
        verify(mockPreparedStatement).setString(1, planName);
    }

    @Test
    void testAutoCalculateStatus() {
        // Arrange
        Target target = new Target();
        target.setStartDate(java.sql.Date.valueOf(LocalDate.now().minusDays(1)));
        target.setEndDate(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
        target.setTargetNumber(100.0f);
        target.setProgress(50.0f);

        // Act & Assert
        assertEquals("In Progress", targetService.calculateStatus(target));

        target.setProgress(100.0f);
        assertEquals("Achieved", targetService.calculateStatus(target));

        target.setEndDate(java.sql.Date.valueOf(LocalDate.now().minusDays(1)));
        target.setProgress(50.0f);
        assertEquals("Failed", targetService.calculateStatus(target));

        target.setStartDate(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
        target.setEndDate(java.sql.Date.valueOf(LocalDate.now().plusDays(2)));
        assertEquals("Not Started", targetService.calculateStatus(target));

        target.setStatus("Cancelled");
        assertEquals("Cancelled", targetService.calculateStatus(target));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        mockedStatic.close();
    }
}
