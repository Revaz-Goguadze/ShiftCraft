package com.example.shiftcraft.application.service;

import com.example.shiftcraft.persistence.entity.*;
import com.example.shiftcraft.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimesheetServiceTest {

    @Mock
    private TimesheetRepository timesheetRepository;

    @Mock
    private TimesheetEntryRepository timesheetEntryRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TimesheetService timesheetService;

    private User testUser;
    private Timesheet testTimesheet;
    private Assignment testAssignment;
    private ShiftInstance testShiftInstance;
    private ShiftTemplate testShiftTemplate;
    private Location testLocation;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "$2a$10$N.UN6inR9EGNhPpthkDcOuP6PNJz6i/Y5DUiH/rjc6ZJiX8V1z1CK", "Test", "User");
        testUser.setId("user123");

        testLocation = new Location("Test Location", "America/New_York");
        testLocation.setId("location123");

        testRole = new Role("STAFF");
        testRole.setId("role123");

        testShiftTemplate = new ShiftTemplate("Day Shift", testLocation, testRole,
            LocalTime.of(9, 0), LocalTime.of(17, 0));
        testShiftTemplate.setId("template123");
        testShiftTemplate.setBreakMinutes(30);

        testShiftInstance = new ShiftInstance(testShiftTemplate, LocalDate.of(2024, 6, 10));
        testShiftInstance.setId("shift123");

        testAssignment = new Assignment(testShiftInstance, testUser, "manager123");
        testAssignment.setId("assignment123");
        testAssignment.setStatus(Assignment.AssignmentStatus.ACTIVE);

        testTimesheet = new Timesheet(testUser, LocalDate.of(2024, 6, 10), LocalDate.of(2024, 6, 14));
        testTimesheet.setId("timesheet123");
    }

    @Test
    void generateTimesheet_Success() {
        // Arrange
        LocalDate periodStart = LocalDate.of(2024, 6, 10);
        LocalDate periodEnd = LocalDate.of(2024, 6, 14);
        
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(timesheetRepository.findByUserIdAndPeriodStartAndPeriodEnd("user123", periodStart, periodEnd))
            .thenReturn(Optional.empty());
        when(timesheetRepository.save(any(Timesheet.class))).thenReturn(testTimesheet);
        when(assignmentRepository.findByUserIdAndDateRange("user123", periodStart, periodEnd))
            .thenReturn(Arrays.asList(testAssignment));
        when(timesheetEntryRepository.save(any(TimesheetEntry.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Timesheet result = timesheetService.generateTimesheet("user123", periodStart, periodEnd);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getPeriodStart()).isEqualTo(periodStart);
        assertThat(result.getPeriodEnd()).isEqualTo(periodEnd);

        verify(userRepository).findById("user123");
        verify(timesheetRepository).findByUserIdAndPeriodStartAndPeriodEnd("user123", periodStart, periodEnd);
        verify(assignmentRepository).findByUserIdAndDateRange("user123", periodStart, periodEnd);
        verify(timesheetRepository, times(2)).save(any(Timesheet.class));
        verify(timesheetEntryRepository).save(any(TimesheetEntry.class));
    }

    @Test
    void generateTimesheet_UserNotFound_ThrowsException() {
        // Arrange
        LocalDate periodStart = LocalDate.of(2024, 6, 10);
        LocalDate periodEnd = LocalDate.of(2024, 6, 14);
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> 
            timesheetService.generateTimesheet("nonexistent", periodStart, periodEnd))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("User not found with id: nonexistent");

        verify(userRepository).findById("nonexistent");
        verifyNoInteractions(timesheetRepository);
    }

    @Test
    void generateTimesheet_PeriodStartAfterEnd_ThrowsException() {
        // Arrange
        LocalDate periodStart = LocalDate.of(2024, 6, 14);
        LocalDate periodEnd = LocalDate.of(2024, 6, 10);
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> 
            timesheetService.generateTimesheet("user123", periodStart, periodEnd))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Period start must be before period end");
    }

    @Test
    void generateTimesheet_TimesheetAlreadyExists_ThrowsException() {
        // Arrange
        LocalDate periodStart = LocalDate.of(2024, 6, 10);
        LocalDate periodEnd = LocalDate.of(2024, 6, 14);
        
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(timesheetRepository.findByUserIdAndPeriodStartAndPeriodEnd("user123", periodStart, periodEnd))
            .thenReturn(Optional.of(testTimesheet));

        // Act & Assert
        assertThatThrownBy(() -> 
            timesheetService.generateTimesheet("user123", periodStart, periodEnd))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Timesheet already exists for this period");
    }

    @Test
    void addManualEntry_Success() {
        // Arrange
        LocalDate workDate = LocalDate.of(2024, 6, 12);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(14, 0);
        Integer breakMinutes = 30;
        String description = "Manual overtime";

        testTimesheet.setStatus(Timesheet.TimesheetStatus.DRAFT);
        when(timesheetRepository.findById("timesheet123")).thenReturn(Optional.of(testTimesheet));
        when(timesheetEntryRepository.save(any(TimesheetEntry.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(timesheetRepository.save(any(Timesheet.class))).thenReturn(testTimesheet);

        // Act
        TimesheetEntry result = timesheetService.addManualEntry("timesheet123", workDate, startTime, endTime, breakMinutes, description);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getWorkDate()).isEqualTo(workDate);
        assertThat(result.getStartTime()).isEqualTo(startTime);
        assertThat(result.getEndTime()).isEqualTo(endTime);
        assertThat(result.getBreakMinutes()).isEqualTo(breakMinutes);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getEntryType()).isEqualTo(TimesheetEntry.EntryType.MANUAL_ADJUSTMENT);

        verify(timesheetRepository).findById("timesheet123");
        verify(timesheetEntryRepository).save(any(TimesheetEntry.class));
        verify(timesheetRepository).save(testTimesheet);
    }

    @Test
    void addManualEntry_TimesheetNotDraft_ThrowsException() {
        // Arrange
        testTimesheet.setStatus(Timesheet.TimesheetStatus.SUBMITTED);
        when(timesheetRepository.findById("timesheet123")).thenReturn(Optional.of(testTimesheet));

        // Act & Assert
        assertThatThrownBy(() -> 
            timesheetService.addManualEntry("timesheet123", LocalDate.now(), 
                LocalTime.of(9, 0), LocalTime.of(17, 0), 30, "test"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Can only add entries to draft timesheets");
    }

    @Test
    void calculateOvertimeHours_ReturnsCorrectValue() {
        // Arrange
        testTimesheet.setOvertimeHours(new BigDecimal("8.5"));
        when(timesheetRepository.findById("timesheet123")).thenReturn(Optional.of(testTimesheet));

        // Act
        BigDecimal result = timesheetService.calculateOvertimeHours("timesheet123");

        // Assert
        assertThat(result).isEqualTo(new BigDecimal("8.5"));
        verify(timesheetRepository).findById("timesheet123");
    }

    @Test
    void getTimesheet_Found_ReturnsTimesheet() {
        // Arrange
        LocalDate periodStart = LocalDate.of(2024, 6, 10);
        LocalDate periodEnd = LocalDate.of(2024, 6, 14);
        when(timesheetRepository.findByUserIdAndPeriodStartAndPeriodEnd("user123", periodStart, periodEnd))
            .thenReturn(Optional.of(testTimesheet));

        // Act
        Optional<Timesheet> result = timesheetService.getTimesheet("user123", periodStart, periodEnd);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testTimesheet);
        verify(timesheetRepository).findByUserIdAndPeriodStartAndPeriodEnd("user123", periodStart, periodEnd);
    }

    @Test
    void getUserTimesheets_ReturnsUserTimesheets() {
        // Arrange
        List<Timesheet> userTimesheets = Arrays.asList(testTimesheet);
        when(timesheetRepository.findByUserId("user123")).thenReturn(userTimesheets);

        // Act
        List<Timesheet> result = timesheetService.getUserTimesheets("user123");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testTimesheet);
        verify(timesheetRepository).findByUserId("user123");
    }

    @Test
    void submitTimesheet_Success() {
        // Arrange
        testTimesheet.setStatus(Timesheet.TimesheetStatus.DRAFT);
        when(timesheetRepository.findById("timesheet123")).thenReturn(Optional.of(testTimesheet));
        when(timesheetRepository.save(any(Timesheet.class))).thenReturn(testTimesheet);

        // Act
        Timesheet result = timesheetService.submitTimesheet("timesheet123");

        // Assert
        assertThat(result.getStatus()).isEqualTo(Timesheet.TimesheetStatus.SUBMITTED);
        verify(timesheetRepository).findById("timesheet123");
        verify(timesheetRepository).save(testTimesheet);
    }

    @Test
    void submitTimesheet_NotDraftStatus_ThrowsException() {
        // Arrange
        testTimesheet.setStatus(Timesheet.TimesheetStatus.SUBMITTED);
        when(timesheetRepository.findById("timesheet123")).thenReturn(Optional.of(testTimesheet));

        // Act & Assert
        assertThatThrownBy(() -> 
            timesheetService.submitTimesheet("timesheet123"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Only draft timesheets can be submitted");
    }

    @Test
    void approveTimesheet_Success() {
        // Arrange
        testTimesheet.setStatus(Timesheet.TimesheetStatus.SUBMITTED);
        when(timesheetRepository.findById("timesheet123")).thenReturn(Optional.of(testTimesheet));
        when(timesheetRepository.save(any(Timesheet.class))).thenReturn(testTimesheet);

        // Act
        Timesheet result = timesheetService.approveTimesheet("timesheet123", "manager123");

        // Assert
        assertThat(result.getStatus()).isEqualTo(Timesheet.TimesheetStatus.APPROVED);
        assertThat(result.getApprovedBy()).isEqualTo("manager123");
        assertThat(result.getApprovedAt()).isNotNull();

        verify(timesheetRepository).findById("timesheet123");
        verify(timesheetRepository).save(testTimesheet);
    }

    @Test
    void approveTimesheet_NotSubmittedStatus_ThrowsException() {
        // Arrange
        testTimesheet.setStatus(Timesheet.TimesheetStatus.DRAFT);
        when(timesheetRepository.findById("timesheet123")).thenReturn(Optional.of(testTimesheet));

        // Act & Assert
        assertThatThrownBy(() -> 
            timesheetService.approveTimesheet("timesheet123", "manager123"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Only submitted timesheets can be approved");
    }

    @Test
    void getTimesheetsByStatus_ReturnsFilteredTimesheets() {
        // Arrange
        List<Timesheet> submittedTimesheets = Arrays.asList(testTimesheet);
        when(timesheetRepository.findByStatus(Timesheet.TimesheetStatus.SUBMITTED))
            .thenReturn(submittedTimesheets);

        // Act
        List<Timesheet> result = timesheetService.getTimesheetsByStatus(Timesheet.TimesheetStatus.SUBMITTED);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testTimesheet);
        verify(timesheetRepository).findByStatus(Timesheet.TimesheetStatus.SUBMITTED);
    }

    @Test
    void generateWeeklyTimesheet_Success() {
        // Arrange
        LocalDate weekStart = LocalDate.of(2024, 6, 10);  // Monday
        LocalDate weekEnd = LocalDate.of(2024, 6, 16);    // Sunday
        
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(timesheetRepository.findByUserIdAndPeriodStartAndPeriodEnd("user123", weekStart, weekEnd))
            .thenReturn(Optional.empty());
        when(timesheetRepository.save(any(Timesheet.class))).thenReturn(testTimesheet);
        when(assignmentRepository.findByUserIdAndDateRange("user123", weekStart, weekEnd))
            .thenReturn(Collections.emptyList());

        // Act
        Timesheet result = timesheetService.generateWeeklyTimesheet("user123", weekStart);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).findById("user123");
        verify(timesheetRepository).findByUserIdAndPeriodStartAndPeriodEnd("user123", weekStart, weekEnd);
        verify(assignmentRepository).findByUserIdAndDateRange("user123", weekStart, weekEnd);
    }

    @Test
    void timesheetCalculateTotals_CalculatesCorrectly() {
        // Arrange
        Timesheet timesheet = new Timesheet(testUser, LocalDate.of(2024, 6, 10), LocalDate.of(2024, 6, 14));
        
        TimesheetEntry entry1 = new TimesheetEntry();
        entry1.setHours(new BigDecimal("8.0"));
        
        TimesheetEntry entry2 = new TimesheetEntry();
        entry2.setHours(new BigDecimal("7.5"));
        
        TimesheetEntry entry3 = new TimesheetEntry();
        entry3.setHours(new BigDecimal("40.0")); // This will push into overtime
        
        timesheet.getEntries().addAll(Arrays.asList(entry1, entry2, entry3));

        // Act
        timesheet.calculateTotals();

        // Assert
        assertThat(timesheet.getTotalHours()).isEqualTo(new BigDecimal("55.5"));
        assertThat(timesheet.getRegularHours()).isEqualTo(new BigDecimal("40"));
        assertThat(timesheet.getOvertimeHours()).isEqualTo(new BigDecimal("15.5"));
    }
}