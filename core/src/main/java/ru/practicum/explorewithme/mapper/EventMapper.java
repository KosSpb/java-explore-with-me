package ru.practicum.explorewithme.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.explorewithme.StatsResponseDto;
import ru.practicum.explorewithme.dto.request.EventRequestDto;
import ru.practicum.explorewithme.dto.response.EventFullInfoResponseDto;
import ru.practicum.explorewithme.dto.response.EventResponseDto;
import ru.practicum.explorewithme.model.*;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "categoryEntity")
    @Mapping(target = "initiator", source = "initiatorEntity")
    @Mapping(target = "location", source = "locationEntity")
    Event dtoToEvent(EventRequestDto eventRequestDto, Category categoryEntity,
                     User initiatorEntity, Location locationEntity);

    EventResponseDto eventToShortDto(Event event);

    EventFullInfoResponseDto eventToFullDto(Event event);

    @Mapping(target = "views", source = "hits")
    @Mapping(target = "eventId", source = "uri", qualifiedByName = "mapUriToEventId")
    EventViews statsDtoToEventViews(StatsResponseDto statsResponseDto);

    @Named(value = "mapUriToEventId")
    default Long mapUriToEventId(String uri) {
        return Long.parseLong(String.valueOf(uri.charAt(uri.length() - 1)));
    }

}
