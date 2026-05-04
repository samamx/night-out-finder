package com.nightout.repository;

import com.nightout.entity.SavedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedEventRepository extends JpaRepository<SavedEvent, Long> {

    // Returns all saved events sorted by most recently saved
    List<SavedEvent> findAllByOrderBySavedAtDesc();

    // Checks if an event with this URL is already saved and prevents duplicates
    boolean existsByUrl(String url);
}