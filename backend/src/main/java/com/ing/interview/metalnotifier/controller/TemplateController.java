package com.ing.interview.metalnotifier.controller;

import com.ing.interview.metalnotifier.entity.NotificationTemplate;
import com.ing.interview.metalnotifier.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    
    private final NotificationService notificationService;
    
    @Autowired
    public TemplateController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    @GetMapping
    public ResponseEntity<List<NotificationTemplate>> getAllTemplates() {
        List<NotificationTemplate> templates = notificationService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NotificationTemplate> getTemplateById(@PathVariable Long id) {
        NotificationTemplate template = notificationService.getTemplateById(id);
        if (template != null) {
            return ResponseEntity.ok(template);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<NotificationTemplate> createTemplate(@RequestBody  @Valid NotificationTemplate template) {
        NotificationTemplate savedTemplate = notificationService.saveTemplate(template);
        return ResponseEntity.ok(savedTemplate);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<NotificationTemplate> updateTemplate(@PathVariable Long id, @RequestBody NotificationTemplate template) {
        NotificationTemplate existingTemplate = notificationService.getTemplateById(id);
        if (existingTemplate == null) {
            return ResponseEntity.notFound().build();
        }
        template.setId(id);
        NotificationTemplate updatedTemplate = notificationService.saveTemplate(template);
        return ResponseEntity.ok(updatedTemplate);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {

        NotificationTemplate existingTemplate = notificationService.getTemplateById(id);
        if (existingTemplate == null) {
            return ResponseEntity.notFound().build();
        }
        notificationService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
