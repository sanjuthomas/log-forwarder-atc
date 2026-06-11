package com.logforwarder.atc.controller;

import com.logforwarder.atc.dto.DeregistrationRequest;
import com.logforwarder.atc.dto.RegistrationRequest;
import com.logforwarder.atc.dto.RegistrationResponse;
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

    public InstanceRegistrationController(InstanceRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PutMapping
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        RegistrationResponse response = registrationService.register(request);
        HttpStatus status = response.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deregister(@Valid @RequestBody DeregistrationRequest request) {
        registrationService.deregister(request);
        return ResponseEntity.noContent().build();
    }
}
