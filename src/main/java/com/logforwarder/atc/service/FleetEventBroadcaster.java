package com.logforwarder.atc.service;

import com.logforwarder.atc.dto.FleetChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class FleetEventBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(FleetEventBroadcaster.class);

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> removeEmitter(emitter));
        emitter.onTimeout(() -> removeEmitter(emitter));
        emitter.onError(ex -> removeEmitter(emitter));
        return emitter;
    }

    public void broadcast(FleetChangeEvent event) {
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("fleet-change")
                        .data(event));
            } catch (IOException ex) {
                log.debug("Removing fleet event subscriber after send failure: {}", ex.getMessage());
                deadEmitters.add(emitter);
            }
        }
        deadEmitters.forEach(this::removeEmitter);
    }

    private void removeEmitter(SseEmitter emitter) {
        emitters.remove(emitter);
        emitter.complete();
    }
}
