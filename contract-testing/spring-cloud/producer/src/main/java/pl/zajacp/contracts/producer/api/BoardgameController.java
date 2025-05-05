package pl.zajacp.contracts.producer.api;


import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Validated
@RestController
public class BoardgameController {

    @GetMapping("/boardgame/{yearPublished}")
    public Boardgame getBestBggInYear(@PathVariable
                                      @Range(min = 2010, max = 2025, message = "Year must be between 2010 and 2025")
                                      Integer yearPublished) {
        return switch (yearPublished) {
            case 2010 -> new Boardgame("7 Wonders", yearPublished, new BigDecimal("7.8"));
            case 2011 -> new Boardgame("Eclipse", yearPublished, new BigDecimal("7.9"));
            case 2012 -> new Boardgame("Terra Mystica", yearPublished, new BigDecimal("8.1"));
            case 2013 -> new Boardgame("Concordia", yearPublished, new BigDecimal("8.1"));
            case 2014 -> new Boardgame("Orléans", yearPublished, new BigDecimal("8.0"));
            case 2015 -> new Boardgame("Pandemic Legacy: Season 1", yearPublished, new BigDecimal("8.4"));
            case 2016 -> new Boardgame("Scythe", yearPublished, new BigDecimal("8.2"));
            case 2017 -> new Boardgame("Gloomhaven", yearPublished, new BigDecimal("8.7"));
            case 2018 -> new Boardgame("Root", yearPublished, new BigDecimal("8.1"));
            case 2019 -> new Boardgame("Wingspan", yearPublished, new BigDecimal("8.1"));
            case 2020 -> new Boardgame("Dune: Imperium", yearPublished, new BigDecimal("8.2"));
            case 2021 -> new Boardgame("Ark Nova", yearPublished, new BigDecimal("8.5"));
            case 2022 -> new Boardgame("Cascadia", yearPublished, new BigDecimal("8.0"));
            case 2023 -> new Boardgame("Frosthaven", yearPublished, new BigDecimal("8.9"));
            case 2024 -> new Boardgame("Weather Machine", yearPublished, new BigDecimal("8.2"));
            case 2025 -> new Boardgame("Distilled", yearPublished, new BigDecimal("8.1"));
            default -> throw new IllegalArgumentException("Year must be between 2010 and 2025");
        };
    }

    public record Boardgame(String title, Integer yearPublished, BigDecimal avgRating) {
    }

}
