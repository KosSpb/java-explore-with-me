package ru.practicum.explorewithme.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.StatsRequestDto;
import ru.practicum.explorewithme.StatsResponseDto;
import ru.practicum.explorewithme.client.StatsClient;
import ru.practicum.explorewithme.dao.CategoryRepository;
import ru.practicum.explorewithme.dao.EventRepository;
import ru.practicum.explorewithme.dao.RequestForEventRepository;
import ru.practicum.explorewithme.dao.UserRepository;
import ru.practicum.explorewithme.dto.request.EventRequestDto;
import ru.practicum.explorewithme.dto.response.EventFullInfoResponseDto;
import ru.practicum.explorewithme.dto.response.EventResponseDto;
import ru.practicum.explorewithme.enums.EventModerationAction;
import ru.practicum.explorewithme.enums.EventModerationState;
import ru.practicum.explorewithme.enums.EventsSortType;
import ru.practicum.explorewithme.exception.ConditionsNotMetException;
import ru.practicum.explorewithme.exception.IncorrectRequestException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {
    private final StatsClient statsClient;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final LocationService locationService;
    private final EventMapper eventMapper;
    private final RequestForEventRepository requestForEventRepository;
    private final DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public EventService(StatsClient statsClient,
                        EventRepository eventRepository,
                        CategoryRepository categoryRepository,
                        UserRepository userRepository,
                        LocationService locationService,
                        EventMapper eventMapper,
                        RequestForEventRepository requestForEventRepository) {
        this.statsClient = statsClient;
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.locationService = locationService;
        this.eventMapper = eventMapper;
        this.requestForEventRepository = requestForEventRepository;
    }

    public Collection<EventResponseDto> getAllEventsCreatedByUser(long userId, int from, int size) {
        User eventsCreator = userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("get all events created by user: User with id=" + userId + " was not found");
        });

        Pageable pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        List<Event> eventsCreatedByUser =
                eventRepository.findByInitiatorOrderByCreatedOnAsc(eventsCreator, pageRequest).getContent();

        if (eventsCreatedByUser.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<EventViews> eventsViews = getViewsOfAllEvents(eventsCreatedByUser);

            List<ConfirmedRequestsQuantity> confirmedRequestsQuantityByEvents =
                    requestForEventRepository.countConfirmedRequestsByEvents(eventsCreatedByUser);

            return eventsCreatedByUser.stream()
                    .map(eventMapper::eventToShortDto)
                    .peek(eventResponseDto -> setViewsAndConfirmedRequests(
                            eventsViews, confirmedRequestsQuantityByEvents, eventResponseDto))
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    public EventFullInfoResponseDto createEventByUser(EventRequestDto eventRequestDto, long userId) {
        User initiatorOfEvent = userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("creation of event: User with id=" + userId + " was not found");
        });

        Category categoryOfEvent = categoryRepository.findById(eventRequestDto.getCategory()).orElseThrow(() -> {
            throw new NotFoundException("creation of event: Category with id=" + eventRequestDto.getCategory() +
                    " was not found");
        });

        if (eventRequestDto.getPaid() == null) {
            eventRequestDto.setPaid(false);
        }
        if (eventRequestDto.getParticipantLimit() == null) {
            eventRequestDto.setParticipantLimit(0L);
        }
        if (eventRequestDto.getRequestModeration() == null) {
            eventRequestDto.setRequestModeration(true);
        }

        Location createdLocation = locationService.createLocation(eventRequestDto.getLocation());

        Event eventToCreate =
                eventMapper.dtoToEvent(eventRequestDto, categoryOfEvent, initiatorOfEvent, createdLocation);
        eventToCreate.setState(EventModerationState.PENDING);

        EventFullInfoResponseDto createdEventResponseDto =
                eventMapper.eventToFullDto(eventRepository.save(eventToCreate));
        createdEventResponseDto.setViews(0L);
        createdEventResponseDto.setConfirmedRequests(0L);

        return createdEventResponseDto;
    }

    public EventFullInfoResponseDto getEventCreatedByUserById(long userId, long eventId) {
        User eventCreator = userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("get full info of event created by user: " +
                    "User with id=" + userId + " was not found");
        });

        Event requiredEvent = eventRepository.findByInitiatorAndId(eventCreator, eventId);

        if (requiredEvent == null) {
            throw new NotFoundException("get full info of event created by user: " +
                    "Event with id=" + eventId + " was not found");
        }

        EventFullInfoResponseDto eventFullInfoResponseDto = getEventFullInfoResponseDtoWithViews(requiredEvent);

        ConfirmedRequestsQuantity confirmedRequestsQuantity =
                requestForEventRepository.countConfirmedRequestsBySingleEvent(requiredEvent);

        eventFullInfoResponseDto.setConfirmedRequests(
                confirmedRequestsQuantity == null ? 0L : confirmedRequestsQuantity.getConfirmedRequests());

        return eventFullInfoResponseDto;
    }

    public EventFullInfoResponseDto updateEventByUser(EventRequestDto eventRequestDto, long userId, long eventId) {
        User initiatorOfEvent = userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("update of event by user: User with id=" + userId + " was not found");
        });
        Event eventToUpdate = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new NotFoundException("update of event by user: Event with id=" + eventId + " was not found");
        });

        if (eventToUpdate.getState() == EventModerationState.PUBLISHED) {
            throw new ConditionsNotMetException("update of event by user: " +
                    "Only pending or canceled events can be changed");
        }

        if (eventRequestDto.getStateAction() == EventModerationAction.CANCEL_REVIEW) {
            eventToUpdate.setState(EventModerationState.CANCELED);
            return eventMapper.eventToFullDto(eventRepository.save(eventToUpdate));
        }

        updateEventFields(eventRequestDto, eventToUpdate);
        eventToUpdate.setState(EventModerationState.PENDING);

        EventFullInfoResponseDto updatedEventResponseDto =
                eventMapper.eventToFullDto(eventRepository.save(eventToUpdate));
        updatedEventResponseDto.setViews(0L);
        updatedEventResponseDto.setConfirmedRequests(0L);

        return updatedEventResponseDto;
    }

    public Collection<EventFullInfoResponseDto> getFullInfoAboutAllEventsByAdmin(List<Long> userIds,
                                                                                 List<String> statesOfEvent,
                                                                                 List<Long> categoryIds,
                                                                                 LocalDateTime rangeStart,
                                                                                 LocalDateTime rangeEnd,
                                                                                 int from, int size) {
        Set<EventModerationState> statesAsEnums = null;
        if (statesOfEvent != null) {
            statesAsEnums = statesOfEvent.stream()
                    .map(EventModerationState::valueOf)
                    .collect(Collectors.toSet());
        }

        Pageable pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        List<Event> requestedEvents =
                eventRepository.findAllEventsByParametersForAdmin(
                        userIds, statesAsEnums, categoryIds, rangeStart, rangeEnd, pageRequest).getContent();

        if (requestedEvents.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<EventViews> eventsViews = getViewsOfAllEvents(requestedEvents);

            List<ConfirmedRequestsQuantity> confirmedRequestsQuantityByEvents =
                    requestForEventRepository.countConfirmedRequestsByEvents(requestedEvents);

            return requestedEvents.stream()
                    .map(eventMapper::eventToFullDto)
                    .peek(eventResponseDto -> {
                        Long views = eventsViews.stream()
                                .filter(eventViews -> eventResponseDto.getId().equals(eventViews.getEventId()))
                                .findFirst()
                                .map(EventViews::getViews)
                                .orElse(0L);
                        eventResponseDto.setViews(views);

                        Long confirmedRequests = confirmedRequestsQuantityByEvents.stream()
                                .filter(quantityByEvent -> quantityByEvent.getEvent().getId()
                                        .equals(eventResponseDto.getId()))
                                .findFirst()
                                .map(ConfirmedRequestsQuantity::getConfirmedRequests)
                                .orElse(0L);
                        eventResponseDto.setConfirmedRequests(confirmedRequests);
                    })
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    public EventFullInfoResponseDto updateEventAndPublicationStatusEditByAdmin(EventRequestDto eventRequestDto,
                                                                               long eventId) {
        Event eventToUpdate = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new NotFoundException("update of event by admin: Event with id=" + eventId + " was not found");
        });

        if (eventRequestDto.getStateAction() == EventModerationAction.PUBLISH_EVENT) {
            if (eventToUpdate.getState() != EventModerationState.PENDING) {
                throw new ConditionsNotMetException("update of event by admin: Cannot publish the event " +
                        "because it's not in the right state: " + eventToUpdate.getState().toString());
            } else {
                eventToUpdate.setState(EventModerationState.PUBLISHED);
                eventToUpdate.setPublishedOn(LocalDateTime.now());
                updateEventFields(eventRequestDto, eventToUpdate);
            }
        } else {
            if (eventToUpdate.getState() != EventModerationState.PUBLISHED) {
                eventToUpdate.setState(EventModerationState.CANCELED);
            } else {
                throw new ConditionsNotMetException("update of event by admin: Cannot reject the event " +
                        "because it's already published");
            }
        }

        EventFullInfoResponseDto updatedEventResponseDto =
                eventMapper.eventToFullDto(eventRepository.save(eventToUpdate));
        updatedEventResponseDto.setViews(0L);
        updatedEventResponseDto.setConfirmedRequests(0L);

        return updatedEventResponseDto;
    }

    public Collection<EventResponseDto> getAllPublishedEvents(String searchText,
                                                              List<Long> categoryIds,
                                                              Boolean paid,
                                                              LocalDateTime rangeStart,
                                                              LocalDateTime rangeEnd,
                                                              Boolean onlyAvailable,
                                                              EventsSortType sortType,
                                                              int from, int size,
                                                              String requestURI,
                                                              String remoteIpAddress) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new IncorrectRequestException("get all published events: Start time cannot be after end time");
        }

        Pageable pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);

        List<Event> publishedEvents =
                eventRepository.findAllPublishedEventsByParameters(
                        searchText, categoryIds, paid, onlyAvailable, rangeStart, rangeEnd, pageRequest).getContent();

        if (publishedEvents.isEmpty()) {
            registerRequestToEndpoint(requestURI, remoteIpAddress);
            return Collections.emptyList();
        }

        List<EventViews> eventsViews = getViewsOfAllEvents(publishedEvents);
        List<ConfirmedRequestsQuantity> confirmedRequestsQuantityByEvents =
                requestForEventRepository.countConfirmedRequestsByEvents(publishedEvents);

        registerRequestToEndpoint(requestURI, remoteIpAddress);

        if (sortType == EventsSortType.EVENT_DATE) {
            return publishedEvents.stream()
                    .map(eventMapper::eventToShortDto)
                    .peek(eventResponseDto -> setViewsAndConfirmedRequests(
                            eventsViews, confirmedRequestsQuantityByEvents, eventResponseDto))
                    .collect(Collectors.toUnmodifiableList());
        } else {
            return publishedEvents.stream()
                    .map(eventMapper::eventToShortDto)
                    .peek(eventResponseDto -> setViewsAndConfirmedRequests(
                            eventsViews, confirmedRequestsQuantityByEvents, eventResponseDto))
                    .sorted(Comparator.comparing(EventResponseDto::getViews).reversed())
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    public EventFullInfoResponseDto getFullInfoAboutPublishedEventById(long eventId,
                                                                       String requestURI,
                                                                       String remoteIpAddress) {
        Event publishedEvent = eventRepository.findByIdAndState(eventId, EventModerationState.PUBLISHED);

        if (publishedEvent == null) {
            throw new NotFoundException("get full info of published event: " +
                    "Event with id=" + eventId + " was not found");
        }

        EventFullInfoResponseDto eventFullInfoResponseDto = getEventFullInfoResponseDtoWithViews(publishedEvent);
        ConfirmedRequestsQuantity confirmedRequestsQuantity =
                requestForEventRepository.countConfirmedRequestsBySingleEvent(publishedEvent);

        eventFullInfoResponseDto.setConfirmedRequests(
                confirmedRequestsQuantity == null ? 0L : confirmedRequestsQuantity.getConfirmedRequests());

        registerRequestToEndpoint(requestURI, remoteIpAddress);
        return eventFullInfoResponseDto;
    }

    private void setViewsAndConfirmedRequests(List<EventViews> eventsViews,
                                              List<ConfirmedRequestsQuantity> confirmedRequestsQuantityByEvents,
                                              EventResponseDto eventResponseDto) {
        Long views = eventsViews.stream()
                .filter(eventViews -> eventResponseDto.getId().equals(eventViews.getEventId()))
                .findFirst()
                .map(EventViews::getViews)
                .orElse(0L);
        eventResponseDto.setViews(views);

        Long confirmedRequests = confirmedRequestsQuantityByEvents.stream()
                .filter(quantityByEvent -> quantityByEvent.getEvent().getId().equals(eventResponseDto.getId()))
                .findFirst()
                .map(ConfirmedRequestsQuantity::getConfirmedRequests)
                .orElse(0L);
        eventResponseDto.setConfirmedRequests(confirmedRequests);
    }

    private void updateEventFields(EventRequestDto eventRequestDto, Event eventToUpdate) {
        if (eventRequestDto.getAnnotation() != null && !eventRequestDto.getAnnotation().isBlank()) {
            eventToUpdate.setAnnotation(eventRequestDto.getAnnotation());
        }

        if (eventRequestDto.getCategory() != null
                && !eventRequestDto.getCategory().equals(eventToUpdate.getCategory().getId())) {

            Category categoryForEventUpdate =
                    categoryRepository.findById(eventRequestDto.getCategory()).orElseThrow(() -> {
                        throw new NotFoundException("update of event: Category with id=" +
                                eventRequestDto.getCategory() + " was not found");
                    });

            eventToUpdate.setCategory(categoryForEventUpdate);
        }

        if (eventRequestDto.getDescription() != null && !eventRequestDto.getDescription().isBlank()) {
            eventToUpdate.setDescription(eventRequestDto.getDescription());
        }
        if (eventRequestDto.getEventDate() != null) {
            eventToUpdate.setEventDate(eventRequestDto.getEventDate());
        }

        if (eventRequestDto.getLocation() != null) {
            if (!eventRequestDto.getLocation().getLon().equals(eventToUpdate.getLocation().getLon())
                    || !eventRequestDto.getLocation().getLat().equals(eventToUpdate.getLocation().getLat())) {

                Location locationForEventUpdate = locationService
                        .updateLocation(eventRequestDto.getLocation(), eventToUpdate.getLocation().getId());

                eventToUpdate.setLocation(locationForEventUpdate);
            }
        }

        if (eventRequestDto.getPaid() != null) {
            eventToUpdate.setPaid(eventRequestDto.getPaid());
        }
        if (eventRequestDto.getParticipantLimit() != null) {
            eventToUpdate.setParticipantLimit(eventRequestDto.getParticipantLimit());
        }
        if (eventRequestDto.getRequestModeration() != null) {
            eventToUpdate.setRequestModeration(eventRequestDto.getRequestModeration());
        }
        if (eventRequestDto.getTitle() != null && !eventRequestDto.getTitle().isBlank()) {
            eventToUpdate.setTitle(eventRequestDto.getTitle());
        }
    }

    private void registerRequestToEndpoint(String requestURI, String remoteIpAddress) {
        statsClient.registerEndpointHit(StatsRequestDto.builder()
                .app("ewm-main-service")
                .uri(requestURI)
                .ip(remoteIpAddress)
                .timestamp(LocalDateTime.now())
                .build());
    }

    private List<EventViews> getViewsOfAllEvents(List<Event> events) {
        String startTime = LocalDateTime.now().minusYears(100).format(pattern);
        String endTime = LocalDateTime.now().plusYears(100).format(pattern);

        List<String> uris = events.stream()
                .map((Event event) -> "/events/" + event.getId())
                .collect(Collectors.toList());

        List<StatsResponseDto> statsOfViews = statsClient.getStats(startTime, endTime, uris, false);

        return statsOfViews.stream()
                .map(eventMapper::statsDtoToEventViews)
                .collect(Collectors.toList());
    }

    private EventFullInfoResponseDto getEventFullInfoResponseDtoWithViews(Event requiredEvent) {
        String startTime = LocalDateTime.now().minusYears(100).format(pattern);
        String endTime = LocalDateTime.now().plusYears(100).format(pattern);
        List<String> uris = Collections.singletonList("/events/" + requiredEvent.getId());

        List<StatsResponseDto> statsOfViews = statsClient.getStats(startTime, endTime, uris, true);

        EventFullInfoResponseDto eventFullInfoResponseDto = eventMapper.eventToFullDto(requiredEvent);

        eventFullInfoResponseDto.setViews(statsOfViews.stream()
                .map(eventMapper::statsDtoToEventViews)
                .findFirst()
                .map(EventViews::getViews)
                .orElse(0L));

        return eventFullInfoResponseDto;
    }
}
