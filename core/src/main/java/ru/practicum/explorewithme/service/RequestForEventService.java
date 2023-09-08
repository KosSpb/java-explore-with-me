package ru.practicum.explorewithme.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.dao.EventRepository;
import ru.practicum.explorewithme.dao.RequestForEventRepository;
import ru.practicum.explorewithme.dao.UserRepository;
import ru.practicum.explorewithme.dto.request.UpdateStatusOfRequestsForEventDto;
import ru.practicum.explorewithme.dto.response.AllTypeRequestsForEventsResponseDto;
import ru.practicum.explorewithme.dto.response.RequestForEventResponseDto;
import ru.practicum.explorewithme.enums.EventModerationState;
import ru.practicum.explorewithme.enums.RequestForEventStatus;
import ru.practicum.explorewithme.exception.ConditionsNotMetException;
import ru.practicum.explorewithme.exception.IncorrectRequestException;
import ru.practicum.explorewithme.exception.LimitReachedException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.RequestForEventMapper;
import ru.practicum.explorewithme.model.Event;
import ru.practicum.explorewithme.model.RequestForEvent;
import ru.practicum.explorewithme.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestForEventService {
    private final RequestForEventRepository requestForEventRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestForEventMapper requestForEventMapper;

    @Autowired
    public RequestForEventService(RequestForEventRepository requestForEventRepository,
                                  UserRepository userRepository,
                                  EventRepository eventRepository,
                                  RequestForEventMapper requestForEventMapper) {
        this.requestForEventRepository = requestForEventRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.requestForEventMapper = requestForEventMapper;
    }

    public Collection<RequestForEventResponseDto> getAllRequestsForEventByInitiator(long userId, long eventId) {
        User initiator = userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("get all requests for event of initiator: User with id=" +
                    userId + " was not found");
        });
        Event eventToRequestIn = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new NotFoundException("get all requests for event of initiator: Event with id=" +
                    eventId + " was not found");
        });

        List<RequestForEvent> allRequestsForEvent =
                requestForEventRepository.findByEventOrderByCreated(eventToRequestIn);

        return allRequestsForEvent.stream()
                .map(requestForEventMapper::requestForEventToDto)
                .collect(Collectors.toUnmodifiableList());
    }

    public AllTypeRequestsForEventsResponseDto updateStatusOfRequestsForEventByInitiator(
            UpdateStatusOfRequestsForEventDto requestForEventDto, long userId, long eventId) {

        User initiator = userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("update status of requests for event: User with id=" +
                    userId + " was not found");
        });
        Event eventToRequestIn = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new NotFoundException("update status of requests for event: Event with id=" +
                    eventId + " was not found");
        });

        if (!eventToRequestIn.getRequestModeration()) {
            throw new ConditionsNotMetException("update status of requests for event: " +
                    "Pre-moderation of requests is disabled. Confirmation is not required");
        }

        if (eventToRequestIn.getConfirmedRequests().equals(eventToRequestIn.getParticipantLimit())) {
            throw new LimitReachedException("update status of requests for event: " +
                    "The participant limit has been reached");
        }

        List<RequestForEvent> requestsForEventToUpdate =
                requestForEventRepository.findAllById(requestForEventDto.getRequestIds());

        List<RequestForEventResponseDto> confirmedRequests = new ArrayList<>();
        List<RequestForEventResponseDto> rejectedRequests = new ArrayList<>();

        for (RequestForEvent requestForEvent : requestsForEventToUpdate) {
            if (requestForEvent.getStatus() != RequestForEventStatus.PENDING) {
                throw new IncorrectRequestException("Request with id= " + requestForEvent.getId() +
                        " must have status PENDING");
            }

            if (eventToRequestIn.getConfirmedRequests().equals(eventToRequestIn.getParticipantLimit())) {
                requestForEvent.setStatus(RequestForEventStatus.CANCELED);
                rejectedRequests.add(requestForEventMapper.requestForEventToDto(requestForEvent));
            }
            if (requestForEventDto.getStatus() == RequestForEventStatus.REJECTED) {
                requestForEvent.setStatus(RequestForEventStatus.REJECTED);
                rejectedRequests.add(requestForEventMapper.requestForEventToDto(requestForEvent));

            } else {
                requestForEvent.setStatus(RequestForEventStatus.CONFIRMED);
                confirmedRequests.add(requestForEventMapper.requestForEventToDto(requestForEvent));

                eventToRequestIn.setConfirmedRequests(eventToRequestIn.getConfirmedRequests() + 1);
                eventRepository.save(eventToRequestIn);
            }
        }
        requestForEventRepository.saveAll(requestsForEventToUpdate);

        return AllTypeRequestsForEventsResponseDto.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

    public Collection<RequestForEventResponseDto> getAllRequestsOfUserForEventsByUserId(long userId) {
        User requester = userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("get all requests of user for events: User with id=" +
                    userId + " was not found");
        });

        List<RequestForEvent> allRequestsForEvents =
                requestForEventRepository.findByRequesterOrderByCreatedAsc(requester);

        return allRequestsForEvents.stream()
                .map(requestForEventMapper::requestForEventToDto)
                .collect(Collectors.toUnmodifiableList());
    }

    public RequestForEventResponseDto createRequestForEventByUser(long userId, long eventId) {
        User requester = userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("create request for event: User with id=" + userId + " was not found");
        });
        Event eventToRequestIn = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new NotFoundException("create request for event: Event with id=" + eventId + " was not found");
        });

        if (requester.getId().equals(eventToRequestIn.getInitiator().getId())) {
            throw new ConditionsNotMetException("create request for event: " +
                    "The initiator of the event cannot add a request to participate in his event");
        }

        if (eventToRequestIn.getState() != EventModerationState.PUBLISHED) {
            throw new ConditionsNotMetException("create request for event: " +
                    "It is impossible to participate in an unpublished event");
        }

        if (eventToRequestIn.getParticipantLimit() != 0
                && eventToRequestIn.getConfirmedRequests().equals(eventToRequestIn.getParticipantLimit())) {
            throw new LimitReachedException("create request for event: The participant limit has been reached");
        }

        RequestForEvent requestForEvent =
                requestForEventRepository.findByEventAndRequester(eventToRequestIn, requester);

        if (requestForEvent != null) {
            throw new ConditionsNotMetException("create request for event: It is forbidden to add a repeated request");
        } else {
            requestForEvent = RequestForEvent.builder()
                    .event(eventToRequestIn)
                    .requester(requester)
                    .status(RequestForEventStatus.PENDING)
                    .build();
        }

        if (!eventToRequestIn.getRequestModeration() || eventToRequestIn.getParticipantLimit() == 0) {
            requestForEvent.setStatus(RequestForEventStatus.CONFIRMED);
            eventToRequestIn.setConfirmedRequests(eventToRequestIn.getConfirmedRequests() + 1);
            eventRepository.save(eventToRequestIn);
            requestForEvent.setEvent(eventToRequestIn);
        }

        return requestForEventMapper.requestForEventToDto(requestForEventRepository.save(requestForEvent));
    }

    public RequestForEventResponseDto cancelRequestForEventByUser(long userId, long requestId) {
        User requester = userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("cancel request for event: User with id=" + userId + " was not found");
        });
        RequestForEvent requestForEvent = requestForEventRepository.findById(requestId).orElseThrow(() -> {
            throw new NotFoundException("cancel request for event: Request with id=" + requestId + " was not found");
        });

        if (requestForEvent.getStatus() == RequestForEventStatus.CONFIRMED) {
            Event eventOfCanceledRequest =
                    eventRepository.findById(requestForEvent.getEvent().getId()).orElseThrow(() -> {
                        throw new NotFoundException("cancel request for event: Event with id="
                                + requestForEvent.getEvent().getId() + " was not found");
                    });

            eventOfCanceledRequest.setConfirmedRequests(eventOfCanceledRequest.getConfirmedRequests() - 1);
            eventRepository.save(eventOfCanceledRequest);
        }

        requestForEvent.setStatus(RequestForEventStatus.CANCELED);
        return requestForEventMapper.requestForEventToDto(requestForEventRepository.save(requestForEvent));
    }
}
