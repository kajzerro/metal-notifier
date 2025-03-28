package com.ing.interview.metalnotifier.service;

import com.ing.interview.metalnotifier.model.MetalPrice;
import com.ing.interview.metalnotifier.entity.NotificationTemplate;
import com.ing.interview.metalnotifier.entity.Recipient;
import com.ing.interview.metalnotifier.repository.NotificationTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    
    private final NotificationTemplateRepository templateRepository;
    private final EmailService emailService;
    
    @Autowired
    public NotificationService(NotificationTemplateRepository templateRepository, EmailService emailService) {
        this.templateRepository = templateRepository;
        this.emailService = emailService;
    }
    
    public void processNewPrice(MetalPrice metalPrice) {
        List<NotificationTemplate> templates = templateRepository.findAll();
        
        for (NotificationTemplate template : templates) {
            if (template.matchesAllRules(metalPrice)) {
                for (Recipient recipient : template.getRecipients()) {
                    emailService.sendEmail(recipient, template, metalPrice);
                }
            }
        }
    }
    
    public NotificationTemplate saveTemplate(NotificationTemplate template) {
        if (template.getRecipients() != null) {
            template.getRecipients().forEach(recipient -> {
                recipient.setTemplate(template);
            });
        }

        if (template.getRules() != null) {
            template.getRules().forEach(rule -> {
                rule.setTemplate(template);
            });
        }

        return templateRepository.save(template);
    }
    
    public List<NotificationTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }
    
    public NotificationTemplate getTemplateById(Long id) {
        return templateRepository.findById(id).orElse(null);
    }
    
    public void deleteTemplate(Long id) {
        templateRepository.deleteById(id);
    }
}
