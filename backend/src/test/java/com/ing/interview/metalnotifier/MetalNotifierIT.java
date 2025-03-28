package com.ing.interview.metalnotifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ing.interview.metalnotifier.model.MetalPrice;
import com.ing.interview.metalnotifier.entity.NotificationTemplate;
import com.ing.interview.metalnotifier.model.Operator;
import com.ing.interview.metalnotifier.entity.Recipient;
import com.ing.interview.metalnotifier.entity.Rule;
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
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
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

        NotificationTemplate template = new NotificationTemplate();
        template.setTitle("Gold price alert");
        template.setContent("Gold price has changed significantly!");
        template.setRecipients(Arrays.asList(new Recipient("testtest@ingtest.pl"), new Recipient("testtest2@ingtest.pl")));

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
        
        // Save template thrue API
        NotificationTemplate createdTemplate = createNewTemplateThrueAPI(template);
        Long templateId = createdTemplate.getId();
        
        // 2. Check if template exist in db thru API
        ensureTemplateSuccessfullySavedThrueAPI(templateId, "Gold price alert", 2);

        // 3.Send gold price notificaiton which should trigger email
        sendNewPriceNotification(new MetalPrice("gold", BigDecimal.valueOf(1600.00)));

        verify(emailService, times(2)).sendEmail(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.reset(emailService);
        // 4. .Send gold price notificaiton which should not trigger email

        sendNewPriceNotification(new MetalPrice("gold", BigDecimal.valueOf(1400.00)));
        verifyNoInteractions(emailService);

        // 5. Update template
        createdTemplate.setContent("Updated content");
        String updatedTemplateJson = objectMapper.writeValueAsString(createdTemplate);
        
        mockMvc.perform(put("/api/templates/{id}", templateId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedTemplateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("Updated content")));
        
        // 6. Delete tempalte
        mockMvc.perform(delete("/api/templates/{id}", templateId))
                .andExpect(status().isNoContent());
        
        // 7. Check if deleted
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

}