package com.logforwarder.atc.config;

import com.logforwarder.atc.controller.InstanceRegistrationController;
import com.logforwarder.atc.controller.InstanceStatusController;
import com.logforwarder.atc.service.DeregisteredInstanceService;
import com.logforwarder.atc.service.FleetEventBroadcaster;
import com.logforwarder.atc.service.FleetStatsService;
import com.logforwarder.atc.service.InstancePollingService;
import com.logforwarder.atc.service.InstanceRegistrationService;
import com.logforwarder.atc.service.InstanceStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        InstanceRegistrationController.class,
        InstanceStatusController.class,
        GlobalExceptionHandler.class
})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InstanceRegistrationService registrationService;

    @MockitoBean
    private InstancePollingService pollingService;

    @MockitoBean
    private FleetEventBroadcaster fleetEventBroadcaster;

    @MockitoBean
    private InstanceStatusService statusService;

    @MockitoBean
    private FleetStatsService fleetStatsService;

    @MockitoBean
    private DeregisteredInstanceService deregisteredInstanceService;

    @Test
    void notFoundReturnsApiErrorJson() throws Exception {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(statusService.getInstance(id))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Instance not found"));

        mockMvc.perform(get("/api/instances/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Instance not found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void validationErrorReturnsApiErrorJson() throws Exception {
        mockMvc.perform(put("/api/instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hostname": "",
                                  "port": 8080,
                                  "process_id": 1,
                                  "timestamp": "2026-06-11T14:30:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("hostname: must not be blank"))
                .andExpect(jsonPath("$.status").value(400));
    }
}
