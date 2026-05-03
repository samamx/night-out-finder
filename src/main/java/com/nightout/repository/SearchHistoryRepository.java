package com.nightout.repository;

import com.nightout.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    // Spring generates the SQL for this automatically based on the method name
    List<SearchHistory> findAllByOrderBySearchedAtDesc();
}