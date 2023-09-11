package ru.practicum.explorewithme.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.model.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
