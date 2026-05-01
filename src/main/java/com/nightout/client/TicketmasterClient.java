package com.nightout.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nightout.model.Event;
import com.nightout.model.PriceFilter;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TicketmasterClient implements EventSource{

    private static final String BASE_URL ="https://app.ticketmaster.com/discovery/v2/events.json";
    private static final String SOURCE_NAME = "Ticketmaster";
    private static final Logger logger = Logger.getLogger(TicketmasterClient.class.getName());

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TicketmasterClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }

    public List<Event> searchEvents(String keyword, PriceFilter priceFilter, int maxResults) throws Exception {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        StringBuilder url = new StringBuilder(String.format(
    "%s?apikey=%s&keyword=%s&city=London&countryCode=GB&size=%d&sort=date,asc",
    BASE_URL, apiKey, encodedKeyword, maxResults
    ));

    if (priceFilter.isFree()) {
        url.append("&priceMin=0&priceMax=0");
    } else if (priceFilter.isRange()) {
        url.append("&priceMin=").append((int)priceFilter.getMinPrice().doubleValue());
        url.append("&priceMax=").append((int)priceFilter.getMaxPrice().doubleValue());
    }

    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url.toString())).GET().build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
        if (logger.isLoggable(java.util.logging.Level.SEVERE)) {
            logger.severe(String.format("Ticketmaster error: HTTP %d", response.statusCode()));
        }
        return new ArrayList<>();
    }

    return parseEvents(response.body());
    }

    private List<Event> parseEvents(String json) throws Exception {
    List<Event> events = new ArrayList<>();
    JsonNode root = objectMapper.readTree(json);
    JsonNode embedded = root.path("_embedded");
    if (embedded.isMissingNode()) return events;

    for (JsonNode node : embedded.path("events")) {
        String name = node.path("name").asText("Unknown Event");
        String url  = node.path("url").asText("#");

        JsonNode dates = node.path("dates").path("start");
        String date = dates.path("localDate").asText("TBC");
        String time = dates.path("localTime").asText("TBC");

        String genre = "N/A";
        JsonNode classifications = node.path("classifications");
        if (classifications.isArray() && classifications.size() > 0)
            genre = classifications.get(0).path("genre").path("name").asText("N/A");

        String venueName = "Unknown Venue";
        String venueAddress = "Unknown Address";
        JsonNode venues = node.path("_embedded").path("venues");
        if (venues.isArray() && venues.size() > 0) {
            venueName    = venues.get(0).path("name").asText("Unknown Venue");
            venueAddress = venues.get(0).path("address").path("line1").asText("Unknown Address");
        }

        events.add(new Event(name, date, time, venueName, venueAddress,
                             url, genre, parsePriceRange(node), SOURCE_NAME));
    }
    return events;
}

private String parsePriceRange(JsonNode node) {
    JsonNode ranges = node.path("priceRanges");
    if (!ranges.isArray() || ranges.size() == 0) return "Price TBC";
    double min = ranges.get(0).path("min").asDouble(0);
    double max = ranges.get(0).path("max").asDouble(0);
    if (max == 0) return "Free";
    if (min == max) return String.format("£%.0f", min);
    return String.format("£%.0f – £%.0f", min, max);
}

}