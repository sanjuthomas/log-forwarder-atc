package com.logforwarder.atc.service;

import com.logforwarder.atc.domain.FleetChangeType;
import com.logforwarder.atc.dto.FleetChangeEvent;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FleetEventBroadcasterTest {

    @Test
    void subscribeReturnsOpenEmitter() {
        FleetEventBroadcaster broadcaster = new FleetEventBroadcaster();

        SseEmitter emitter = broadcaster.subscribe();

        assertNotNull(emitter);
    }

    @Test
    void broadcastDeliversFleetChangeEvent() throws Exception {
        FleetEventBroadcaster broadcaster = new FleetEventBroadcaster();
        SseEmitter emitter = broadcaster.subscribe();
        UUID id = UUID.randomUUID();
        FleetChangeEvent event = new FleetChangeEvent(
                FleetChangeType.REGISTERED,
                id,
                "app-server-01",
                12345
        );

        assertDoesNotThrow(() -> broadcaster.broadcast(event));
        emitter.complete();
    }

    @Test
    void broadcastRemovesEmittersThatFailToSend() throws Exception {
        FleetEventBroadcaster broadcaster = new FleetEventBroadcaster();
        SseEmitter failingEmitter = new SseEmitter(0L) {
            @Override
            public void send(Object object) throws IOException {
                throw new IOException("broken stream");
            }
        };
        var emittersField = FleetEventBroadcaster.class.getDeclaredField("emitters");
        emittersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        var emitters = (java.util.List<SseEmitter>) emittersField.get(broadcaster);
        emitters.add(failingEmitter);

        assertDoesNotThrow(() -> broadcaster.broadcast(new FleetChangeEvent(
                FleetChangeType.DEREGISTERED,
                UUID.randomUUID(),
                "app-server-01",
                12345
        )));
    }

    @Test
    void fleetChangeEventFactoryMethodsPopulateFields() {
        UUID id = UUID.randomUUID();

        FleetChangeEvent registered = FleetChangeEvent.registered(
                new com.logforwarder.atc.dto.RegistrationResponse(
                        id,
                        "app-server-01",
                        12345,
                        java.time.Instant.parse("2026-06-11T14:30:00Z"),
                        8080,
                        java.time.Instant.parse("2026-06-11T14:30:05Z"),
                        com.logforwarder.atc.domain.Reachability.REACHABLE,
                        true
                )
        );
        FleetChangeEvent deregistered = FleetChangeEvent.deregistered(
                new com.logforwarder.atc.dto.DeregisteredInstance(id, "app-server-01", 12345)
        );

        assertEquals(FleetChangeType.REGISTERED, registered.type());
        assertEquals(FleetChangeType.DEREGISTERED, deregistered.type());
        assertEquals(id, registered.instanceId());
        assertEquals(12345, deregistered.processId());
    }
}
