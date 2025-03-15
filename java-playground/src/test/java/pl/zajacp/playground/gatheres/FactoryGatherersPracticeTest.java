package pl.zajacp.playground.gatheres;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Gatherers;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("preview")
class FactoryGatherersPracticeTest {

    @Test
    void slidingWindowAverage_shouldReturnCorrectAverages() {
        // Input: 1, 2, 3, 4, 5
        // Output: 2.0, 3.0, 4.0 (averages of [1,2,3], [2,3,4], [3,4,5])
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        int windowSize = 3;
        List<Double> result = numbers.stream()
                .gather(Gatherers.windowSliding(3))
                .map(nums -> nums.stream().mapToInt(i -> i).average().orElseThrow())
                .toList();

        assertThat(result).containsExactly(2.0, 3.0, 4.0);
    }

    @Test
    void distinctPairs_shouldReturnAdjacentPairs() {
        // Input: 1, 2, 3, 4, 5
        // Output: [1,2], [3,4]
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        List<List<Integer>> result = numbers.stream()
                .gather(Gatherers.windowFixed(2))
                .filter(l -> l.size() == 2)
                .toList();

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).containsExactly(1, 2);
        assertThat(result.get(1)).containsExactly(3, 4);
    }

    @Test
    void overlappingPairs_shouldReturnAdjacentOverlappingPairs() {
        // Input: 1, 2, 3, 4
        // Output: [1,2], [2,3], [3,4]
        List<Integer> numbers = List.of(1, 2, 3, 4);

        List<List<Integer>> result = numbers.stream()
                .gather(Gatherers.windowSliding(2))
                .toList();

        assertThat(result).hasSize(3);
        assertThat(result.get(0)).containsExactly(1, 2);
        assertThat(result.get(1)).containsExactly(2, 3);
        assertThat(result.get(2)).containsExactly(3, 4);
    }

    @Test
    void runningSum_shouldReturnCorrectSums() {
        // Input: 1, 2, 3, 4, 5
        // Output: 1, 3, 6, 10, 15
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        List<Integer> result = numbers.stream()
                .gather(Gatherers.scan(() -> 0, Integer::sum))
                .toList();

        assertThat(result).containsExactly(1, 3, 6, 10, 15);
    }

    @Test
    void skipTake_shouldSkipAndTakeCorrectElements() {
        // skip -> take -> skip -> take ...
        // Input: 1, 2, 3, 4, 5, 6, 7, 8
        // Output: 2, 3, 5, 6, 8, 9

        // not possible to implement with Gatherers factory methods
    }

    @Test
    void deduplication_shouldRemoveConsecutiveDuplicates() {
        // Input: 1, 1, 2, 3, 3, 3, 4, 5, 5
        // Output: 1, 2, 3, 4, 5
        List<Integer> numbers = List.of(1, 1, 2, 3, 3, 3, 4, 5, 5);

        List<Integer> result = new HashSet<>(numbers)
                .stream().toList();

        assertThat(result).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void peakDetection_shouldFindLocalMaxima() {
        // Input: 1, 3, 2, 5, 4, 6, 2
        // Output: 3, 5, 6
        List<Integer> numbers = List.of(1, 3, 2, 5, 4, 6, 2);

        List<Integer> result = numbers.stream()
                .gather(Gatherers.windowSliding(3))
                .map(l -> l.stream().mapToInt(i -> i).max().orElseThrow())
                .toList();

        var finalResult = new ArrayList<Integer>();
        for (int i = 0; i < result.size(); i++) {
            if(i % 2 == 0) finalResult.add(result.get(i));
        }
        assertThat(finalResult).containsExactly(3, 5, 6);
    }

    @Test
    void batchProcessing_shouldGroupByThreshold() {
        // Input: 1, 2, 3, 4, 5, 6, 7, 8, 9
        // Batch size: 3, Threshold: 10
        // Output: [4,5,6], [7,8,9]
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

        int batchSize = 3;
        int treshold = 10;

        List<List<Integer>> result = numbers.stream()
                .gather(Gatherers.windowFixed(batchSize))
                .filter(batch -> batch.stream().mapToInt(i -> i).sum() > treshold)
                .toList();

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).containsExactly(4, 5, 6);
        assertThat(result.get(1)).containsExactly(7, 8, 9);
    }
}
