package com.example.shiftcraft.web.controller;

import com.example.shiftcraft.application.service.LeaveService;
import com.example.shiftcraft.application.service.TimesheetService;
import com.example.shiftcraft.application.service.UserService;
import com.example.shiftcraft.persistence.entity.LeaveRequest;
import com.example.shiftcraft.persistence.entity.Timesheet;
import com.example.shiftcraft.persistence.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/approvals")
@PreAuthorize("hasRole('MANAGER')")
public class ApprovalController {
    
    private final LeaveService leaveService;
    private final TimesheetService timesheetService;
    private final UserService userService;
    
    @Autowired
    public ApprovalController(LeaveService leaveService, TimesheetService timesheetService, UserService userService) {
        this.leaveService = leaveService;
        this.timesheetService = timesheetService;
        this.userService = userService;
    }
    
    @GetMapping
    public String approvalsPage(Model model) {
        // Get pending leave requests
        List<LeaveRequest> pendingLeaveRequests = leaveService.getPendingLeaveRequests();
        
        // Get submitted timesheets awaiting approval
        List<Timesheet> submittedTimesheets = timesheetService.getTimesheetsByStatus(Timesheet.TimesheetStatus.SUBMITTED);
        
        model.addAttribute("pendingLeaveRequests", pendingLeaveRequests);
        model.addAttribute("submittedTimesheets", submittedTimesheets);
        
        return "approvals";
    }
    
    @PostMapping("/leave/{requestId}/approve")
    public String approveLeaveRequest(@PathVariable String requestId,
                                    @RequestParam(required = false) String notes,
                                    RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = auth.getName();
            
            Optional<User> currentUser = userService.findByEmail(currentUserEmail);
            
            if (currentUser.isPresent()) {
                LeaveRequest approved = leaveService.approveLeaveRequest(
                    requestId, currentUser.get().getId(), notes);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Leave request approved for " + approved.getUser().getFullName());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/approvals";
    }
    
    @PostMapping("/leave/{requestId}/reject")
    public String rejectLeaveRequest(@PathVariable String requestId,
                                   @RequestParam(required = false) String notes,
                                   RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = auth.getName();
            
            Optional<User> currentUser = userService.findByEmail(currentUserEmail);
            
            if (currentUser.isPresent()) {
                LeaveRequest rejected = leaveService.rejectLeaveRequest(
                    requestId, currentUser.get().getId(), notes);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Leave request rejected for " + rejected.getUser().getFullName());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/approvals";
    }
    
    @PostMapping("/timesheet/{timesheetId}/approve")
    public String approveTimesheet(@PathVariable String timesheetId,
                                  RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = auth.getName();
            
            Optional<User> currentUser = userService.findByEmail(currentUserEmail);
            
            if (currentUser.isPresent()) {
                Timesheet approved = timesheetService.approveTimesheet(
                    timesheetId, currentUser.get().getId());
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Timesheet approved for " + approved.getUser().getFullName());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/approvals";
    }
    
    @GetMapping("/leave/{requestId}")
    public String viewLeaveRequestDetails(@PathVariable String requestId, Model model) {
        // For detailed view of leave request (optional enhancement)
        return "redirect:/approvals";
    }
    
    @GetMapping("/timesheet/{timesheetId}")
    public String viewTimesheetDetails(@PathVariable String timesheetId, Model model) {
        // For detailed view of timesheet (optional enhancement)
        return "redirect:/approvals";
    }
}