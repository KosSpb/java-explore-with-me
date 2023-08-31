package ru.practicum.explorewithme.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explorewithme.StatsRequestDto;
import ru.practicum.explorewithme.StatsResponseDto;
import ru.practicum.explorewithme.model.HitCount;
import ru.practicum.explorewithme.model.StatUnit;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface StatsServerMapper {

    StatUnit dtoToStatUnit(StatsRequestDto statsRequestDto);

    StatsResponseDto statUnitToDto(StatUnit statUnit);

    @Mapping(target = "app", source = "singleApp")
    @Mapping(target = "uri", source = "singleUri")
    @Mapping(target = "hits", source = "totalHits")
    StatsResponseDto hitCountToDto(HitCount hitCount);

}
