package pl.zajacp.contracts.consumer.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.io.IOException;

@Configuration
public class ClientConfiguration {

    @Value("${producer.url:http://localhost:8080}")
    private String producerUrl;

    @Bean
    public BoardgameClient boardgameClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(producerUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, ClientConfiguration::handle4xxResponse)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(BoardgameClient.class);
    }

    private static void handle4xxResponse(HttpRequest request, ClientHttpResponse response) throws IOException {
        throw new BoardgameClientException(String.format(
                "Request failed with status %s, URL: %s, Response: %s",
                response.getStatusCode(),
                request.getURI(),
                new String(response.getBody().readAllBytes())
        ));
    }
}
