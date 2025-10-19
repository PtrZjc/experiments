package gatheres;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests demonstrating the built-in Java Stream Gatherers API.
 * <p>
 * Gatherers are intermediate stream operations introduced in JDK 22 (JEP 461)
 * that allow custom transformations of stream elements. They are more powerful
 * than standard intermediate operations because they can be stateful, change
 * the number of elements, and short-circuit.
 */
class GatherersExamplesTest {

    /**
     * Demonstrates {@link Gatherers#windowFixed(int)}.
     * <p>
     * Groups consecutive elements into fixed-size lists. The last window may
     * contain fewer elements if the stream size is not evenly divisible.
     */
    @Test
    void shouldDemonstrateWindowFixed() {
        // when
        var result = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.windowFixed(2))
                .toList();

        // then
        assertThat(result).containsExactly(
                List.of(1, 2),
                List.of(3, 4),
                List.of(5)
        );
    }

    /**
     * Demonstrates {@link Gatherers#windowSliding(int)}.
     * <p>
     * Creates overlapping windows that slide over the stream elements.
     * Each window contains the specified number of elements, sliding one
     * element at a time.
     */
    @Test
    void shouldDemonstrateWindowSliding() {
        // when
        var result = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.windowSliding(2))
                .toList();

        // then
        assertThat(result).containsExactly(
                List.of(1, 2),
                List.of(2, 3),
                List.of(3, 4),
                List.of(4, 5)
        );
    }

    /**
     * Demonstrates {@link Gatherers#scan(java.util.function.Supplier, java.util.function.BiFunction)}.
     * <p>
     * Performs a prefix scan (cumulative operation) that emits each intermediate result.
     * Similar to {@code reduce}, but outputs all intermediate values instead of just the final result.
     * This example calculates running sums: 1, 1+2=3, 3+3=6, 6+4=10.
     */
    @Test
    void shouldDemonstrateScanWithRunningSum() {
        // when
        var result = Stream.of(1, 2, 3, 4)
                .gather(Gatherers.scan(() -> 0, Integer::sum))
                .toList();

        // then
        assertThat(result).containsExactly(1, 3, 6, 10);
    }

    /**
     * Demonstrates {@link Gatherers#scan(java.util.function.Supplier, java.util.function.BiFunction)}
     * with String concatenation.
     * <p>
     * Shows how scan can be used with non-numeric types to build up a result incrementally.
     * Each step concatenates the current word to the accumulated string.
     */
    @Test
    void shouldDemonstrateScanWithStringConcatenation() {
        // when
        var result = Stream.of("Hello", "World", "from", "Gatherers")
                .gather(Gatherers.scan(() -> "", (acc, word) -> acc.isEmpty() ? word : acc + " " + word))
                .toList();

        // then
        assertThat(result).containsExactly(
                "Hello",
                "Hello World",
                "Hello World from",
                "Hello World from Gatherers"
        );
    }

    /**
     * Demonstrates {@link Gatherers#fold(java.util.function.Supplier, java.util.function.BiFunction)}.
     * <p>
     * Performs a stateful reduction that processes all elements and emits a single result.
     * Unlike {@code scan}, which emits intermediate results, {@code fold} only emits the final result.
     */
    @Test
    void shouldDemonstrateFoldWithSum() {
        // when
        var result = Stream.of(1, 2, 3, 4)
                .gather(Gatherers.fold(() -> 0, Integer::sum))
                .toList();

        // then
        assertThat(result).containsExactly(10);
    }

    /**
     * Demonstrates {@link Gatherers#fold(java.util.function.Supplier, java.util.function.BiFunction)}
     * for finding the maximum value.
     * <p>
     * Shows how fold can be used for operations other than sum, such as finding
     * the maximum element in a stream.
     */
    @Test
    void shouldDemonstrateFoldWithMax() {
        // when
        var result = Stream.of(5, 12, 8, 42, 3, 15)
                .gather(Gatherers.fold(() -> Integer.MIN_VALUE, Integer::max))
                .toList();

        // then
        assertThat(result).containsExactly(42);
    }
}
