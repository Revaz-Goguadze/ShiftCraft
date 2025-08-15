package com.example.shiftcraft.application.service;

import com.example.shiftcraft.persistence.entity.LeaveRequest;
import com.example.shiftcraft.persistence.entity.User;
import com.example.shiftcraft.persistence.repository.LeaveRequestRepository;
import com.example.shiftcraft.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LeaveService leaveService;

    private User testUser;
    private LeaveRequest testLeaveRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "$2a$10$N.UN6inR9EGNhPpthkDcOuP6PNJz6i/Y5DUiH/rjc6ZJiX8V1z1CK", "Test", "User");
        testUser.setId("user123");

        testLeaveRequest = new LeaveRequest(
            testUser,
            LocalDate.of(2024, 6, 10),
            LocalDate.of(2024, 6, 12),
            LeaveRequest.LeaveType.VACATION,
            "Summer vacation"
        );
        testLeaveRequest.setId("request123");
    }

    @Test
    void submitLeaveRequest_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(7);
        LeaveRequest.LeaveType leaveType = LeaveRequest.LeaveType.VACATION;
        String reason = "Family vacation";

        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(leaveRequestRepository.findUserLeaveInPeriod("user123", startDate, endDate))
            .thenReturn(Collections.emptyList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequest result = leaveService.submitLeaveRequest("user123", startDate, endDate, leaveType, reason);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getStatus()).isEqualTo(LeaveRequest.LeaveStatus.PENDING);

        verify(userRepository).findById("user123");
        verify(leaveRequestRepository).findUserLeaveInPeriod("user123", startDate, endDate);
        verify(leaveRequestRepository).save(any(LeaveRequest.class));
    }

    @Test
    void submitLeaveRequest_UserNotFound_ThrowsException() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(7);
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> 
            leaveService.submitLeaveRequest("nonexistent", startDate, endDate, 
                LeaveRequest.LeaveType.VACATION, "reason"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("User not found with id: nonexistent");

        verify(userRepository).findById("nonexistent");
        verifyNoInteractions(leaveRequestRepository);
    }

    @Test
    void submitLeaveRequest_StartDateAfterEndDate_ThrowsException() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(5);
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> 
            leaveService.submitLeaveRequest("user123", startDate, endDate, 
                LeaveRequest.LeaveType.VACATION, "reason"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Start date must be before end date");
    }

    @Test
    void submitLeaveRequest_StartDateInPast_ThrowsException() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> 
            leaveService.submitLeaveRequest("user123", startDate, endDate, 
                LeaveRequest.LeaveType.VACATION, "reason"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cannot request leave for past dates");
    }

    @Test
    void submitLeaveRequest_OverlappingRequest_ThrowsException() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(7);
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(leaveRequestRepository.findUserLeaveInPeriod("user123", startDate, endDate))
            .thenReturn(Arrays.asList(testLeaveRequest));

        // Act & Assert
        assertThatThrownBy(() -> 
            leaveService.submitLeaveRequest("user123", startDate, endDate, 
                LeaveRequest.LeaveType.VACATION, "reason"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Leave request overlaps with existing leave");
    }

    @Test
    void getPendingLeaveRequests_ReturnsRequests() {
        // Arrange
        List<LeaveRequest> pendingRequests = Arrays.asList(testLeaveRequest);
        when(leaveRequestRepository.findPendingRequests()).thenReturn(pendingRequests);

        // Act
        List<LeaveRequest> result = leaveService.getPendingLeaveRequests();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testLeaveRequest);
        verify(leaveRequestRepository).findPendingRequests();
    }

    @Test
    void approveLeaveRequest_Success() {
        // Arrange
        testLeaveRequest.setStatus(LeaveRequest.LeaveStatus.PENDING);
        when(leaveRequestRepository.findById("request123")).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequest result = leaveService.approveLeaveRequest("request123", "manager123", "Approved");

        // Assert
        assertThat(result.getStatus()).isEqualTo(LeaveRequest.LeaveStatus.APPROVED);
        assertThat(result.getReviewedBy()).isEqualTo("manager123");
        assertThat(result.getReviewNotes()).isEqualTo("Approved");
        assertThat(result.getReviewedAt()).isNotNull();

        verify(leaveRequestRepository).findById("request123");
        verify(leaveRequestRepository).save(testLeaveRequest);
    }

    @Test
    void approveLeaveRequest_RequestNotFound_ThrowsException() {
        // Arrange
        when(leaveRequestRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> 
            leaveService.approveLeaveRequest("nonexistent", "manager123", "notes"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Leave request not found with id: nonexistent");
    }

    @Test
    void approveLeaveRequest_NotPendingStatus_ThrowsException() {
        // Arrange
        testLeaveRequest.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        when(leaveRequestRepository.findById("request123")).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThatThrownBy(() -> 
            leaveService.approveLeaveRequest("request123", "manager123", "notes"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Only pending requests can be approved");
    }

    @Test
    void rejectLeaveRequest_Success() {
        // Arrange
        testLeaveRequest.setStatus(LeaveRequest.LeaveStatus.PENDING);
        when(leaveRequestRepository.findById("request123")).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        LeaveRequest result = leaveService.rejectLeaveRequest("request123", "manager123", "Cannot approve");

        // Assert
        assertThat(result.getStatus()).isEqualTo(LeaveRequest.LeaveStatus.REJECTED);
        assertThat(result.getReviewedBy()).isEqualTo("manager123");
        assertThat(result.getReviewNotes()).isEqualTo("Cannot approve");
        assertThat(result.getReviewedAt()).isNotNull();

        verify(leaveRequestRepository).findById("request123");
        verify(leaveRequestRepository).save(testLeaveRequest);
    }

    @Test
    void getUserLeaveRequests_ReturnsUserRequests() {
        // Arrange
        List<LeaveRequest> userRequests = Arrays.asList(testLeaveRequest);
        when(leaveRequestRepository.findByUserId("user123")).thenReturn(userRequests);

        // Act
        List<LeaveRequest> result = leaveService.getUserLeaveRequests("user123");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testLeaveRequest);
        verify(leaveRequestRepository).findByUserId("user123");
    }

    @Test
    void hasApprovedLeave_UserHasApprovedLeave_ReturnsTrue() {
        // Arrange
        testLeaveRequest.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        LocalDate startDate = LocalDate.of(2024, 6, 1);
        LocalDate endDate = LocalDate.of(2024, 6, 15);
        
        when(leaveRequestRepository.findUserLeaveInPeriod("user123", startDate, endDate))
            .thenReturn(Arrays.asList(testLeaveRequest));

        // Act
        boolean result = leaveService.hasApprovedLeave("user123", startDate, endDate);

        // Assert
        assertThat(result).isTrue();
        verify(leaveRequestRepository).findUserLeaveInPeriod("user123", startDate, endDate);
    }

    @Test
    void hasApprovedLeave_UserHasNoPendingLeave_ReturnsFalse() {
        // Arrange
        testLeaveRequest.setStatus(LeaveRequest.LeaveStatus.PENDING);
        LocalDate startDate = LocalDate.of(2024, 6, 1);
        LocalDate endDate = LocalDate.of(2024, 6, 15);
        
        when(leaveRequestRepository.findUserLeaveInPeriod("user123", startDate, endDate))
            .thenReturn(Arrays.asList(testLeaveRequest));

        // Act
        boolean result = leaveService.hasApprovedLeave("user123", startDate, endDate);

        // Assert
        assertThat(result).isFalse();
        verify(leaveRequestRepository).findUserLeaveInPeriod("user123", startDate, endDate);
    }

    @Test
    void cancelLeaveRequest_Success() {
        // Arrange
        testLeaveRequest.setStatus(LeaveRequest.LeaveStatus.PENDING);
        when(leaveRequestRepository.findById("request123")).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // Act
        leaveService.cancelLeaveRequest("request123", "user123");

        // Assert
        assertThat(testLeaveRequest.getStatus()).isEqualTo(LeaveRequest.LeaveStatus.CANCELLED);
        verify(leaveRequestRepository).findById("request123");
        verify(leaveRequestRepository).save(testLeaveRequest);
    }

    @Test
    void cancelLeaveRequest_NotOwnRequest_ThrowsException() {
        // Arrange
        when(leaveRequestRepository.findById("request123")).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThatThrownBy(() -> 
            leaveService.cancelLeaveRequest("request123", "otheruser123"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Can only cancel own leave requests");
    }

    @Test
    void cancelLeaveRequest_NotPendingStatus_ThrowsException() {
        // Arrange
        testLeaveRequest.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        when(leaveRequestRepository.findById("request123")).thenReturn(Optional.of(testLeaveRequest));

        // Act & Assert
        assertThatThrownBy(() -> 
            leaveService.cancelLeaveRequest("request123", "user123"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Can only cancel pending requests");
    }
}