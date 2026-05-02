package com.nightout;

import com.nightout.client.*;
import com.nightout.model.Event;
import com.nightout.model.PriceFilter;
import com.nightout.service.EventService;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

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
            System.out.println("No API keys found in .env, add at least one to continue");
            System.exit(1);
        }

        EventService eventService = new EventService(new EventAggregator(sources));
        Scanner scanner = new Scanner(System.in);

        printBanner(sources.size());

        while (true) {
            System.out.print("\nWhat do you feel like doing? (or 'quit' to exit)\n> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit")) {
                System.out.println("\nEnjoy your night out!\n");
                break;
            }

            if (input.isBlank()) {
                System.out.println("Please enter a valid search term e.g. \"live jazz\"\n");
                continue;
            }

            PriceFilter priceFilter = askPricePreference(scanner);
            List<Event> events = eventService.findEvents(input, priceFilter, 5);

            if (events.isEmpty()) {
                System.out.println("No events found, try a different search term or adjust your price preference.\n");
            } else {
                System.out.println("Found " + events.size() + " events:\n");
                System.out.println("-".repeat(62));
                for (int i = 0; i < events.size(); i++) {
                    System.out.println((i + 1) + ". " + events.get(i));
                    System.out.println("-".repeat(62));
                }
            }
        }                                      

        eventService.shutdown();
        scanner.close();
    }                                       

    private static PriceFilter askPricePreference(Scanner scanner) {
        while (true) {
            System.out.println("\nWhat's your budget?");
            System.out.println("   [1] Free events only");
            System.out.println("   [2] Set a price range");
            System.out.println("   [3] Any price");
            System.out.print("> ");
            switch (scanner.nextLine().trim()) {
                case "1" -> { return PriceFilter.free(); }
                case "2" -> {
                    Double min = promptPrice(scanner, "   Min price (£): ");
                    if (min == null) continue;
                    Double max = promptPrice(scanner, "   Max price (£): ");
                    if (max == null) continue;
                    if (max < min) { System.out.println("Max must be greater than min."); continue; }
                    return PriceFilter.range(min, max);
                }
                case "3" -> { return PriceFilter.any(); }
                default  -> System.out.println("Enter 1, 2, or 3.");
            }
        }
    }

    private static Double promptPrice(Scanner scanner, String prompt) {
        System.out.print(prompt);
        try {
            double value = Double.parseDouble(scanner.nextLine().trim().replace("£", ""));
            if (value < 0) { System.out.println("Price cannot be negative."); return null; }
            return value;
        } catch (NumberFormatException e) {
            System.out.println("Enter a valid number e.g. 20");
            return null;
        }
    }

    private static void printBanner(int sourceCount) {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║       🌆  NIGHT OUT FINDER — LONDON      ║");
        System.out.println("║   Tell me what you feel like doing...    ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("\n  Searching across " + sourceCount + " source(s) simultaneously.");
        System.out.println("  Examples: \"live jazz\", \"comedy night\", \"art exhibition\"\n");
    }

}                        