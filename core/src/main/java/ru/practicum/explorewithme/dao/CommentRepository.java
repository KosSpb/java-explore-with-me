package ru.practicum.explorewithme.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.model.Comment;
import ru.practicum.explorewithme.model.Event;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c " +
            "FROM Comment AS c " +
            "WHERE (COALESCE (:searchText) IS NULL OR (upper(c.text) like upper(concat('%', :searchText, '%')))) " +
            "AND (COALESCE (:rangeStart) IS NULL OR c.created > :rangeStart) " +
            "AND (COALESCE (:rangeEnd) IS NULL OR c.created < :rangeEnd) " +
            "AND (COALESCE (:userIds) IS NULL OR c.author.id IN :userIds) " +
            "AND (COALESCE (:eventIds) IS NULL OR c.event.id IN :eventIds) " +
            "AND (COALESCE (:onlyEdited) = false OR c.isEdited = :onlyEdited) " +
            "ORDER BY c.id ASC")
    Page<Comment> findAllCommentsByParametersForAdmin(@Param("searchText") String searchText,
                                                      @Param("userIds") List<Long> userIds,
                                                      @Param("eventIds") List<Long> eventIds,
                                                      @Param("onlyEdited") Boolean onlyEdited,
                                                      @Param("rangeStart") LocalDateTime rangeStart,
                                                      @Param("rangeEnd") LocalDateTime rangeEnd,
                                                      Pageable pageable);

    List<Comment> findByEventInOrderByCreatedAsc(Collection<Event> eventsCreatedByUser);

    List<Comment> findByEventOrderByCreatedAsc(Event requiredEvent);

}
