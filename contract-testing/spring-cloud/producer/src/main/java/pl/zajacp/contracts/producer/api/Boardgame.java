package pl.zajacp.contracts.producer.api;

import java.math.BigDecimal;

public record Boardgame(String title, Integer yearPublished, BigDecimal avgRating) {
}
