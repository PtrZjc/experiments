package pl.zajacp.playground.gatheres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MapperPlayground {
    public static void main(String[] args) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var jsonMap = """
                {
                    "param": {
                    }
                }
                """;

        var jsonArray = """
                {
                    "param": [
                    ]
                }
                """;

        Stream.of(
                mapper.readValue(jsonMap, Json.class),
                mapper.readValue(jsonArray, Json.class)
        ).forEach(json -> {
            System.out.print("json = " + json + " " + "has $.param of type:  ");
            if (json.param instanceof Map) {
                System.out.println("Map");
            } else if (json.param instanceof List<?>) {
                System.out.println("List");
            }
        });
    }

    record Json(Object param) {
    }
}
