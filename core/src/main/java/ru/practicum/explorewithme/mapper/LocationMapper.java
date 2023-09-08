package ru.practicum.explorewithme.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import ru.practicum.explorewithme.dto.request.LocationRequestDto;
import ru.practicum.explorewithme.dto.response.LocationResponseDto;
import ru.practicum.explorewithme.model.Location;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface LocationMapper {

    Location dtoToLocation(LocationRequestDto locationRequestDto);

    LocationResponseDto locationToDto(Location location);

}
