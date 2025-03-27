package com.ing.interview.metalnotifier.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ing.interview.metalnotifier.model.NotificationTemplate;
import com.ing.interview.metalnotifier.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TemplateControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TemplateController templateController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(templateController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetAllTemplates_ReturnsListOfTemplates() throws Exception {
        // Given
        NotificationTemplate template1 = new NotificationTemplate();
        template1.setId(1L);
        template1.setTitle("Template 1");
        template1.setContent("Content 1");

        NotificationTemplate template2 = new NotificationTemplate();
        template2.setId(2L);
        template2.setTitle("Template 2");
        template2.setContent("Content 2");

        List<NotificationTemplate> templates = Arrays.asList(template1, template2);
        when(notificationService.getAllTemplates()).thenReturn(templates);

        // When & Then
        mockMvc.perform(get("/api/templates")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Template 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Template 2")));

        verify(notificationService).getAllTemplates();
    }

    @Test
    public void testGetTemplateById_ExistingId_ReturnsTemplate() throws Exception {
        // Given
        Long id = 1L;
        NotificationTemplate template = new NotificationTemplate();
        template.setId(id);
        template.setTitle("Test Template");
        template.setContent("Test Content");

        when(notificationService.getTemplateById(id)).thenReturn(template);

        // When & Then
        mockMvc.perform(get("/api/templates/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Template")))
                .andExpect(jsonPath("$.content", is("Test Content")));

        verify(notificationService).getTemplateById(id);
    }

    @Test
    public void testGetTemplateById_NonExistingId_ReturnsNotFound() throws Exception {
        // Given
        Long id = 999L;
        when(notificationService.getTemplateById(id)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/templates/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(notificationService).getTemplateById(id);
    }

    @Test
    public void testCreateTemplate_ValidData_ReturnsCreatedTemplate() throws Exception {
        // Given
        NotificationTemplate template = new NotificationTemplate();
        template.setTitle("New Template");
        template.setContent("New Content");

        NotificationTemplate savedTemplate = new NotificationTemplate();
        savedTemplate.setId(1L);
        savedTemplate.setTitle("New Template");
        savedTemplate.setContent("New Content");

        when(notificationService.saveTemplate(any(NotificationTemplate.class))).thenReturn(savedTemplate);

        // When & Then
        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("New Template")))
                .andExpect(jsonPath("$.content", is("New Content")));

        verify(notificationService).saveTemplate(any(NotificationTemplate.class));
    }

    @Test
    public void testCreateTemplate_InvalidData_ReturnsBadRequest() throws Exception {
        // Given
        NotificationTemplate template = new NotificationTemplate();
        // Brak wymaganych pól title i content

        // When & Then
        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateTemplate_ExistingIdAndValidData_ReturnsUpdatedTemplate() throws Exception {
        // Given
        Long id = 1L;
        NotificationTemplate template = new NotificationTemplate();
        template.setTitle("Updated Template");
        template.setContent("Updated Content");

        NotificationTemplate existingTemplate = new NotificationTemplate();
        existingTemplate.setId(id);

        NotificationTemplate updatedTemplate = new NotificationTemplate();
        updatedTemplate.setId(id);
        updatedTemplate.setTitle("Updated Template");
        updatedTemplate.setContent("Updated Content");

        when(notificationService.getTemplateById(id)).thenReturn(existingTemplate);
        when(notificationService.saveTemplate(any(NotificationTemplate.class))).thenReturn(updatedTemplate);

        // When & Then
        mockMvc.perform(put("/api/templates/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Updated Template")))
                .andExpect(jsonPath("$.content", is("Updated Content")));

        verify(notificationService).getTemplateById(id);
        verify(notificationService).saveTemplate(any(NotificationTemplate.class));
    }

    @Test
    public void testUpdateTemplate_NonExistingId_ReturnsNotFound() throws Exception {
        // Given
        Long id = 999L;
        NotificationTemplate template = new NotificationTemplate();
        template.setTitle("Updated Template");
        template.setContent("Updated Content");

        when(notificationService.getTemplateById(id)).thenReturn(null);

        // When & Then
        mockMvc.perform(put("/api/templates/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template)))
                .andExpect(status().isNotFound());

        verify(notificationService).getTemplateById(id);
        verify(notificationService, never()).saveTemplate(any(NotificationTemplate.class));
    }

    @Test
    public void testDeleteTemplate_ExistingId_ReturnsOk() throws Exception {
        // Given
        Long id = 1L;
        NotificationTemplate existingTemplate = new NotificationTemplate();
        existingTemplate.setId(id);

        when(notificationService.getTemplateById(id)).thenReturn(existingTemplate);
        doNothing().when(notificationService).deleteTemplate(id);

        // When & Then
        mockMvc.perform(delete("/api/templates/{id}", id))
                .andExpect(status().isNoContent());

        verify(notificationService).getTemplateById(id);
        verify(notificationService).deleteTemplate(id);
    }

    @Test
    public void testDeleteTemplate_NonExistingId_ReturnsNotFound() throws Exception {
        // Given
        Long id = 999L;
        when(notificationService.getTemplateById(id)).thenReturn(null);

        // When & Then
        mockMvc.perform(delete("/api/templates/{id}", id))
                .andExpect(status().isNotFound());

        verify(notificationService).getTemplateById(id);
        verify(notificationService, never()).deleteTemplate(id);
    }
}