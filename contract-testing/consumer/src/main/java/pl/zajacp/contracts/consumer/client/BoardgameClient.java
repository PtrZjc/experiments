package pl.zajacp.contracts.consumer.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.math.BigDecimal;

@HttpExchange(accept = "application/json", contentType = "application/json")
public interface BoardgameClient {

    @GetExchange("/boardgame/{year}")
    Boardgame getBestBoardgameForYear(@PathVariable int year);

    record Boardgame(String title, Integer yearPublished, BigDecimal avgRating) {
    }
}
