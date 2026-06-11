package com.logforwarder.atc.controller;

import com.logforwarder.atc.dto.DeregistrationRequest;
import com.logforwarder.atc.dto.RegistrationRequest;
import com.logforwarder.atc.dto.RegistrationResponse;
import com.logforwarder.atc.service.InstanceRegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@WebMvcTest(InstanceRegistrationController.class)
class InstanceRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InstanceRegistrationService registrationService;

    @Test
    void registerReturnsCreatedForNewInstance() throws Exception {
        UUID id = UUID.randomUUID();
        when(registrationService.register(any(RegistrationRequest.class)))
                .thenReturn(new RegistrationResponse(
                        id,
                        "host-1",
                        999,
                        Instant.parse("2026-06-11T16:00:00Z"),
                        8081,
                        8082,
                        8083,
                        Instant.parse("2026-06-11T16:01:00Z"),
                        com.logforwarder.atc.domain.Reachability.UNKNOWN,
                        true
                ));

        mockMvc.perform(put("/api/instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hostname": "host-1",
                                  "startTime": "2026-06-11T16:00:00Z",
                                  "healthPort": 8081,
                                  "readyPort": 8082,
                                  "metricsPort": 8083,
                                  "pid": 999
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.hostname").value("host-1"))
                .andExpect(jsonPath("$.created").value(true));
    }

    @Test
    void deregisterReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hostname": "host-1",
                                  "pid": 999
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void deregisterReturnsNotFoundWhenMissing() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "not found"))
                .when(registrationService)
                .deregister(any(DeregistrationRequest.class));

        mockMvc.perform(delete("/api/instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hostname": "missing-host",
                                  "pid": 1
                                }
                                """))
                .andExpect(status().isNotFound());
    }
}
