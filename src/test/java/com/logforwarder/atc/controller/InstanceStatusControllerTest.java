package com.logforwarder.atc.controller;

import com.logforwarder.atc.service.FleetEventBroadcaster;
import com.logforwarder.atc.service.InstanceStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@WebMvcTest(InstanceStatusController.class)
class InstanceStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InstanceStatusService statusService;

    @MockBean
    private FleetEventBroadcaster fleetEventBroadcaster;

    @Test
    void streamFleetEventsStartsAsyncSubscription() throws Exception {
        SseEmitter emitter = new SseEmitter(0L);
        when(fleetEventBroadcaster.subscribe()).thenReturn(emitter);

        mockMvc.perform(get("/api/instances/events"))
                .andExpect(request().asyncStarted());

        verify(fleetEventBroadcaster).subscribe();
        emitter.complete();
    }
}
