package com.ceramiflow.controller;

import com.ceramiflow.service.realtime.RealtimeEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/stream")
public class SseController {
    private final RealtimeEventPublisher p;

    public SseController(RealtimeEventPublisher p) {
        this.p = p;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return p.subscribe();
    }
}