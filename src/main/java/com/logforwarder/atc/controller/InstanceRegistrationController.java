package com.logforwarder.atc.controller;

import com.logforwarder.atc.dto.DeregistrationRequest;
import com.logforwarder.atc.dto.DeregisteredInstance;
import com.logforwarder.atc.dto.FleetChangeEvent;
import com.logforwarder.atc.dto.RegistrationRequest;
import com.logforwarder.atc.dto.RegistrationResponse;
import com.logforwarder.atc.service.FleetEventBroadcaster;
import com.logforwarder.atc.service.InstancePollingService;
import com.logforwarder.atc.service.InstanceRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instances")
public class InstanceRegistrationController {

    private final InstanceRegistrationService registrationService;
    private final InstancePollingService pollingService;
    private final FleetEventBroadcaster fleetEventBroadcaster;

    public InstanceRegistrationController(
            InstanceRegistrationService registrationService,
            InstancePollingService pollingService,
            FleetEventBroadcaster fleetEventBroadcaster
    ) {
        this.registrationService = registrationService;
        this.pollingService = pollingService;
        this.fleetEventBroadcaster = fleetEventBroadcaster;
    }

    @PutMapping
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        RegistrationResponse response = registrationService.register(request);
        pollingService.pollInstance(response.id());
        RegistrationResponse refreshed = registrationService.getRegistrationResponse(response.id(), response.created());
        fleetEventBroadcaster.broadcast(FleetChangeEvent.registered(refreshed));
        HttpStatus status = refreshed.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(refreshed);
    }

    @DeleteMapping
    public ResponseEntity<Void> deregister(@Valid @RequestBody DeregistrationRequest request) {
        DeregisteredInstance removed = registrationService.deregister(request);
        fleetEventBroadcaster.broadcast(FleetChangeEvent.deregistered(removed));
        return ResponseEntity.noContent().build();
    }
}
