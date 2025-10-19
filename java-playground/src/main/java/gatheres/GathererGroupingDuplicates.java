package gatheres;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

public class GathererGroupingDuplicates {
    static void main() {
// --- How to use it ---
        Stream.of(1, 1, 2, 3, 3, 3, 1, 2, 2)
                .gather(consecutiveDuplicates())
                .forEach(System.out::println);

        // Output:
        // [1, 1]
        // [2]
        // [3, 3, 3]
        // [1]
        // [2, 2]
    }


    public static <T> Gatherer<T, ?, List<T>> consecutiveDuplicates() {
        // We need a state to hold the current group
        class GroupingState<T> {
            List<T> currentGroup = new ArrayList<>();
        }
        // 1. Initializer: Start with an empty state
        Supplier<GroupingState<T>> initializer = GroupingState::new;

        // 2. Integrator: The core logic
        Gatherer.Integrator<GroupingState<T>, T, List<T>> integrator =
                (state, element, downstream) -> {

                    // If the group is empty or the element matches, add it
                    if (state.currentGroup.isEmpty() || state.currentGroup.getFirst().equals(element)) {
                        state.currentGroup.add(element);
                    } else {
                        // New element: emit the old group...
                        downstream.push(List.copyOf(state.currentGroup));
                        // ...and start a new group
                        state.currentGroup.clear();
                        state.currentGroup.add(element);
                    }
                    return true; // Continue processing
                };

        // 3. Finisher: Emit the last group
        BiConsumer<GroupingState<T>, Gatherer.Downstream<? super List<T>>> finisher =
                (state, downstream) -> {

                    if (!state.currentGroup.isEmpty()) {
                        downstream.push(List.copyOf(state.currentGroup));
                    }
                };

        return Gatherer.ofSequential(initializer, integrator, finisher);
    }
}
