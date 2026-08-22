package ru.yandex.practicum.collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.collector.model.hub.HubEvent;
import ru.yandex.practicum.collector.service.KafkaSenderService;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
public class HubController {
    private final KafkaSenderService kafkaSenderService;

    @PostMapping("/hubs")
    public ResponseEntity<Void> collectHubEvent(@Valid @RequestBody HubEvent event) {
        kafkaSenderService.sendHubEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}