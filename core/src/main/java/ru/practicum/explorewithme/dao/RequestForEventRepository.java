package ru.practicum.explorewithme.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.model.Event;
import ru.practicum.explorewithme.model.RequestForEvent;
import ru.practicum.explorewithme.model.User;

import java.util.List;

public interface RequestForEventRepository extends JpaRepository<RequestForEvent, Long> {

    RequestForEvent findByEventAndRequester(Event eventToRequest, User requester);

    List<RequestForEvent> findByRequesterOrderByCreatedAsc(User requester);

    List<RequestForEvent> findByEventOrderByCreated(Event eventToRequestIn);

}
