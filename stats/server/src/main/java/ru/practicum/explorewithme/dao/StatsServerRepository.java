package ru.practicum.explorewithme.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.model.HitCount;
import ru.practicum.explorewithme.model.StatUnit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsServerRepository extends JpaRepository<StatUnit, Long> {
    @Query("SELECT s.app AS singleApp, s.uri AS singleUri, " +
            "CASE WHEN :unique = false THEN COUNT(s.uri) " +
            "ELSE COUNT(DISTINCT CONCAT(s.uri, s.ip)) " +
            "END AS totalHits " +
            "FROM StatUnit AS s " +
            "WHERE s.timestamp BETWEEN :start AND :end " +
            "AND (COALESCE (:uris) IS NULL OR s.uri IN :uris) " +
            "GROUP BY singleUri, singleApp " +
            "ORDER BY totalHits DESC")
    List<HitCount> countHitsForListedUrisInTimeRangeConsideringIpUniqueness(@Param("start") LocalDateTime start,
                                                                            @Param("end") LocalDateTime end,
                                                                            @Param("uris") List<String> uris,
                                                                            @Param("unique") boolean unique);
}
