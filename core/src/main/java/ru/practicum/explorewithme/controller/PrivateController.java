package ru.practicum.explorewithme.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.request.CommentRequestDto;
import ru.practicum.explorewithme.dto.request.EventRequestDto;
import ru.practicum.explorewithme.dto.request.UpdateStatusOfRequestsForEventDto;
import ru.practicum.explorewithme.dto.response.*;
import ru.practicum.explorewithme.service.CommentService;
import ru.practicum.explorewithme.service.EventService;
import ru.practicum.explorewithme.service.RequestForEventService;
import ru.practicum.explorewithme.validation.OnCreate;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@RestController
@Slf4j
@RequestMapping("/users/{userId}")
@Validated
public class PrivateController {
    private final EventService eventService;
    private final RequestForEventService requestForEventService;
    private final CommentService commentService;

    @Autowired
    public PrivateController(EventService eventService,
                             RequestForEventService requestForEventService,
                             CommentService commentService) {
        this.eventService = eventService;
        this.requestForEventService = requestForEventService;
        this.commentService = commentService;
    }

    @GetMapping("/events")
    public Collection<EventResponseDto> getAllEventsCreatedByUser(
            @PathVariable(value = "userId") @Positive long userId,
            @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(value = "size", defaultValue = "10") @Positive int size) {

        return eventService.getAllEventsCreatedByUser(userId, from, size);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(OnCreate.class)
    public EventFullInfoResponseDto createEventByUser(@RequestBody @Valid EventRequestDto eventRequestDto,
                                                      @PathVariable(value = "userId") @Positive long userId) {

        EventRequestDto.checkEventDate(eventRequestDto.getEventDate());
        EventFullInfoResponseDto createdEvent = eventService.createEventByUser(eventRequestDto, userId);
        log.info("createEventByUser - request for event [\"{}\"] creation by user with id {} was processed.",
                eventRequestDto, userId);
        return createdEvent;
    }

    @GetMapping("/events/{eventId}")
    public EventFullInfoResponseDto getFullInfoEventCreatedByUserById(
            @PathVariable(value = "userId") @Positive long userId,
            @PathVariable(value = "eventId") @Positive long eventId) {

        return eventService.getEventCreatedByUserById(userId, eventId);
    }

    @PatchMapping("/events/{eventId}")
    public EventFullInfoResponseDto updateEventByUser(@RequestBody @Valid EventRequestDto eventRequestDto,
                                                      @PathVariable(value = "userId") @Positive long userId,
                                                      @PathVariable(value = "eventId") @Positive long eventId) {

        EventRequestDto.checkEventDate(eventRequestDto.getEventDate());
        EventFullInfoResponseDto updatedEvent = eventService.updateEventByUser(eventRequestDto, userId, eventId);
        log.info("updateEventByUser - request for update of event with id {} to \"{}\" by user with id {} was processed.",
                eventId, eventRequestDto, userId);
        return updatedEvent;
    }

    @GetMapping("/events/{eventId}/requests")
    public Collection<RequestForEventResponseDto> getAllRequestsForEventByInitiator(
            @PathVariable(value = "userId") @Positive long userId,
            @PathVariable(value = "eventId") @Positive long eventId) {

        return requestForEventService.getAllRequestsForEventByInitiator(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public AllTypeRequestsForEventsResponseDto updateStatusOfRequestsForEventByInitiator(
            @RequestBody @Valid UpdateStatusOfRequestsForEventDto requestForEventDto,
            @PathVariable(value = "userId") @Positive long userId,
            @PathVariable(value = "eventId") @Positive long eventId) {

        AllTypeRequestsForEventsResponseDto updatedRequestsForEvent =
                requestForEventService.updateStatusOfRequestsForEventByInitiator(requestForEventDto, userId, eventId);
        log.info("updateStatusOfRequestsForEventByUser - request for update requests with ids {} for event " +
                "with id {} by user with id {} was processed.", requestForEventDto.getRequestIds(), eventId, userId);
        return updatedRequestsForEvent;
    }

    @GetMapping("/requests")
    public Collection<RequestForEventResponseDto> getAllRequestsOfUserForEventsByUserId(
            @PathVariable(value = "userId") @Positive long userId) {

        return requestForEventService.getAllRequestsOfUserForEventsByUserId(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestForEventResponseDto createRequestForEventByUser(
            @PathVariable(value = "userId") @Positive long userId,
            @RequestParam(value = "eventId") @Positive long eventId) {

        RequestForEventResponseDto createdRequestForEvent =
                requestForEventService.createRequestForEventByUser(userId, eventId);
        log.info("createRequestForEventByUser - request for creation of request for event with id {} " +
                "by user with id {} was processed.", eventId, userId);
        return createdRequestForEvent;
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public RequestForEventResponseDto cancelRequestForEventByUser(
            @PathVariable(value = "userId") @Positive long userId,
            @PathVariable(value = "requestId") @Positive long requestId) {

        RequestForEventResponseDto cancelledRequestForEvent =
                requestForEventService.cancelRequestForEventByUser(userId, requestId);
        log.info("cancelRequestForEventByUser - request for cancellation of request with id {} for event " +
                "by user with id {} was processed.", requestId, userId);
        return cancelledRequestForEvent;
    }

    @PostMapping("/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentFullInfoResponseDto createCommentByUser(
            @RequestBody @Valid CommentRequestDto commentRequestDto,
            @PathVariable(value = "userId") @Positive long userId,
            @RequestParam(value = "eventId") @Positive long eventId) {

        CommentFullInfoResponseDto createdComment = commentService.createCommentByUser(commentRequestDto, userId, eventId);
        log.info("createCommentByUser - request for comment [\"{}\"] creation for event with id {} " +
                "by user with id {} was processed.", commentRequestDto, eventId, userId);
        return createdComment;
    }

    @PatchMapping("/comments/{commentId}")
    public CommentFullInfoResponseDto updateCommentByUser(
            @RequestBody @Valid CommentRequestDto commentRequestDto,
            @PathVariable(value = "userId") @Positive long userId,
            @PathVariable(value = "commentId") @Positive long commentId) {

        CommentFullInfoResponseDto updatedComment = commentService.updateCommentByUser(commentRequestDto, userId, commentId);
        log.info("updateCommentByUser - request for update of comment with id {} to [\"{}\"] " +
                "by user with id {} was processed.", commentId, commentRequestDto, userId);
        return updatedComment;
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByUser(
            @PathVariable(value = "userId") @Positive long userId,
            @PathVariable(value = "commentId") @Positive long commentId) {

        commentService.deleteCommentByUser(userId, commentId);
        log.info("deleteCommentByUser - request for deletion of comment with id {} " +
                "by user with id {} was processed.", commentId, userId);
    }
}
