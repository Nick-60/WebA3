package com.example.leave.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ApprovalFlowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loginAndGetToken(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", username, "password", password));
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        String json = res.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode node = objectMapper.readTree(json);
        String token = node.get("accessToken").asText();
        assertThat(token).isNotBlank();
        return token;
    }

    @Test
    void employeeCreatesThenManagerApproves() throws Exception {
        // 1) 员工登录并提交请假申请
        String empToken = loginAndGetToken("emp", "emp123");

        Map<String, Object> req = Map.of(
                "leave_type", "ANNUAL",
                "start_date", LocalDate.of(2025, 3, 1).toString(),
                "end_date", LocalDate.of(2025, 3, 3).toString(),
                "comment", "annual leave"
        );
        MvcResult createRes = mockMvc.perform(post("/api/leave/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + empToken)
                        .content(objectMapper.writeValueAsBytes(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        String createJson = createRes.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode createdNode = objectMapper.readTree(createJson);
        long leaveId = createdNode.get("data").get("id").asLong();
        assertThat(leaveId).isGreaterThan(0L);

        // 2) 经理登录并审批通过
        String mgrToken = loginAndGetToken("mgr", "mgr123");
        Map<String, Object> approveBody = Map.of("comment", "OK");
        mockMvc.perform(patch("/api/leave/" + leaveId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + mgrToken)
                        .content(objectMapper.writeValueAsBytes(approveBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }
}

