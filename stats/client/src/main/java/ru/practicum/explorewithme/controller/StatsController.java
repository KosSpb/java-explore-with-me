package ru.practicum.explorewithme.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.StatsRequestDto;
import ru.practicum.explorewithme.client.StatsClient;

import javax.validation.Valid;

@RestController
public class StatsController {
    private final StatsClient statsClient;

    @Autowired
    public StatsController(StatsClient statsClient) {
        this.statsClient = statsClient;
    }

    @PostMapping("/hit")
    public ResponseEntity<Object> registerEndpointHit(@RequestBody @Valid StatsRequestDto statsRequestDto) {
        return statsClient.registerEndpointHit(statsRequestDto);
    }

    @GetMapping("/stats")
    public ResponseEntity<Object> getStats(@RequestParam(value = "start") String start,
                                           @RequestParam(value = "end") String end,
                                           @RequestParam(value = "uris", required = false) String[] uris,
                                           @RequestParam(value = "unique", defaultValue = "false") boolean unique) {
        return statsClient.getStats(start, end, uris, unique);
    }
}
