package com.example.shiftcraft.web.controller;

import com.example.shiftcraft.application.service.ScheduleService;
import com.example.shiftcraft.application.service.UserService;
import com.example.shiftcraft.persistence.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {
    
    private final ScheduleService scheduleService;
    private final UserService userService;
    
    @Autowired
    public ScheduleController(ScheduleService scheduleService, UserService userService) {
        this.scheduleService = scheduleService;
        this.userService = userService;
    }
    
    @GetMapping
    public String viewSchedule(@RequestParam(required = false) String date, Model model) {
        LocalDate scheduleDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        
        Optional<User> currentUser = userService.findByEmail(currentUserEmail);
        
        if (currentUser.isPresent()) {
            User user = currentUser.get();
            boolean isManager = userService.userHasRole(user.getId(), "MANAGER");
            
            if (isManager) {
                // Managers see full schedule for all users
                ScheduleService.WeeklySchedule weeklySchedule = scheduleService.getWeeklySchedule(scheduleDate);
                model.addAttribute("weeklySchedule", weeklySchedule);
                model.addAttribute("isManager", true);
            } else {
                // Staff see only their own schedule
                ScheduleService.UserWeeklySchedule userSchedule = 
                    scheduleService.getUserWeeklySchedule(user.getId(), scheduleDate);
                model.addAttribute("userSchedule", userSchedule);
                model.addAttribute("isManager", false);
            }
            
            model.addAttribute("currentUser", user);
            model.addAttribute("selectedDate", scheduleDate);
        }
        
        return "schedule";
    }
    
    @GetMapping("/week")
    public String viewWeeklySchedule(@RequestParam String weekStart, Model model) {
        return viewSchedule(weekStart, model);
    }
}