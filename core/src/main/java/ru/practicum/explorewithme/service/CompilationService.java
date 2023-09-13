package ru.practicum.explorewithme.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.dao.CompilationRepository;
import ru.practicum.explorewithme.dao.EventRepository;
import ru.practicum.explorewithme.dao.RequestForEventRepository;
import ru.practicum.explorewithme.dto.request.CompilationRequestDto;
import ru.practicum.explorewithme.dto.response.CompilationResponseDto;
import ru.practicum.explorewithme.dto.response.EventResponseDto;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.CompilationMapper;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.model.Compilation;
import ru.practicum.explorewithme.model.ConfirmedRequestsQuantity;
import ru.practicum.explorewithme.model.Event;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;
    private final RequestForEventRepository requestForEventRepository;

    @Autowired
    public CompilationService(CompilationRepository compilationRepository,
                              EventRepository eventRepository,
                              CompilationMapper compilationMapper,
                              EventMapper eventMapper,
                              RequestForEventRepository requestForEventRepository) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
        this.compilationMapper = compilationMapper;
        this.eventMapper = eventMapper;
        this.requestForEventRepository = requestForEventRepository;
    }

    public Collection<CompilationResponseDto> getAllEventsCompilations(Boolean pinned, int from, int size) {

        Pageable pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        List<Compilation> requestedCompilations =
                compilationRepository.findRequiredCompilations(pinned, pageRequest).getContent();

        Set<Event> eventsOfCompilation = requestedCompilations.stream()
                .flatMap(compilation -> compilation.getEvents().stream())
                .collect(Collectors.toSet());

        Set<EventResponseDto> eventDtosOfCompilation =
                getEventResponseDtosWithConfirmedRequests(eventsOfCompilation);

        return requestedCompilations.stream()
                .map(compilation -> compilationMapper.compilationToDto(compilation,
                        eventDtosOfCompilation.stream()
                                .filter(eventResponseDto -> compilation.getEvents().stream()
                                        .anyMatch(event -> eventResponseDto.getId().equals(event.getId())))
                                .collect(Collectors.toSet())))
                .collect(Collectors.toUnmodifiableList());
    }

    public CompilationResponseDto getEventsCompilationById(Long compId) {

        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> {
            throw new NotFoundException("get compilation by id: Compilation with id=" + compId + " was not found");
        });

        Set<EventResponseDto> eventDtosOfCompilation =
                getEventResponseDtosWithConfirmedRequests(compilation.getEvents());

        return compilationMapper.compilationToDto(compilation, eventDtosOfCompilation);
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

        Set<EventResponseDto> eventDtosOfCompilation =
                getEventResponseDtosWithConfirmedRequests(eventsOfCompilation);

        return compilationMapper.compilationToDto(
                compilationRepository.save(compilationMapper.dtoToCompilation(
                        compilationRequestDto, eventsOfCompilation)), eventDtosOfCompilation);
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

        Set<EventResponseDto> eventDtosOfCompilation =
                getEventResponseDtosWithConfirmedRequests(compilationToUpdate.getEvents());

        return compilationMapper.compilationToDto(
                compilationRepository.save(compilationToUpdate), eventDtosOfCompilation);
    }

    private Set<EventResponseDto> getEventResponseDtosWithConfirmedRequests(Set<Event> eventsOfCompilation) {

        Map<Long, Long> confirmedRequestsQuantityByEventIds;
        Set<EventResponseDto> eventDtosOfCompilation;

        if (!eventsOfCompilation.isEmpty()) {
            confirmedRequestsQuantityByEventIds =
                    requestForEventRepository.countConfirmedRequestsByEvents(eventsOfCompilation).stream()
                            .collect(Collectors.toMap(
                                    confirmedRequestsQuantity -> confirmedRequestsQuantity.getEvent().getId(),
                                    ConfirmedRequestsQuantity::getConfirmedRequests));

            eventDtosOfCompilation = eventsOfCompilation.stream()
                    .map(eventMapper::eventToShortDto)
                    .peek(eventResponseDto -> eventResponseDto.setConfirmedRequests(
                            confirmedRequestsQuantityByEventIds.get(eventResponseDto.getId())))
                    .collect(Collectors.toSet());
        } else {
            eventDtosOfCompilation = Collections.emptySet();
        }
        return eventDtosOfCompilation;
    }
}
