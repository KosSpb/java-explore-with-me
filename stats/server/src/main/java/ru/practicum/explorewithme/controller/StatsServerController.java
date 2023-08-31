package ru.practicum.explorewithme.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.StatsRequestDto;
import ru.practicum.explorewithme.StatsResponseDto;
import ru.practicum.explorewithme.service.StatsServerService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
public class StatsServerController {
    private final StatsServerService statsServerService;
    private final String dateTimePattern = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    public StatsServerController(StatsServerService statsServerService) {
        this.statsServerService = statsServerService;
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public StatsResponseDto registerEndpointHit(@RequestBody @Valid StatsRequestDto statsRequestDto) {
        return statsServerService.registerEndpointHit(statsRequestDto);
    }

    @GetMapping("/stats")
    public Collection<StatsResponseDto> getStats(@RequestParam(value = "start")
                                                 @DateTimeFormat(pattern = dateTimePattern) LocalDateTime start,
                                                 @RequestParam(value = "end")
                                                 @DateTimeFormat(pattern = dateTimePattern) LocalDateTime end,
                                                 @RequestParam(value = "uris", required = false) List<String> uris,
                                                 @RequestParam(value = "unique", defaultValue = "false") boolean unique) {
        return statsServerService.getStats(start, end, uris, unique);
    }
}
