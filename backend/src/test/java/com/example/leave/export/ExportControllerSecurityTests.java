package com.example.leave.export;

import com.example.leave.controller.ExportController;
import com.example.leave.service.ExportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.mockito.Mockito.doAnswer;

import com.example.leave.config.SecurityConfig;
import com.example.leave.security.JwtAuthenticationFilter;
import com.example.common.ApiResponse;

@WebMvcTest(
        controllers = ExportController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@AutoConfigureMockMvc
@Import(ExportControllerSecurityTests.TestSecurityConfig.class)
public class ExportControllerSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExportService exportService;

    @Test
    @WithMockUser(username = "hr", roles = {"HR"})
    void hrCanExportXlsx() throws Exception {
        // 模拟服务写入少量内容即可
        doAnswer(invocation -> {
            var out = invocation.getArgument(0, java.io.OutputStream.class);
            out.write(new byte[]{0x50, 0x4B, 0x03, 0x04}); // ZIP magic of XLSX
            out.flush();
            return null;
        }).when(exportService).exportHrReport(org.mockito.Mockito.any(), org.mockito.Mockito.any(), org.mockito.Mockito.any(), org.mockito.Mockito.any());

        mockMvc.perform(get("/api/leave/hr/export")
                        .param("from", "2024-01-01")
                        .param("to", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")));
    }

    @Test
    @WithMockUser(username = "emp", roles = {"EMPLOYEE"})
    void nonHrForbidden() throws Exception {
        mockMvc.perform(get("/api/leave/hr/export")
                        .param("from", "2024-01-01")
                        .param("to", "2024-12-31"))
                .andExpect(status().isForbidden());
    }

    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                AuthenticationEntryPoint authenticationEntryPoint,
                                                AccessDeniedHandler accessDeniedHandler) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**", "/api/health").permitAll()
                    .anyRequest().authenticated()
                );
            return http.build();
        }

        @Bean
        AuthenticationEntryPoint authenticationEntryPoint() {
            return (request, response, authException) -> {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json;charset=UTF-8");
                String body = toJson(ApiResponse.error(401, "未认证", "UNAUTHORIZED"));
                response.getWriter().write(body);
            };
        }

        @Bean
        AccessDeniedHandler accessDeniedHandler() {
            return (request, response, accessDeniedException) -> {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json;charset=UTF-8");
                String body = toJson(ApiResponse.error(403, "无权限", "FORBIDDEN"));
                response.getWriter().write(body);
            };
        }

        private static String toJson(Object obj) {
            try {
                return new ObjectMapper().writeValueAsString(obj);
            } catch (Exception e) {
                return "{}";
            }
        }
    }
}

