package com.example.shiftcraft.web.controller;

import com.example.shiftcraft.application.service.TimesheetService;
import com.example.shiftcraft.application.service.UserService;
import com.example.shiftcraft.persistence.entity.Timesheet;
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
@RequestMapping("/timesheets")
public class TimesheetController {
    
    private final TimesheetService timesheetService;
    private final UserService userService;
    
    @Autowired
    public TimesheetController(TimesheetService timesheetService, UserService userService) {
        this.timesheetService = timesheetService;
        this.userService = userService;
    }
    
    @GetMapping
    public String timesheetsPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        
        Optional<User> currentUser = userService.findByEmail(currentUserEmail);
        
        if (currentUser.isPresent()) {
            User user = currentUser.get();
            boolean isManager = userService.userHasRole(user.getId(), "MANAGER");
            
            if (isManager) {
                // Managers can see all timesheets
                List<Timesheet> allTimesheets = timesheetService.getTimesheetsByStatus(null);
                model.addAttribute("timesheets", allTimesheets);
                model.addAttribute("isManager", true);
                
                // Add staff list for generating timesheets
                List<User> staff = userService.getStaff();
                model.addAttribute("staff", staff);
            } else {
                // Staff see only their own timesheets
                List<Timesheet> userTimesheets = timesheetService.getUserTimesheets(user.getId());
                model.addAttribute("timesheets", userTimesheets);
                model.addAttribute("isManager", false);
            }
            
            model.addAttribute("currentUser", user);
            model.addAttribute("timesheetForm", new TimesheetForm());
        }
        
        return "timesheets";
    }
    
    @PostMapping("/generate")
    public String generateTimesheet(@ModelAttribute TimesheetForm form,
                                   RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = auth.getName();
            
            Optional<User> currentUser = userService.findByEmail(currentUserEmail);
            
            if (currentUser.isPresent()) {
                String targetUserId = form.getUserId();
                
                // If no userId specified, use current user
                if (targetUserId == null || targetUserId.isEmpty()) {
                    targetUserId = currentUser.get().getId();
                } else {
                    // Only managers can generate timesheets for others
                    if (!userService.userHasRole(currentUser.get().getId(), "MANAGER")) {
                        redirectAttributes.addFlashAttribute("errorMessage", 
                            "Only managers can generate timesheets for other users");
                        return "redirect:/timesheets";
                    }
                }
                
                Timesheet timesheet = timesheetService.generateTimesheet(
                    targetUserId,
                    form.getPeriodStart(),
                    form.getPeriodEnd()
                );
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Timesheet generated successfully for " + 
                    timesheet.getUser().getFullName() + 
                    " (" + timesheet.getTotalHours() + " hours)");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/timesheets";
    }
    
    @PostMapping("/generate-weekly")
    public String generateWeeklyTimesheet(@RequestParam String weekStart,
                                         @RequestParam(required = false) String userId,
                                         RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = auth.getName();
            
            Optional<User> currentUser = userService.findByEmail(currentUserEmail);
            
            if (currentUser.isPresent()) {
                String targetUserId = userId;
                
                // If no userId specified, use current user
                if (targetUserId == null || targetUserId.isEmpty()) {
                    targetUserId = currentUser.get().getId();
                } else {
                    // Only managers can generate timesheets for others
                    if (!userService.userHasRole(currentUser.get().getId(), "MANAGER")) {
                        redirectAttributes.addFlashAttribute("errorMessage", 
                            "Only managers can generate timesheets for other users");
                        return "redirect:/timesheets";
                    }
                }
                
                LocalDate weekStartDate = LocalDate.parse(weekStart);
                Timesheet timesheet = timesheetService.generateWeeklyTimesheet(targetUserId, weekStartDate);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Weekly timesheet generated successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/timesheets";
    }
    
    @PostMapping("/{timesheetId}/submit")
    public String submitTimesheet(@PathVariable String timesheetId,
                                 RedirectAttributes redirectAttributes) {
        try {
            Timesheet timesheet = timesheetService.submitTimesheet(timesheetId);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Timesheet submitted for approval!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/timesheets";
    }
    
    @GetMapping("/{timesheetId}")
    public String viewTimesheet(@PathVariable String timesheetId, Model model) {
        // For detailed timesheet view (could be expanded)
        return "redirect:/timesheets";
    }
    
    @GetMapping("/export")
    public String exportTimesheets(@RequestParam LocalDate periodStart,
                                  @RequestParam LocalDate periodEnd,
                                  Model model) {
        // For CSV export functionality (could be implemented)
        return "redirect:/timesheets";
    }
    
    /**
     * Form class for timesheet generation
     */
    public static class TimesheetForm {
        private String userId;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        
        // Constructors
        public TimesheetForm() {}
        
        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public LocalDate getPeriodStart() { return periodStart; }
        public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
        
        public LocalDate getPeriodEnd() { return periodEnd; }
        public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    }
}