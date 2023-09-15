package ru.practicum.explorewithme.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.dao.LocationRepository;
import ru.practicum.explorewithme.dto.request.LocationRequestDto;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.LocationMapper;
import ru.practicum.explorewithme.model.Location;

@Service
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Autowired
    public LocationService(LocationRepository locationRepository, LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
    }

    public Location createLocation(LocationRequestDto locationRequestDto) {

        return locationRepository.save(locationMapper.dtoToLocation(locationRequestDto));
    }

    public Location updateLocation(LocationRequestDto locationRequestDto, Long locId) {

        Location locationToUpdate = locationRepository.findById(locId).orElseThrow(() -> {
            throw new NotFoundException("update of location: Location with id=" + locId + " was not found");
        });

        if (locationRequestDto.getLat() != null) {
            locationToUpdate.setLat(locationRequestDto.getLat());
        }
        if (locationRequestDto.getLon() != null) {
            locationToUpdate.setLon(locationRequestDto.getLon());
        }
        return locationRepository.save(locationToUpdate);
    }
}
