package com.edelmetalle.notifier.pricemonitor.controller;

import com.edelmetalle.notifier.pricemonitor.dto.NotificationTemplateDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@Transactional
class TemplateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<NotificationTemplateDto> jsonTester;

    @Test
    void shouldCreateTemplateWithValidData() throws Exception {
        var ruleDto = new NotificationTemplateDto.RuleDto(null, "PRICE_GREATER", "2000");
        var recipientDto = new NotificationTemplateDto.RecipientDto(null, "test@test.com");

        var dto = new NotificationTemplateDto(
                null,
                "Test Template",
                "Test Content",
                List.of(ruleDto),
                List.of(recipientDto)
        );

        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTester.write(dto).getJson()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectTemplateWithInvalidOperandForOperator() throws Exception {
        String invalidJson = """
        {
            "title": "Invalid Template",
            "content": "Test",
            "rules": [
                {
                    "operator": "ITEM_IS",
                    "operand": "999.99"
                }
            ],
            "recipients": [{"email": "test@test.com"}]
        }
        """;

        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectTemplateWithNoRules() throws Exception {
        String invalidJson = """
        {
            "title": "Invalid Template",
            "content": "Test",
            "rules": [],
            "recipients": [{"email": "test@test.com"}]
        }
        """;

        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAllTemplates() throws Exception {
        mockMvc.perform(get("/api/templates")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteTemplate() throws Exception {

        var ruleDto = new NotificationTemplateDto.RuleDto(null, "PRICE_GREATER", "2000");
        var recipientDto = new NotificationTemplateDto.RecipientDto(null, "test@test.com");

        var dto = new NotificationTemplateDto(
                null,
                "Template To Delete",
                "Content",
                List.of(ruleDto),
                List.of(recipientDto)
        );


        String response = mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTester.write(dto).getJson()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();


        Long createdId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response)
                .get("id")
                .asLong();

        mockMvc.perform(delete("/api/templates/" + createdId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/templates/" + createdId))
                .andExpect(status().isNotFound());
    }
}