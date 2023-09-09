package ru.practicum.explorewithme.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.dao.CompilationRepository;
import ru.practicum.explorewithme.dao.EventRepository;
import ru.practicum.explorewithme.dto.request.CompilationRequestDto;
import ru.practicum.explorewithme.dto.response.CompilationResponseDto;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.CompilationMapper;
import ru.practicum.explorewithme.model.Compilation;
import ru.practicum.explorewithme.model.Event;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Autowired
    public CompilationService(CompilationRepository compilationRepository,
                              EventRepository eventRepository,
                              CompilationMapper compilationMapper) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
        this.compilationMapper = compilationMapper;
    }

    public Collection<CompilationResponseDto> getAllEventsCompilations(Boolean pinned, int from, int size) {
        Pageable pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        List<Compilation> requestedCompilations =
                compilationRepository.findRequiredCompilations(pinned, pageRequest).getContent();

        return requestedCompilations.stream()
                .map(compilationMapper::compilationToDto)
                .collect(Collectors.toUnmodifiableList());
    }

    public CompilationResponseDto getEventsCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> {
            throw new NotFoundException("get compilation by id: Compilation with id=" + compId + " was not found");
        });
        return compilationMapper.compilationToDto(compilation);
    }

    public CompilationResponseDto createCompilationByAdmin(CompilationRequestDto compilationRequestDto) {
        if (compilationRequestDto.getPinned() == null) {
            compilationRequestDto.setPinned(false);
        }

        Set<Event> eventsOfCompilation;
        if (compilationRequestDto.getEvents() == null || compilationRequestDto.getEvents().isEmpty()) {
            eventsOfCompilation = Collections.emptySet();
        } else {
            eventsOfCompilation = eventRepository.findByIdIn(compilationRequestDto.getEvents());
        }

        return compilationMapper.compilationToDto(
                compilationRepository.save(
                        compilationMapper.dtoToCompilation(compilationRequestDto, eventsOfCompilation)));
    }

    public void deleteCompilationByAdmin(long compId) {
        compilationRepository.findById(compId).orElseThrow(() -> {
            throw new NotFoundException("deletion of compilation: Compilation with id=" + compId + " was not found");
        });
        compilationRepository.deleteById(compId);
    }

    public CompilationResponseDto updateCompilationByAdmin(CompilationRequestDto compilationRequestDto, long compId) {
        Compilation compilationToUpdate = compilationRepository.findById(compId).orElseThrow(() -> {
            throw new NotFoundException("update of compilation: Compilation with id=" + compId + " was not found");
        });

        if (compilationRequestDto.getEvents() != null) {
            Set<Event> eventsOfCompilation;
            if (compilationRequestDto.getEvents().isEmpty()) {
                eventsOfCompilation = Collections.emptySet();
            } else {
                eventsOfCompilation = eventRepository.findByIdIn(compilationRequestDto.getEvents());
            }
            compilationToUpdate.setEvents(eventsOfCompilation);
        }
        if (compilationRequestDto.getPinned() != null) {
            compilationToUpdate.setPinned(compilationRequestDto.getPinned());
        }
        compilationToUpdate.setTitle(compilationToUpdate.getTitle());

        return compilationMapper.compilationToDto(compilationRepository.save(compilationToUpdate));
    }
}
