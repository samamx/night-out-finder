package com.nightout.config;

import com.nightout.client.*;
import com.nightout.service.EventService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AppConfig {

    @Bean
    public Dotenv dotenv() {
        return Dotenv.load();
    }

    @Bean
    public List<EventSource> sources(Dotenv dotenv) {
        List<EventSource> sources = new ArrayList<>();

        String ticketmasterKey = dotenv.get("TICKETMASTER_API_KEY", "");
        if (!ticketmasterKey.isBlank()) {
            sources.add(new TicketmasterClient(ticketmasterKey));
            System.out.println("Ticketmaster connected");
        } else {
            System.out.println("Ticketmaster skipped (no key in .env)");
        }

        String skiddleKey = dotenv.get("SKIDDLE_API_KEY", "");
        if (!skiddleKey.isBlank()) {
            sources.add(new SkiddleClient(skiddleKey));
            System.out.println("Skiddle connected");
        } else {
            System.out.println("Skiddle skipped (no key in .env)");
        }

        String meetupToken = dotenv.get("MEETUP_ACCESS_TOKEN", "");
        if (!meetupToken.isBlank()) {
            sources.add(new MeetupClient(meetupToken));
            System.out.println("Meetup connected");
        } else {
            System.out.println("Meetup skipped (no token in .env)");
        }

        if (sources.isEmpty()) {
            throw new IllegalStateException("No API keys found in .env, add at least one to continue.");
        }

        return sources;
    }

    @Bean
    public EventAggregator eventAggregator(List<EventSource> sources) {
        return new EventAggregator(sources);
    }

    @Bean
    public EventService eventService(EventAggregator eventAggregator) {
        return new EventService(eventAggregator);
    }


}