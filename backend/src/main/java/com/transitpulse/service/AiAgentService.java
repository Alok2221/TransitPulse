package com.transitpulse.service;

import com.transitpulse.domain.TransportMode;
import com.transitpulse.dto.AgentAdviseRequest;
import com.transitpulse.dto.AgentAdviseResponse;
import com.transitpulse.dto.CompareRequest;
import com.transitpulse.dto.CompareResponse;
import com.transitpulse.dto.ModeOptionDto;
import com.transitpulse.dto.WeatherDto;
import org.springframework.stereotype.Service;

@Service
public class AiAgentService {

    private final NlPreferenceParser parser;
    private final WeatherService weatherService;
    private final ComparisonService comparisonService;
    private final CityGrammar cityGrammar;

    public AiAgentService(
            NlPreferenceParser parser,
            WeatherService weatherService,
            ComparisonService comparisonService,
            CityGrammar cityGrammar
    ) {
        this.parser = parser;
        this.weatherService = weatherService;
        this.comparisonService = comparisonService;
        this.cityGrammar = cityGrammar;
    }

    public AgentAdviseResponse advise(AgentAdviseRequest request) {
        NlPreferenceParser.ParsedTrip parsed = parser.parse(request.getMessage());
        WeatherService.WeatherResult weatherResult = weatherService.forCity(parsed.destination, parsed.morning);
        WeatherDto weather = weatherResult.weather();

        CompareRequest compareRequest = new CompareRequest();
        compareRequest.setOrigin(parsed.origin);
        compareRequest.setDestination(parsed.destination);
        compareRequest.setDistanceKm(null);
        compareRequest.setPriority(parsed.priority);
        compareRequest.setAvoidParking(parsed.avoidParking);
        compareRequest.setMaxHours(parsed.maxHours);

        CompareResponse comparison = comparisonService.compare(compareRequest, weather, weatherResult.source());
        ModeOptionDto top = comparison.getRanking().isEmpty() ? null : comparison.getRanking().get(0);
        TransportMode mode = top == null ? null : top.getMode();

        AgentAdviseResponse response = new AgentAdviseResponse();
        response.setParsedConstraints(parsed.constraints);
        response.setDetectedPriority(parsed.priority);
        response.setWeather(weather);
        response.setComparison(comparison);
        response.setRecommendedMode(mode);
        response.setRecommendation(buildRecommendation(mode, parsed, weather));
        response.setReasoning(buildReasoning(comparison, parsed, weather, top));
        return response;
    }

    private String buildRecommendation(TransportMode mode, NlPreferenceParser.ParsedTrip parsed, WeatherDto weather) {
        return "Na trasę " + parsed.origin + " → " + parsed.destination
                + " najlepiej sprawdzi się " + modeLabel(mode)
                + " — priorytet: " + priorityLabel(parsed.priority)
                + ", w " + cityGrammar.inCity(weather.getCity()) + " teraz " + weather.getLabelPl() + ".";
    }

    private String buildReasoning(
            CompareResponse comparison,
            NlPreferenceParser.ParsedTrip parsed,
            WeatherDto weather,
            ModeOptionDto top
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Trasa ").append(parsed.origin).append(" → ").append(parsed.destination);
        sb.append(" to około ").append(formatKm(comparison.getDistanceKm())).append(" km. ");

        if (parsed.maxHours != null) {
            sb.append("Masz limit ").append(formatHours(parsed.maxHours)).append(", więc odrzucam opcje, które się w nim nie mieszczą. ");
        }
        if (parsed.avoidParking) {
            sb.append("Nie doliczam parkingu do kosztu samochodu. ");
        }

        if (!weather.isBikeFriendly()) {
            sb.append("Pogoda (").append(weather.getLabelPl()).append(") słabo nadaje się na rower. ");
        } else {
            sb.append("Pogoda (").append(weather.getLabelPl()).append(") nie wyklucza roweru. ");
        }

        if (top != null) {
            sb.append("W rankingu wygrywa ").append(top.getLabel().toLowerCase())
                    .append(": ok. ").append(formatMoney(top.getCostPln()))
                    .append(", ").append(formatDuration(top.getDurationMinutes()))
                    .append(", ").append(formatCo2(top.getCo2Kg())).append(".");
            String tip = friendlyTip(top);
            if (!tip.isBlank()) {
                sb.append(" ").append(tip);
            }
        }
        return sb.toString().trim();
    }

    private String friendlyTip(ModeOptionDto top) {
        return switch (top.getMode()) {
            case TRAIN -> "Cena biletu jest szacunkowa, a do czasu doliczam dojście na peron.";
            case BUS -> "Cena bileta autobusowego jest szacunkowa.";
            case CAR -> top.isFeasible()
                    ? "W koszcie auta liczę paliwo i amortyzację."
                    : "Samochód wypada słabiej przy Twoich ograniczeniach.";
            case BIKE -> top.isFeasible()
                    ? "Rower jest najtańszy i bez emisji, ale wymaga czasu i sił."
                    : "Rower odpada przez dystans, pogodę albo limit czasu.";
        };
    }

    private String formatKm(double km) {
        if (Math.abs(km - Math.rint(km)) < 0.05) {
            return String.valueOf((int) Math.rint(km));
        }
        return String.format(java.util.Locale.GERMANY, "%.1f", km);
    }

    private String formatHours(double hours) {
        if (Math.abs(hours - Math.rint(hours)) < 0.05) {
            int h = (int) Math.rint(hours);
            return h + (h == 1 ? " godzinę" : " godzin");
        }
        return String.format(java.util.Locale.GERMANY, "%.1f h", hours);
    }

    private String formatMoney(double pln) {
        return String.format(java.util.Locale.GERMANY, "%.2f zł", pln);
    }

    private String formatCo2(double kg) {
        return String.format(java.util.Locale.GERMANY, "%.2f kg CO₂", kg);
    }

    private String formatDuration(double minutes) {
        int total = (int) Math.round(minutes);
        int h = total / 60;
        int m = total % 60;
        if (h == 0) {
            return m + " min";
        }
        if (m == 0) {
            return h + " h";
        }
        return h + " h " + m + " min";
    }

    private String modeLabel(TransportMode mode) {
        if (mode == null) {
            return "brak jednoznacznej rekomendacji";
        }
        return switch (mode) {
            case CAR -> "samochód";
            case TRAIN -> "pociąg";
            case BUS -> "autobus";
            case BIKE -> "rower";
        };
    }

    private String priorityLabel(com.transitpulse.domain.Priority priority) {
        return switch (priority) {
            case CHEAPEST -> "najtaniej";
            case FASTEST -> "najszybciej";
            case GREENEST -> "najzieleniej";
            case BALANCED -> "zrównoważony wybór";
        };
    }
}
