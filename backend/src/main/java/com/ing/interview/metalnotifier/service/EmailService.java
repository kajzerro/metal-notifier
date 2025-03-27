package com.ing.interview.metalnotifier.service;

import com.ing.interview.metalnotifier.model.MetalPrice;
import com.ing.interview.metalnotifier.model.NotificationTemplate;
import com.ing.interview.metalnotifier.model.Recipient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {
    
    public void sendEmail(Recipient recipient, NotificationTemplate template, MetalPrice metalPrice) {
        log.info("Recipient: {}", recipient.getEmail());
        log.info("Title: {}", template.getTitle());
        log.info("Content: {}", template.getContent());
        log.info("Metal notification: {} : {}", metalPrice.getPrice(), metalPrice.getItemType());
        log.info("-----------------------------------");
    }
}