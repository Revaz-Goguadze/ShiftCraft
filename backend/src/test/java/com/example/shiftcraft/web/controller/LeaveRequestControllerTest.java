package com.example.shiftcraft.web.controller;

import com.example.shiftcraft.application.service.LeaveService;
import com.example.shiftcraft.application.service.UserService;
import com.example.shiftcraft.persistence.entity.LeaveRequest;
import com.example.shiftcraft.persistence.entity.User;
import com.example.shiftcraft.web.security.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeaveRequestController.class)
class LeaveRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaveService leaveService;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private User testUser;
    private LeaveRequest testLeaveRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("staff@example.com", "$2a$10$N.UN6inR9EGNhPpthkDcOuP6PNJz6i/Y5DUiH/rjc6ZJiX8V1z1CK", "Staff", "Member");
        testUser.setId("user123");

        testLeaveRequest = new LeaveRequest(
            testUser,
            LocalDate.of(2024, 6, 10),
            LocalDate.of(2024, 6, 12),
            LeaveRequest.LeaveType.VACATION,
            "Summer vacation"
        );
        testLeaveRequest.setId("request123");
        testLeaveRequest.setRequestedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = "staff@example.com", roles = "STAFF")
    void requestsPage_AsStaff_ReturnsRequestsView() throws Exception {
        // Arrange
        List<LeaveRequest> userRequests = Arrays.asList(testLeaveRequest);
        when(userService.findByEmail("staff@example.com")).thenReturn(Optional.of(testUser));
        when(leaveService.getUserLeaveRequests("user123")).thenReturn(userRequests);

        // Act & Assert
        mockMvc.perform(get("/requests"))
            .andExpect(status().isOk())
            .andExpect(view().name("requests"))
            .andExpect(model().attributeExists("currentUser"))
            .andExpect(model().attributeExists("userRequests"))
            .andExpect(model().attributeExists("leaveRequest"))
            .andExpect(model().attributeExists("leaveTypes"))
            .andExpect(model().attribute("userRequests", hasSize(1)))
            .andExpect(model().attribute("leaveTypes", 
                is(LeaveRequest.LeaveType.values())));

        verify(userService).findByEmail("staff@example.com");
        verify(leaveService).getUserLeaveRequests("user123");
    }

    @Test
    @WithMockUser(username = "staff@example.com", roles = "STAFF")
    void submitLeaveRequest_ValidRequest_RedirectsWithSuccess() throws Exception {
        // Arrange
        when(userService.findByEmail("staff@example.com")).thenReturn(Optional.of(testUser));
        when(leaveService.submitLeaveRequest(
            eq("user123"), 
            any(LocalDate.class), 
            any(LocalDate.class), 
            eq(LeaveRequest.LeaveType.VACATION), 
            eq("Test reason")))
            .thenReturn(testLeaveRequest);

        // Act & Assert
        mockMvc.perform(post("/requests")
                .with(csrf())
                .param("startDate", "2024-06-10")
                .param("endDate", "2024-06-12")
                .param("leaveType", "VACATION")
                .param("reason", "Test reason"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/requests"))
            .andExpect(flash().attributeExists("successMessage"))
            .andExpect(flash().attribute("successMessage", 
                containsString("Leave request submitted successfully")));

        verify(userService).findByEmail("staff@example.com");
        verify(leaveService).submitLeaveRequest(
            "user123", 
            LocalDate.of(2024, 6, 10), 
            LocalDate.of(2024, 6, 12), 
            LeaveRequest.LeaveType.VACATION, 
            "Test reason");
    }

    @Test
    @WithMockUser(username = "staff@example.com", roles = "STAFF")
    void submitLeaveRequest_ServiceThrowsException_RedirectsWithError() throws Exception {
        // Arrange
        when(userService.findByEmail("staff@example.com")).thenReturn(Optional.of(testUser));
        when(leaveService.submitLeaveRequest(any(), any(), any(), any(), any()))
            .thenThrow(new IllegalArgumentException("Start date must be before end date"));

        // Act & Assert
        mockMvc.perform(post("/requests")
                .with(csrf())
                .param("startDate", "2024-06-12")
                .param("endDate", "2024-06-10")
                .param("leaveType", "VACATION")
                .param("reason", "Test reason"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/requests"))
            .andExpect(flash().attributeExists("errorMessage"))
            .andExpect(flash().attribute("errorMessage", 
                "Start date must be before end date"));
    }

    @Test
    @WithMockUser(username = "staff@example.com", roles = "STAFF")
    void cancelLeaveRequest_ValidRequest_RedirectsWithSuccess() throws Exception {
        // Arrange
        when(userService.findByEmail("staff@example.com")).thenReturn(Optional.of(testUser));
        doNothing().when(leaveService).cancelLeaveRequest("request123", "user123");

        // Act & Assert
        mockMvc.perform(post("/requests/request123/cancel")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/requests"))
            .andExpect(flash().attributeExists("successMessage"))
            .andExpect(flash().attribute("successMessage", 
                containsString("Leave request cancelled successfully")));

        verify(userService).findByEmail("staff@example.com");
        verify(leaveService).cancelLeaveRequest("request123", "user123");
    }

    @Test
    @WithMockUser(username = "staff@example.com", roles = "STAFF")
    void cancelLeaveRequest_ServiceThrowsException_RedirectsWithError() throws Exception {
        // Arrange
        when(userService.findByEmail("staff@example.com")).thenReturn(Optional.of(testUser));
        doThrow(new IllegalStateException("Can only cancel pending requests"))
            .when(leaveService).cancelLeaveRequest("request123", "user123");

        // Act & Assert
        mockMvc.perform(post("/requests/request123/cancel")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/requests"))
            .andExpect(flash().attributeExists("errorMessage"))
            .andExpect(flash().attribute("errorMessage", 
                "Can only cancel pending requests"));
    }

    @Test
    @WithMockUser(username = "manager@example.com", roles = "MANAGER")
    void requestsPage_AsManager_StillAllowed() throws Exception {
        // Arrange
        User manager = new User("manager@example.com", "$2a$10$N.UN6inR9EGNhPpthkDcOuP6PNJz6i/Y5DUiH/rjc6ZJiX8V1z1CK", "Manager", "User");
        manager.setId("manager123");
        
        when(userService.findByEmail("manager@example.com")).thenReturn(Optional.of(manager));
        when(leaveService.getUserLeaveRequests("manager123")).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/requests"))
            .andExpect(status().isOk())
            .andExpect(view().name("requests"));

        verify(userService).findByEmail("manager@example.com");
        verify(leaveService).getUserLeaveRequests("manager123");
    }

    @Test
    void requestsPage_Unauthenticated_RedirectsToLogin() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/requests"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService, leaveService);
    }

    @Test
    @WithMockUser(username = "staff@example.com", roles = "STAFF")
    void submitLeaveRequest_MissingCsrf_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/requests")
                .param("startDate", "2024-06-10")
                .param("endDate", "2024-06-12")
                .param("leaveType", "VACATION")
                .param("reason", "Test reason"))
            .andExpect(status().isForbidden());

        verifyNoInteractions(leaveService);
    }

    @Test
    @WithMockUser(username = "staff@example.com", roles = "STAFF")
    void requestsPage_UserNotFound_HandlesProperly() throws Exception {
        // Arrange
        when(userService.findByEmail("staff@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/requests"))
            .andExpect(status().isOk())
            .andExpect(view().name("requests"))
            .andExpect(model().attributeDoesNotExist("currentUser"))
            .andExpect(model().attributeDoesNotExist("userRequests"));

        verify(userService).findByEmail("staff@example.com");
        verifyNoInteractions(leaveService);
    }

    @Test
    @WithMockUser(username = "staff@example.com", roles = "STAFF")
    void requestsPage_WithMultipleRequests_DisplaysAll() throws Exception {
        // Arrange
        LeaveRequest request1 = new LeaveRequest(testUser, LocalDate.now().plusDays(5), 
            LocalDate.now().plusDays(7), LeaveRequest.LeaveType.VACATION, "Vacation 1");
        request1.setStatus(LeaveRequest.LeaveStatus.PENDING);
        
        LeaveRequest request2 = new LeaveRequest(testUser, LocalDate.now().plusDays(15), 
            LocalDate.now().plusDays(16), LeaveRequest.LeaveType.SICK, "Doctor appointment");
        request2.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        
        List<LeaveRequest> userRequests = Arrays.asList(request1, request2);
        
        when(userService.findByEmail("staff@example.com")).thenReturn(Optional.of(testUser));
        when(leaveService.getUserLeaveRequests("user123")).thenReturn(userRequests);

        // Act & Assert
        mockMvc.perform(get("/requests"))
            .andExpect(status().isOk())
            .andExpect(view().name("requests"))
            .andExpect(model().attribute("userRequests", hasSize(2)))
            .andExpect(model().attribute("userRequests", hasItem(
                allOf(
                    hasProperty("leaveType", is(LeaveRequest.LeaveType.VACATION)),
                    hasProperty("status", is(LeaveRequest.LeaveStatus.PENDING))
                ))))
            .andExpect(model().attribute("userRequests", hasItem(
                allOf(
                    hasProperty("leaveType", is(LeaveRequest.LeaveType.SICK)),
                    hasProperty("status", is(LeaveRequest.LeaveStatus.APPROVED))
                ))));

        verify(userService).findByEmail("staff@example.com");
        verify(leaveService).getUserLeaveRequests("user123");
    }
}