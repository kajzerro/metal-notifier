package com.ing.interview.metalnotifier.controller;

import com.ing.interview.metalnotifier.model.MetalPrice;
import com.ing.interview.metalnotifier.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MetalPriceController {
    
    private final NotificationService notificationService;
    
    @Autowired
    public MetalPriceController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    @PostMapping("/new-price")
    public ResponseEntity<String> receiveNewPrice(@RequestBody @Valid MetalPrice metalPrice) {
        notificationService.processNewPrice(metalPrice);
        return ResponseEntity.ok("Notification processed");
    }
}