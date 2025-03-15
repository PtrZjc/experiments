package pl.zajacp.playground.gatheres;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.nCopies;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("preview")
class ManualGatherersPracticeTest {

    @Test
    void slidingWindowAverage_shouldReturnCorrectAverages() {
        // Input: 1, 2, 3, 4, 5
        // Output: 2.0, 3.0, 4.0 (averages of [1,2,3], [2,3,4], [3,4,5])
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        int windowSize = 3;

        Supplier<List<Integer>> initializer = ArrayList::new;

        Gatherer.Integrator<List<Integer>, Integer, Double> integrator = Gatherer.Integrator.of(
                (list, num, _) -> {
                    list.add(num);
                    return true;
                }
        );

        BiConsumer<List<Integer>, Gatherer.Downstream<? super Double>> finisher = (list, downstream) -> {
            IntStream.rangeClosed(0, list.size() - windowSize)
                    .forEach(slide -> downstream.push(IntStream.range(slide, slide + windowSize)
                            .map(list::get)
                            .average()
                            .orElseThrow())
                    );
        };

        Gatherer<Integer, List<Integer>, Double> gatherer = Gatherer.ofSequential(initializer, integrator, finisher);

        List<Double> result = numbers.stream()
                .gather(gatherer)
                .toList();

        assertThat(result).containsExactly(2.0, 3.0, 4.0);
    }

    @Test
    void distinctPairs_shouldReturnAdjacentPairs() {
        // Input: 1, 2, 3, 4, 5
        // Output: [1,2], [3,4]
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        enum Num {ODD, EVEN}

        class Divider {
            final Map<Num, List<Integer>> nums = new EnumMap<>(Map.of(
                    Num.ODD, new ArrayList<>(),
                    Num.EVEN, new ArrayList<>()
            ));
        }

        Gatherer.Integrator<Divider, Integer, List<Integer>> integrator = Gatherer.Integrator.of(
                (divider, num, _) -> divider.nums
                        .get(num % 2 == 0 ? Num.EVEN : Num.ODD)
                        .add(num)
        );

        BiConsumer<Divider, Gatherer.Downstream<? super List<Integer>>> finisher = (divider, downstream) -> {
            var odds = divider.nums.get(Num.ODD);
            var even = divider.nums.get(Num.EVEN);

            IntStream.range(0, Math.min(odds.size(), even.size()))
                    .forEach(i -> {
                        downstream.push(List.of(odds.get(i), even.get(i)));
                    });
        };

        Gatherer<Integer, Divider, List<Integer>> gatherer = Gatherer.ofSequential(Divider::new, integrator, finisher);

        List<List<Integer>> result = numbers.stream()
                .gather(gatherer)
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

        Supplier<List<Integer>> initializer = ArrayList::new;

        Gatherer.Integrator<List<Integer>, Integer, List<Integer>> integrator = Gatherer.Integrator.of(
                (list, num, _) -> {
                    list.add(num);
                    return true;
                }
        );

        BiConsumer<List<Integer>, Gatherer.Downstream<? super List<Integer>>> finisher = (list, downstream) -> {
            IntStream.range(0, list.size() - 1)
                    .forEach(slide -> downstream.push(List.of(list.get(slide), list.get(slide + 1))));
        };

        Gatherer<Integer, List<Integer>, List<Integer>> gatherer = Gatherer.ofSequential(initializer, integrator, finisher);

        List<List<Integer>> result = numbers.stream()
                .gather(gatherer)
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

        class Sum {
            int value;
        }

        Gatherer.Integrator<Sum, Integer, Integer> integrator = Gatherer.Integrator.of(
                (sum, num, downstream) -> {
                    sum.value = sum.value + num;
                    return downstream.push(sum.value);
                }
        );

        Gatherer<Integer, Sum, Integer> gatherer = Gatherer.ofSequential(Sum::new, integrator);

        List<Integer> result = numbers.stream()
                .gather(gatherer)
                .toList();

        assertThat(result).containsExactly(1, 3, 6, 10, 15);
    }

    static Stream<Arguments> skipTake_shouldSkipAndTakeCorrectElements() {
        return Stream.of(
                Arguments.arguments(1, 2, List.of(2, 3, 5, 6, 8, 9)),
                Arguments.arguments(2, 3, List.of(3, 4, 5, 8, 9, 10))
        );
    }

    @MethodSource
    @ParameterizedTest
    void skipTake_shouldSkipAndTakeCorrectElements(int skip, int take, List<Integer> expectedValue) {
        // skip -> take -> skip -> take ...
        // Input: 1, 2, 3, 4, 5, 6, 7, 8
        // Output: 2, 3, 5, 6, 8, 9
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        enum Action {SKIP, TAKE}

        List<Action> actionTemplate = Stream.concat(
                nCopies(skip, Action.SKIP).stream(),
                nCopies(take, Action.TAKE).stream()
        ).toList();

        Supplier<List<Integer>> initializer = ArrayList::new;

        Gatherer.Integrator<List<Integer>, Integer, Integer> integrator = Gatherer.Integrator.of(
                (list, num, _) -> {
                    list.add(num);
                    return true;
                }
        );

        BiConsumer<List<Integer>, Gatherer.Downstream<? super Integer>> finisher = (list, downstream) -> {
            IntStream.range(0, list.size())
                    .forEach(i -> {
                        if (actionTemplate.get(i % (skip + take)) == Action.TAKE) {
                            downstream.push(list.get(i));
                        }
                    });
        };

        Gatherer<Integer, List<Integer>, Integer> gatherer = Gatherer.ofSequential(initializer, integrator, finisher);

        List<Integer> result = numbers.stream()
                .gather(gatherer)
                .toList();

        assertThat(result).isEqualTo(expectedValue);
    }

    @Test
    void deduplication_shouldRemoveConsecutiveDuplicates() {
        // Input: 1, 1, 2, 3, 3, 3, 4, 5, 5
        // Output: 1, 2, 3, 4, 5
        List<Integer> numbers = List.of(1, 1, 2, 3, 3, 3, 4, 5, 5);

        class Deduplicator {
            Set<Integer> nums = new HashSet<>();
        }

        Gatherer.Integrator<Deduplicator, Integer, Integer> integrator = Gatherer.Integrator.of(
                (dd, num, _) -> {
                    dd.nums.add(num);
                    return true;
                }
        );

        BiConsumer<Deduplicator, Gatherer.Downstream<? super Integer>> finisher = (dd, downstream) -> {
            dd.nums.forEach(downstream::push);
        };

        Gatherer<Integer, Deduplicator, Integer> gatherer = Gatherer.ofSequential(Deduplicator::new, integrator, finisher);

        List<Integer> result = numbers.stream()
                .gather(gatherer)
                .toList();

        assertThat(result).containsExactly(1, 2, 3, 4, 5);
    }

//    @Test
//    void batchProcessing_shouldGroupByThreshold() {
//        // Input: 1, 2, 3, 4, 5, 6, 7, 8, 9
//        // Batch size: 3, Threshold: 10
//        // Output: [4,5,6], [7,8,9]
//        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
//
//        List<List<Integer>> result = numbers.stream()
//            // Your implementation here
//            .toList();
//
//        assertThat(result).hasSize(2);
//        assertThat(result.get(0)).containsExactly(4, 5, 6);
//        assertThat(result.get(1)).containsExactly(7, 8, 9);
//    }
}
