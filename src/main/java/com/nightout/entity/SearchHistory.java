package com.nightout.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_history")
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userInput;

    @Column(nullable = false)
    private String extractedKeyword;

    @Column(nullable = false)
    private String budget;

    @Column(nullable = false)
    private int resultsCount;

    @Column(nullable = false)
    private LocalDateTime searchedAt;

    public SearchHistory() {}

    public SearchHistory(String userInput, String extractedKeyword, 
                        String budget, int resultsCount) {
        this.userInput = userInput;
        this.extractedKeyword = extractedKeyword;
        this.budget = budget;
        this.resultsCount = resultsCount;
        this.searchedAt = LocalDateTime.now();
    }

    public Long getId()                  { return id; }
    public String getUserInput()         { return userInput; }
    public String getExtractedKeyword()  { return extractedKeyword; }
    public String getBudget()            { return budget; }
    public int getResultsCount()         { return resultsCount; }
    public LocalDateTime getSearchedAt() { return searchedAt; }
}