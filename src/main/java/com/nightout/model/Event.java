package com.nightout.model;

public class Event {
    private String name; 
    private String date; 
    private String time;
    private String venueName;
    private String venueAddress;
    private String url;
    private String genre;
    private String priceRange;
    private String source;

    public Event(String name, String date, String time, String venueName, String venueAddress, String url, String genre, String priceRange, String source) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.venueName = venueName;
        this.venueAddress = venueAddress;
        this.url = url;
        this.genre = genre;
        this.priceRange = priceRange;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getVenueName() {
        return venueName;
    }

    public String getVenueAddress() {
        return venueAddress;
    }

    public String getUrl() {
        return url;
    }

    public String getGenre() {
        return genre;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return String.format("""
            Event: %s
              Date:   %s at %s
              Venue:  %s - %s
              Genre:  %s
              Price:  %s
              Source: %s
              Link:   %s""",
            name, date, time, venueName, venueAddress, genre, priceRange, source, url);
    }
   
}