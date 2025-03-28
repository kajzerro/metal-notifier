// Testy jednostkowe dla NotificationService
package com.ing.interview.metalnotifier.service;

import com.ing.interview.metalnotifier.entity.NotificationTemplate;
import com.ing.interview.metalnotifier.entity.Recipient;
import com.ing.interview.metalnotifier.entity.Rule;
import com.ing.interview.metalnotifier.model.*;
import com.ing.interview.metalnotifier.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationTemplate template1;
    private NotificationTemplate template2;
    private Rule goldRule;
    private Rule silverRule;
    private Rule priceRule;
    private Recipient recipient1;
    private Recipient recipient2;

    @BeforeEach
    public void setup() {
        // Przygotowanie danych testowych
        template1 = new NotificationTemplate();
        template1.setId(1L);
        template1.setTitle("Gold price alert");
        template1.setContent("Gold price has changed!");

        template2 = new NotificationTemplate();
        template2.setId(2L);
        template2.setTitle("Silver price alert");
        template2.setContent("Silver price has changed!");

        goldRule = new Rule();
        goldRule.setId(1L);
        goldRule.setOperator(Operator.ITEM_IS);
        goldRule.setOperand("gold");
        goldRule.setTemplate(template1);

        silverRule = new Rule();
        silverRule.setId(2L);
        silverRule.setOperator(Operator.ITEM_IS);
        silverRule.setOperand("silver");
        silverRule.setTemplate(template2);

        priceRule = new Rule();
        priceRule.setId(3L);
        priceRule.setOperator(Operator.PRICE_IS_GREATER_THAN);
        priceRule.setOperand("1000.00");
        priceRule.setTemplate(template1);

        recipient1 = new Recipient();
        recipient1.setId(1L);
        recipient1.setEmail("test1@example.com");
        recipient1.setTemplate(template1);

        recipient2 = new Recipient();
        recipient2.setId(2L);
        recipient2.setEmail("test2@example.com");
        recipient2.setTemplate(template2);

        List<Rule> rules1 = new ArrayList<>();
        rules1.add(goldRule);
        rules1.add(priceRule);
        template1.setRules(rules1);

        List<Rule> rules2 = new ArrayList<>();
        rules2.add(silverRule);
        template2.setRules(rules2);

        List<Recipient> recipients1 = new ArrayList<>();
        recipients1.add(recipient1);
        template1.setRecipients(recipients1);

        List<Recipient> recipients2 = new ArrayList<>();
        recipients2.add(recipient2);
        template2.setRecipients(recipients2);
    }

    @Test
    public void testProcessNewPrice_MatchingTemplate_SendsNotification() {
        // Given
        MetalPrice metalPrice = new MetalPrice("gold", BigDecimal.valueOf(1500.00));
        when(templateRepository.findAll()).thenReturn(Arrays.asList(template1, template2));

        // When
        notificationService.processNewPrice(metalPrice);

        // Then
        verify(emailService, times(1)).sendEmail(recipient1, template1, metalPrice);
        verify(emailService, never()).sendEmail(recipient2, template2, metalPrice);
    }

    @Test
    public void testProcessNewPrice_NoMatchingTemplate_DoesNotSendNotification() {
        // Given
        MetalPrice metalPrice = new MetalPrice("gold", BigDecimal.valueOf(500.00)); // Cena poniżej warunku
        when(templateRepository.findAll()).thenReturn(Arrays.asList(template1, template2));

        // When
        notificationService.processNewPrice(metalPrice);

        // Then
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    public void testSaveTemplate_NewTemplate_SetsRelations() {
        // Given
        NotificationTemplate newTemplate = new NotificationTemplate();
        newTemplate.setTitle("New Template");
        newTemplate.setContent("New Content");

        Rule newRule = new Rule();
        newRule.setOperator(Operator.ITEM_IS);
        newRule.setOperand("platinum");

        Recipient newRecipient = new Recipient();
        newRecipient.setEmail("new@example.com");

        List<Rule> rules = new ArrayList<>();
        rules.add(newRule);
        newTemplate.setRules(rules);

        List<Recipient> recipients = new ArrayList<>();
        recipients.add(newRecipient);
        newTemplate.setRecipients(recipients);

        when(templateRepository.save(any(NotificationTemplate.class))).thenAnswer(invocation -> {
            NotificationTemplate savedTemplate = invocation.getArgument(0);
            savedTemplate.setId(3L);
            return savedTemplate;
        });

        // When
        NotificationTemplate result = notificationService.saveTemplate(newTemplate);

        // Then
        ArgumentCaptor<NotificationTemplate> templateCaptor = ArgumentCaptor.forClass(NotificationTemplate.class);
        verify(templateRepository).save(templateCaptor.capture());

        NotificationTemplate capturedTemplate = templateCaptor.getValue();
        assertEquals("New Template", capturedTemplate.getTitle());
        assertEquals(1, capturedTemplate.getRules().size());
        assertEquals(1, capturedTemplate.getRecipients().size());

        Rule capturedRule = capturedTemplate.getRules().get(0);
        assertEquals(Operator.ITEM_IS, capturedRule.getOperator());
        assertEquals("platinum", capturedRule.getOperand());
        assertSame(capturedTemplate, capturedRule.getTemplate());

        Recipient capturedRecipient = capturedTemplate.getRecipients().get(0);
        assertEquals("new@example.com", capturedRecipient.getEmail());
        assertSame(capturedTemplate, capturedRecipient.getTemplate());

        assertNotNull(result.getId());
        assertEquals(3L, result.getId());
    }

    @Test
    public void testGetAllTemplates_ReturnsAllTemplates() {
        // Given
        when(templateRepository.findAll()).thenReturn(Arrays.asList(template1, template2));

        // When
        List<NotificationTemplate> result = notificationService.getAllTemplates();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(template1));
        assertTrue(result.contains(template2));
        verify(templateRepository).findAll();
    }

    @Test
    public void testGetTemplateById_ExistingId_ReturnsTemplate() {
        // Given
        Long id = 1L;
        when(templateRepository.findById(id)).thenReturn(Optional.of(template1));

        // When
        NotificationTemplate result = notificationService.getTemplateById(id);

        // Then
        assertNotNull(result);
        assertEquals(template1, result);
        verify(templateRepository).findById(id);
    }

    @Test
    public void testGetTemplateById_NonExistingId_ReturnsNull() {
        // Given
        Long id = 999L;
        when(templateRepository.findById(id)).thenReturn(Optional.empty());

        // When
        NotificationTemplate result = notificationService.getTemplateById(id);

        // Then
        assertNull(result);
        verify(templateRepository).findById(id);
    }

    @Test
    public void testDeleteTemplate() {
        // Given
        Long id = 1L;
        doNothing().when(templateRepository).deleteById(id);

        // When
        notificationService.deleteTemplate(id);

        // Then
        verify(templateRepository).deleteById(id);
    }
}