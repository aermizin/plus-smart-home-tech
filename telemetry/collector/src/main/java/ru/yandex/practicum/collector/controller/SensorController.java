package ru.yandex.practicum.collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.collector.service.KafkaSenderService;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
public class SensorController {
    private final KafkaSenderService kafkaSenderService;

    @PostMapping("/sensors")
    public ResponseEntity<Void> collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        kafkaSenderService.sendSensorEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
