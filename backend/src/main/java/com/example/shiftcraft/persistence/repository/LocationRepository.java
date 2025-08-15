package com.example.shiftcraft.persistence.repository;

import com.example.shiftcraft.persistence.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, String> {
    
    Optional<Location> findByName(String name);
    
    List<Location> findByCity(String city);
    
    List<Location> findByTimezone(String timezone);
}