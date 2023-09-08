package ru.practicum.explorewithme.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.enums.EventModerationState;
import ru.practicum.explorewithme.model.Category;
import ru.practicum.explorewithme.model.Event;
import ru.practicum.explorewithme.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCategory(Category category);

    Set<Event> findByIdIn(Collection<Long> eventIds);

    Page<Event> findByInitiatorOrderByCreatedOnAsc(User initiator, Pageable pageable);

    Event findByInitiatorAndId(User initiator, Long id);

    @Query("SELECT e " +
            "FROM Event AS e " +
            "WHERE (COALESCE (:rangeStart) IS NULL OR e.eventDate > :rangeStart) " +
            "AND (COALESCE (:rangeEnd) IS NULL OR e.eventDate < :rangeEnd) " +
            "AND (COALESCE (:userIds) IS NULL OR e.initiator.id IN :userIds) " +
            "AND (COALESCE (:states) IS NULL OR e.state IN :states) " +
            "AND (COALESCE (:categoryIds) IS NULL OR e.category.id IN :categoryIds) " +
            "ORDER BY e.createdOn ASC")
    Page<Event> findAllEventsByParametersForAdmin(@Param("userIds") List<Long> userIds,
                                                  @Param("states") Set<EventModerationState> states,
                                                  @Param("categoryIds") List<Long> categoryIds,
                                                  @Param("rangeStart") LocalDateTime rangeStart,
                                                  @Param("rangeEnd") LocalDateTime rangeEnd,
                                                  Pageable pageable);

    @Query("SELECT e " +
            "FROM Event AS e " +
            "WHERE (COALESCE (:searchText) IS NULL OR (upper(e.annotation) like upper(concat('%', :searchText, '%')) " +
            "OR upper(e.description) like upper(concat('%', :searchText, '%')))) " +
            "AND (COALESCE (:categoryIds) IS NULL OR e.category.id IN :categoryIds) " +
            "AND (COALESCE (:paid) IS NULL OR e.paid = :paid) " +
            "AND (COALESCE (:onlyAvailable) = false OR (e.confirmedRequests < e.participantLimit)) " +
            "AND ((COALESCE (:rangeStart) IS NOT NULL AND COALESCE (:rangeEnd) IS NOT NULL " +
            "AND e.eventDate BETWEEN :rangeStart AND :rangeEnd) " +
            "OR ((COALESCE (:rangeStart) IS NULL OR COALESCE (:rangeEnd) IS NULL) AND e.eventDate > CURRENT_TIMESTAMP)) " +
            "AND e.state = 'PUBLISHED' " +
            "ORDER BY e.eventDate ASC")
    Page<Event> findAllPublishedEventsByParameters(@Param("searchText") String searchText,
                                                   @Param("categoryIds") List<Long> categoryIds,
                                                   @Param("paid") Boolean paid,
                                                   @Param("onlyAvailable") Boolean onlyAvailable,
                                                   @Param("rangeStart") LocalDateTime rangeStart,
                                                   @Param("rangeEnd") LocalDateTime rangeEnd,
                                                   Pageable pageable);

    Event findByIdAndState(Long eventId, EventModerationState state);

}
