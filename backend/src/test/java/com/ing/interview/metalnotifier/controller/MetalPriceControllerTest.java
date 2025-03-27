package com.ing.interview.metalnotifier.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ing.interview.metalnotifier.model.MetalPrice;
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

@ExtendWith(MockitoExtension.class)
public class MetalPriceControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private MetalPriceController metalPriceController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(metalPriceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void testReceiveNewPrice_ValidData_ReturnsOk() throws Exception {
        // Given
        String requestBody = "{\"itemType\":\"gold\",\"price\":\"1234.56\"}";
        doNothing().when(notificationService).processNewPrice(any(MetalPrice.class));

        // When & Then
        mockMvc.perform(post("/api/new-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification processed"));

        verify(notificationService).processNewPrice(any(MetalPrice.class));
    }

    @Test
    public void testReceiveNewPrice_InvalidItemType_ReturnsBadRequest() throws Exception {
        // Given
        String requestBody = "{\"itemType\":\"invalid\",\"price\":\"1234.56\"}";

        // When & Then
        mockMvc.perform(post("/api/new-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testReceiveNewPrice_InvalidPriceFormat_ReturnsBadRequest() throws Exception {
        // Given
        String requestBody = "{\"itemType\":\"gold\",\"price\":\"invalid\"}";

        // When & Then
        mockMvc.perform(post("/api/new-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testReceiveNewPrice_MissingItemType_ReturnsBadRequest() throws Exception {
        // Given
        String requestBody = "{\"price\":\"1234.56\"}";

        // When & Then
        mockMvc.perform(post("/api/new-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testReceiveNewPrice_MissingPrice_ReturnsBadRequest() throws Exception {
        // Given
        String requestBody = "{\"itemType\":\"gold\"}";

        // When & Then
        mockMvc.perform(post("/api/new-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testReceiveNewPrice_EmptyRequest_ReturnsBadRequest() throws Exception {
        // Given
        String requestBody = "{}";

        // When & Then
        mockMvc.perform(post("/api/new-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}