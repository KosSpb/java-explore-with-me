package ru.practicum.explorewithme.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.StatsRequestDto;
import ru.practicum.explorewithme.StatsResponseDto;
import ru.practicum.explorewithme.dao.StatsServerRepository;
import ru.practicum.explorewithme.exception.IncorrectRequestException;
import ru.practicum.explorewithme.mapper.StatsServerMapper;
import ru.practicum.explorewithme.model.HitCount;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StatsServerService {
    private final StatsServerRepository statsServerRepository;
    private final StatsServerMapper mapper;

    @Autowired
    public StatsServerService(StatsServerRepository statsServerRepository, StatsServerMapper mapper) {
        this.statsServerRepository = statsServerRepository;
        this.mapper = mapper;
    }

    public StatsResponseDto registerEndpointHit(StatsRequestDto statsRequestDto) {
        return mapper.statUnitToDto(statsServerRepository.save(mapper.dtoToStatUnit(statsRequestDto)));
    }

    @Transactional(readOnly = true)
    public Collection<StatsResponseDto> getStats(LocalDateTime start, LocalDateTime end,
                                                 List<String> uris, boolean unique) {

        if (start != null && end != null && start.isAfter(end)) {
            throw new IncorrectRequestException("get stats from statistics service: " +
                    "Start time cannot be after end time");
        }

        List<HitCount> countedHits =
                statsServerRepository.countHitsForListedUrisInTimeRangeConsideringIpUniqueness(start, end, uris, unique);

        return countedHits.stream()
                .map(mapper::hitCountToDto)
                .collect(Collectors.toList());
    }
}
