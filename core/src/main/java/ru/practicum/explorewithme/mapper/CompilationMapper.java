package ru.practicum.explorewithme.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explorewithme.dto.request.CompilationRequestDto;
import ru.practicum.explorewithme.dto.response.CompilationResponseDto;
import ru.practicum.explorewithme.model.Compilation;
import ru.practicum.explorewithme.model.Event;

import java.util.Set;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CompilationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", source = "eventEntities")
    Compilation dtoToCompilation(CompilationRequestDto compilationRequestDto, Set<Event> eventEntities);

    CompilationResponseDto compilationToDto(Compilation compilation);

}
