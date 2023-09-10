package ru.practicum.explorewithme.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.model.ConfirmedRequestsQuantity;
import ru.practicum.explorewithme.model.Event;
import ru.practicum.explorewithme.model.RequestForEvent;
import ru.practicum.explorewithme.model.User;

import java.util.Collection;
import java.util.List;

public interface RequestForEventRepository extends JpaRepository<RequestForEvent, Long> {

    RequestForEvent findByEventAndRequester(Event eventToRequest, User requester);

    List<RequestForEvent> findByRequesterOrderByCreatedAsc(User requester);

    List<RequestForEvent> findByEventOrderByCreated(Event eventToRequestIn);

    @Query("SELECT new ru.practicum.explorewithme.model.ConfirmedRequestsQuantity(r.event, COUNT(r.id)) " +
            "FROM RequestForEvent r " +
            "WHERE r.event IN :events " +
            "AND r.status = 'CONFIRMED' " +
            "GROUP BY r.event " +
            "ORDER BY r.event ASC")
    List<ConfirmedRequestsQuantity> countConfirmedRequestsByEvents(@Param("events") Collection<Event> events);

    @Query("SELECT new ru.practicum.explorewithme.model.ConfirmedRequestsQuantity(r.event, COUNT(r.id)) " +
            "FROM RequestForEvent r " +
            "WHERE r.event = :event " +
            "AND r.status = 'CONFIRMED' " +
            "GROUP BY r.event")
    ConfirmedRequestsQuantity countConfirmedRequestsBySingleEvent(@Param("event") Event event);

}
