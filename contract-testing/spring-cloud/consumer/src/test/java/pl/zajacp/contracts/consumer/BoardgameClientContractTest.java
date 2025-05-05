package pl.zajacp.contracts.consumer;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.ActiveProfiles;
import pl.zajacp.contracts.consumer.client.BoardgameClient;
import pl.zajacp.contracts.consumer.client.BoardgameClientException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureStubRunner(
        ids = "pl.zajacp.contracts:producer:+:stubs",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class BoardgameClientContractTest {

    @Autowired
    private BoardgameClient boardgameClient;

    @Test
    void shouldReturnBoardgameForYear2020() {
        // when
        BoardgameClient.Boardgame boardgame = boardgameClient.getBestBoardgameForYear(2020);

        // then
        assertThat(boardgame).isNotNull();
        assertThat(boardgame.title()).isEqualTo("Dune: Imperium");
        assertThat(boardgame.yearPublished()).isEqualTo(2020);
        assertThat(boardgame.avgRating()).isEqualTo(new BigDecimal("8.2"));
    }

    @Test
    void shouldThrowExceptionForInvalidYear() {
        // when & then
        assertThatThrownBy(() -> boardgameClient.getBestBoardgameForYear(2000))
                .isInstanceOf(BoardgameClientException.class)
                .hasMessageContaining("Request failed with status 400")
                .hasMessageContaining("Year must be between 2010 and 2025");
    }
}
