package com.example.shiftcraft.web.controller;

import com.example.shiftcraft.application.service.LeaveService;
import com.example.shiftcraft.application.service.UserService;
import com.example.shiftcraft.persistence.entity.LeaveRequest;
import com.example.shiftcraft.persistence.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/requests")
public class LeaveRequestController {
    
    private final LeaveService leaveService;
    private final UserService userService;
    
    @Autowired
    public LeaveRequestController(LeaveService leaveService, UserService userService) {
        this.leaveService = leaveService;
        this.userService = userService;
    }
    
    @GetMapping
    public String requestsPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        
        Optional<User> currentUser = userService.findByEmail(currentUserEmail);
        
        // Always add leaveRequest and leaveTypes for template
        model.addAttribute("leaveRequest", new LeaveRequestForm());
        model.addAttribute("leaveTypes", LeaveRequest.LeaveType.values());
        
        if (currentUser.isPresent()) {
            User user = currentUser.get();
            List<LeaveRequest> userRequests = leaveService.getUserLeaveRequests(user.getId());
            
            model.addAttribute("currentUser", user);
            model.addAttribute("userRequests", userRequests);
        }
        
        return "requests";
    }
    
    @PostMapping
    public String submitLeaveRequest(@ModelAttribute LeaveRequestForm form, 
                                   RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = auth.getName();
            
            Optional<User> currentUser = userService.findByEmail(currentUserEmail);
            
            if (currentUser.isPresent()) {
                LeaveRequest request = leaveService.submitLeaveRequest(
                    currentUser.get().getId(),
                    form.getStartDate(),
                    form.getEndDate(),
                    form.getLeaveType(),
                    form.getReason()
                );
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Leave request submitted successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/requests";
    }
    
    @PostMapping("/{requestId}/cancel")
    public String cancelLeaveRequest(@PathVariable String requestId,
                                   RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = auth.getName();
            
            Optional<User> currentUser = userService.findByEmail(currentUserEmail);
            
            if (currentUser.isPresent()) {
                leaveService.cancelLeaveRequest(requestId, currentUser.get().getId());
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Leave request cancelled successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/requests";
    }
    
    /**
     * Form class for leave request submission
     */
    public static class LeaveRequestForm {
        private LocalDate startDate;
        private LocalDate endDate;
        private LeaveRequest.LeaveType leaveType;
        private String reason;
        
        // Constructors
        public LeaveRequestForm() {}
        
        // Getters and Setters
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        
        public LeaveRequest.LeaveType getLeaveType() { return leaveType; }
        public void setLeaveType(LeaveRequest.LeaveType leaveType) { this.leaveType = leaveType; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}