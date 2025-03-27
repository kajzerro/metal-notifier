package com.ing.interview.metalnotifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ing.interview.metalnotifier.model.MetalPrice;
import com.ing.interview.metalnotifier.model.NotificationTemplate;
import com.ing.interview.metalnotifier.model.Operator;
import com.ing.interview.metalnotifier.model.Recipient;
import com.ing.interview.metalnotifier.model.Rule;
import com.ing.interview.metalnotifier.repository.NotificationTemplateRepository;
import com.ing.interview.metalnotifier.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MetalNotifierIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationTemplateRepository templateRepository;

    @MockBean
    EmailService emailService;

    @BeforeEach
    public void setup() {
        templateRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testEndToEndFlow_CreateTemplateAndProcessNotification() throws Exception {
        // 1. Utwórz szablon z regułami
        NotificationTemplate template = new NotificationTemplate();
        template.setTitle("Gold price alert");
        template.setContent("Gold price has changed significantly!");
        template.setRecipients(Arrays.asList(new Recipient("testtest@ingtest.pl"), new Recipient("testtest2@ingtest.pl")));

        // Dodaj reguły
        List<Rule> rules = new ArrayList<>();
        Rule rule1 = new Rule();
        rule1.setOperator(Operator.ITEM_IS);
        rule1.setOperand("gold");
        rule1.setTemplate(template);
        rules.add(rule1);
        
        Rule rule2 = new Rule();
        rule2.setOperator(Operator.PRICE_IS_GREATER_THAN);
        rule2.setOperand("1500.00");
        rule2.setTemplate(template);
        rules.add(rule2);
        
        template.setRules(rules);
        
        // Zapisz szablon poprzez API
        NotificationTemplate createdTemplate = createNewTemplateThrueAPI(template);
        Long templateId = createdTemplate.getId();
        
        // 2. Sprawdź, czy szablon został prawidłowo zapisany w bazie danych
        ensureTemplateSuccessfullySavedThrueAPI(templateId, "Gold price alert", 2);

        // 3. Wyślij powiadomienie o cenie złota, które powinno spowodować wysłanie powiadomienia
        sendNewPriceNotification(new MetalPrice("gold", BigDecimal.valueOf(1600.00)));

        verify(emailService, times(2)).sendEmail(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.reset(emailService);
        // 4. Wyślij powiadomienie o cenie złota, które NIE powinno spowodować wysłania powiadomienia

        sendNewPriceNotification(new MetalPrice("gold", BigDecimal.valueOf(1400.00)));
        verifyNoInteractions(emailService);

        // 5. Zaktualizuj szablon
        createdTemplate.setContent("Updated content");
        String updatedTemplateJson = objectMapper.writeValueAsString(createdTemplate);
        
        mockMvc.perform(put("/api/templates/{id}", templateId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedTemplateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("Updated content")));
        
        // 6. Usuń szablon
        mockMvc.perform(delete("/api/templates/{id}", templateId))
                .andExpect(status().isOk());
        
        // 7. Sprawdź, czy szablon został usunięty
        mockMvc.perform(get("/api/templates/{id}", templateId))
                .andExpect(status().isNotFound());
    }

    private void sendNewPriceNotification(MetalPrice highGoldPrice) throws Exception {
        String priceJson = objectMapper.writeValueAsString(highGoldPrice);

        mockMvc.perform(post("/api/new-price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(priceJson))
                .andExpect(status().isOk());
    }

    private void ensureTemplateSuccessfullySavedThrueAPI(Long templateId, String gold_price_alert, int size) throws Exception {
        mockMvc.perform(get("/api/templates/{id}", templateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(templateId.intValue())))
                .andExpect(jsonPath("$.title", is(gold_price_alert)))
                .andExpect(jsonPath("$.rules", hasSize(size)));
    }

    private NotificationTemplate createNewTemplateThrueAPI(NotificationTemplate template) throws Exception {
        String templateJson = objectMapper.writeValueAsString(template);

        String responseJson = mockMvc.perform(post("/api/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(templateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Gold price alert")))
                .andExpect(jsonPath("$.rules", hasSize(2)))
                .andReturn().getResponse().getContentAsString();

        NotificationTemplate createdTemplate = objectMapper.readValue(responseJson, NotificationTemplate.class);
        return createdTemplate;
    }

    @Test
    @Transactional
    public void testValidation_CreateTemplate_InvalidData() throws Exception {
        // 1. Próba utworzenia szablonu z pustym tytułem
        NotificationTemplate template = new NotificationTemplate();
        template.setTitle(""); // Pusty tytuł
        template.setContent("Some content");
        
        String templateJson = objectMapper.writeValueAsString(template);
        
        mockMvc.perform(post("/api/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(templateJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors", not(empty())));
        
        // 2. Próba utworzenia szablonu z pustą treścią
        template.setTitle("Valid title");
        template.setContent(""); // Pusta treść
        
        templateJson = objectMapper.writeValueAsString(template);
        
        mockMvc.perform(post("/api/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(templateJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors", not(empty())));
    }

    @Test
    public void testValidation_ProcessNewPrice_InvalidData() throws Exception {
        // 1. Próba przetworzenia ceny z nieprawidłowym typem metalu
        MetalPrice invalidMetalPrice = new MetalPrice("invalid", BigDecimal.valueOf(1000.00));
        String invalidPriceJson = objectMapper.writeValueAsString(invalidMetalPrice);
        
        mockMvc.perform(post("/api/new-price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPriceJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors", not(empty())));
        
        // 2. Próba przetworzenia ceny z nieprawidłowym formatem ceny
        MetalPrice invalidPriceFormat = new MetalPrice("gold", BigDecimal.valueOf(1));
        String invalidFormatJson = objectMapper.writeValueAsString(invalidPriceFormat);
        
        mockMvc.perform(post("/api/new-price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidFormatJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors", not(empty())));
    }
}