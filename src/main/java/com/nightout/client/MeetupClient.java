package com.nightout.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nightout.model.Event;
import com.nightout.model.PriceFilter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class MeetupClient implements EventSource {
    private static final String GRAPHQL_URL = "https://api.meetup.com/gql";
    private static final String SOURCE_NAME = "Meetup";

    private final String accessToken;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;


    public MeetupClient(String accessToken) {
    this.accessToken = accessToken;
    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = new ObjectMapper();
}

public String getSourceName() {
    return SOURCE_NAME;
}

@Override
public List<Event> searchEvents(String keyword, PriceFilter priceFilter, int maxResults) throws Exception {

    String query = String.format("""
        {
          "query": "query { keywordSearch(filter: { query: \\"%s\\", lat: 51.5074, lon: -0.1278, radius: 10 }, first: %d) { edges { node { result { ... on Event { title dateTime venue { name address } eventUrl isFree } } } } } }"
        }
        """, keyword.replace("\"", "\\\""), maxResults);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(GRAPHQL_URL))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + accessToken)
            .POST(HttpRequest.BodyPublishers.ofString(query))
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
        System.err.println("Meetup error: HTTP " + response.statusCode());
        return new ArrayList<>();
    }

    List<Event> events = parseEvents(response.body());

    if (priceFilter.isFree())
        events = events.stream().filter(e -> e.getPriceRange().equals("Free")).toList();

    return events;
}

private List<Event> parseEvents(String json) throws Exception {
    List<Event> events = new ArrayList<>();
    JsonNode root = objectMapper.readTree(json);
    JsonNode edges = root.path("data").path("keywordSearch").path("edges");
    if (!edges.isArray()) return events;

    for (JsonNode edge : edges) {
        JsonNode result = edge.path("node").path("result");

        String name    = result.path("title").asText("Unknown Event");
        String url     = result.path("eventUrl").asText("#");
        boolean isFree = result.path("isFree").asBoolean(true);
        String price   = isFree ? "Free" : "Paid (see link)";

        String rawDateTime = result.path("dateTime").asText("TBC");
        String date = rawDateTime.length() >= 10 ? rawDateTime.substring(0, 10) : "TBC";
        String time = rawDateTime.length() >= 16 ? rawDateTime.substring(11, 16) : "TBC";

        String venueName = result.path("venue").path("name").asText("Online / TBC");
        String venueAddr = result.path("venue").path("address").asText("See event link");

        events.add(new Event(name, date, time, venueName, venueAddr,
                             url, "Community", price, SOURCE_NAME));
    }
    return events;
}

}

