package ru.practicum.explorewithme.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.model.Compilation;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("SELECT c " +
            "FROM Compilation AS c " +
            "WHERE (COALESCE (:pinned) IS NULL OR c.pinned = :pinned) " +
            "ORDER BY c.id ASC")
    Page<Compilation> findRequiredCompilations(@Param("pinned") Boolean pinned, Pageable pageable);

}
