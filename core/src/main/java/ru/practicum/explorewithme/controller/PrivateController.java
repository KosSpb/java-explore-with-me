package ru.practicum.explorewithme.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.request.EventRequestDto;
import ru.practicum.explorewithme.dto.request.UpdateStatusOfRequestsForEventDto;
import ru.practicum.explorewithme.dto.response.AllTypeRequestsForEventsResponseDto;
import ru.practicum.explorewithme.dto.response.EventFullInfoResponseDto;
import ru.practicum.explorewithme.dto.response.EventResponseDto;
import ru.practicum.explorewithme.dto.response.RequestForEventResponseDto;
import ru.practicum.explorewithme.service.EventService;
import ru.practicum.explorewithme.service.RequestForEventService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@Slf4j
@RequestMapping("/users/{userId}")
public class PrivateController {
    private final EventService eventService;
    private final RequestForEventService requestForEventService;

    @Autowired
    public PrivateController(EventService eventService, RequestForEventService requestForEventService) {
        this.eventService = eventService;
        this.requestForEventService = requestForEventService;
    }

    @GetMapping("/events")
    public Collection<EventResponseDto> getAllEventsCreatedByUser(
            @PathVariable(value = "userId") long userId,
            @RequestParam(value = "from", defaultValue = "0") int from,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        return eventService.getAllEventsCreatedByUser(userId, from, size);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullInfoResponseDto createEventByUser(@RequestBody @Valid EventRequestDto eventRequestDto,
                                                      @PathVariable(value = "userId") long userId) {

        EventRequestDto.checkEventDate(eventRequestDto.getEventDate());
        EventFullInfoResponseDto createdEvent = eventService.createEventByUser(eventRequestDto, userId);
        log.info("createEventByUser - request for event [\"{}\"] creation by user with id {} was processed.",
                eventRequestDto, userId);
        return createdEvent;
    }

    @GetMapping("/events/{eventId}")
    public EventFullInfoResponseDto getFullInfoEventCreatedByUserById(@PathVariable(value = "userId") long userId,
                                                                      @PathVariable(value = "eventId") long eventId) {

        return eventService.getEventCreatedByUserById(userId, eventId);
    }

    @PatchMapping("/events/{eventId}")
    public EventFullInfoResponseDto updateEventByUser(@RequestBody @Valid EventRequestDto eventRequestDto,
                                                      @PathVariable(value = "userId") long userId,
                                                      @PathVariable(value = "eventId") long eventId) {

        EventRequestDto.checkEventDate(eventRequestDto.getEventDate());
        EventFullInfoResponseDto updatedEvent = eventService.updateEventByUser(eventRequestDto, userId, eventId);
        log.info("updateEventByUser - request for update of event with id {} to \"{}\" by user with id {} was processed.",
                eventId, eventRequestDto, userId);
        return updatedEvent;
    }

    @GetMapping("/events/{eventId}/requests")
    public Collection<RequestForEventResponseDto> getAllRequestsForEventByInitiator(
            @PathVariable(value = "userId") long userId,
            @PathVariable(value = "eventId") long eventId) {

        return requestForEventService.getAllRequestsForEventByInitiator(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public AllTypeRequestsForEventsResponseDto updateStatusOfRequestsForEventByInitiator(
            @RequestBody @Valid UpdateStatusOfRequestsForEventDto requestForEventDto,
            @PathVariable(value = "userId") long userId,
            @PathVariable(value = "eventId") long eventId) {

        AllTypeRequestsForEventsResponseDto updatedRequestsForEvent =
                requestForEventService.updateStatusOfRequestsForEventByInitiator(requestForEventDto, userId, eventId);
        log.info("updateStatusOfRequestsForEventByUser - request for update requests with ids {} for event " +
                "with id {} by user with id {} was processed.", requestForEventDto.getRequestIds(), eventId, userId);
        return updatedRequestsForEvent;
    }

    @GetMapping("/requests")
    public Collection<RequestForEventResponseDto> getAllRequestsOfUserForEventsByUserId(
            @PathVariable(value = "userId") long userId) {

        return requestForEventService.getAllRequestsOfUserForEventsByUserId(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestForEventResponseDto createRequestForEventByUser(@PathVariable(value = "userId") long userId,
                                                                  @RequestParam(value = "eventId") long eventId) {

        RequestForEventResponseDto createdRequestForEvent =
                requestForEventService.createRequestForEventByUser(userId, eventId);
        log.info("createRequestForEventByUser - request for creation of request for event with id {} " +
                "by user with id {} was processed.", eventId, userId);
        return createdRequestForEvent;
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public RequestForEventResponseDto cancelRequestForEventByUser(@PathVariable(value = "userId") long userId,
                                                                  @PathVariable(value = "requestId") long requestId) {

        RequestForEventResponseDto cancelledRequestForEvent =
                requestForEventService.cancelRequestForEventByUser(userId, requestId);
        log.info("cancelRequestForEventByUser - request for cancellation of request with id {} for event " +
                "by user with id {} was processed.", requestId, userId);
        return cancelledRequestForEvent;
    }
}
