package com.nightout.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_events")
public class SavedEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String date;

    private String time;

    @Column(nullable = false)
    private String venueName;

    private String venueAddress;

    @Column(nullable = false)
    private String url;

    private String genre;

    private String priceRange;

    private String source;

    @Column(nullable = false)
    private LocalDateTime savedAt;

    public SavedEvent() {}

public SavedEvent(String name, String date, String time,
                  String venueName, String venueAddress,
                  String url, String genre, String priceRange, String source) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.venueName = venueName;
        this.venueAddress = venueAddress;
        this.url = url;
        this.genre = genre;
        this.priceRange = priceRange;
        this.source = source;
        this.savedAt = LocalDateTime.now();
    }

    public Long getId()            { return id; }
    public String getName()        { return name; }
    public String getDate()        { return date; }
    public String getTime()        { return time; }
    public String getVenueName()   { return venueName; }
    public String getVenueAddress(){ return venueAddress; }
    public String getUrl()         { return url; }
    public String getGenre()       { return genre; }
    public String getPriceRange()  { return priceRange; }
    public String getSource()      { return source; }
    public LocalDateTime getSavedAt() { return savedAt; }

}