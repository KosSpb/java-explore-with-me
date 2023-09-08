package ru.practicum.explorewithme.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u " +
            "FROM User AS u " +
            "WHERE (COALESCE (:userIds) IS NULL OR u.id IN :userIds) " +
            "ORDER BY u.id ASC")
    Page<User> findRequiredUsers(@Param("userIds") List<Long> userIds, Pageable pageable);

}
