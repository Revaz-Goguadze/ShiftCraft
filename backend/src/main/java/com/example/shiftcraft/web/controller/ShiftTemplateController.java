package com.example.shiftcraft.web.controller;

import com.example.shiftcraft.application.service.ShiftService;
import com.example.shiftcraft.persistence.entity.Location;
import com.example.shiftcraft.persistence.entity.Role;
import com.example.shiftcraft.persistence.entity.ShiftTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/templates")
@PreAuthorize("hasRole('MANAGER')")
public class ShiftTemplateController {
    
    private final ShiftService shiftService;
    
    @Autowired
    public ShiftTemplateController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }
    
    @GetMapping
    public String templatesPage(Model model) {
        List<ShiftTemplate> templates = shiftService.getAllShiftTemplates();
        List<Location> locations = shiftService.findAllLocations();
        List<Role> roles = shiftService.findAllRoles();
        
        model.addAttribute("templates", templates);
        model.addAttribute("locations", locations);
        model.addAttribute("roles", roles);
        model.addAttribute("templateForm", new ShiftTemplateForm());
        
        return "templates";
    }
    
    @PostMapping
    public String createShiftTemplate(@ModelAttribute ShiftTemplateForm form,
                                     RedirectAttributes redirectAttributes) {
        try {
            ShiftTemplate template = shiftService.createShiftTemplate(
                form.getName(),
                form.getLocationId(),
                form.getRoleId(),
                form.getStartTime(),
                form.getEndTime(),
                form.getBreakMinutes()
            );
            
            if (form.getDescription() != null && !form.getDescription().trim().isEmpty()) {
                template.setDescription(form.getDescription());
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Shift template '" + template.getName() + "' created successfully!");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/templates";
    }
    
    @PostMapping("/{templateId}/update")
    public String updateShiftTemplate(@PathVariable String templateId,
                                     @RequestParam String name,
                                     @RequestParam String description,
                                     RedirectAttributes redirectAttributes) {
        try {
            ShiftTemplate template = shiftService.updateShiftTemplate(templateId, name, description);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Shift template '" + template.getName() + "' updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/templates";
    }
    
    @PostMapping("/{templateId}/deactivate")
    public String deactivateShiftTemplate(@PathVariable String templateId,
                                         RedirectAttributes redirectAttributes) {
        try {
            ShiftTemplate template = shiftService.deactivateShiftTemplate(templateId);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Shift template '" + template.getName() + "' deactivated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/templates";
    }
    
    @GetMapping("/active")
    public String activeTemplatesPage(Model model) {
        List<ShiftTemplate> activeTemplates = shiftService.getActiveShiftTemplates();
        model.addAttribute("templates", activeTemplates);
        
        return "templates";
    }
    
    /**
     * Form class for shift template creation
     */
    public static class ShiftTemplateForm {
        private String name;
        private String locationId;
        private String roleId;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer breakMinutes = 0;
        private String description;
        
        // Constructors
        public ShiftTemplateForm() {}
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getLocationId() { return locationId; }
        public void setLocationId(String locationId) { this.locationId = locationId; }
        
        public String getRoleId() { return roleId; }
        public void setRoleId(String roleId) { this.roleId = roleId; }
        
        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
        
        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
        
        public Integer getBreakMinutes() { return breakMinutes; }
        public void setBreakMinutes(Integer breakMinutes) { this.breakMinutes = breakMinutes; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}