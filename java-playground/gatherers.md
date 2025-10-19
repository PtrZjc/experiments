Here's a cheat sheet for Java Stream Gatherers, a powerful feature for creating custom intermediate
stream operations.

**Note:** Gatherers were introduced as a **preview feature in JDK 22** (JEP 461).

-----

## 🚀 Java Stream Gatherers: The Basics

A **Gatherer** lets you define a custom **intermediate operation** for a Stream pipeline. You use it
with the new `Stream.gather()` method.

Think of it this way:

* `collect()` is for **terminal operations** (ending the stream, e.g., `toList()`).
* `gather()` is for **intermediate operations** (transforming the stream and passing it on, e.g.,
  `map()`, `filter()`).

Gatherers are powerful because they can be:

* **Stateful:** They can remember previous elements.
* **Transformative:** They can change the number of elements (e.g., one-to-many, many-to-one, or
  many-to-many).
* **Short-circuiting:** They can stop processing early.

### Syntax

The new method on the `Stream` interface is:

```java
Stream<R> gather(Gatherer<? super T, ?, R> gatherer)
```

-----

## 🛠️ Built-in Gatherers

Java provides a utility class, `java.util.stream.Gatherers`, with several common operations ready to
use.

| Method                                                         | Description                                                                       | Example                                                                                                                  |
|:---------------------------------------------------------------|:----------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------|
| **`windowFixed(int size)`**                                    | Groups elements into fixed-size lists. The last list may be smaller.              | `Stream.of(1, 2, 3, 4, 5)`<br>`.gather(Gatherers.windowFixed(2))`<br>► `Stream([1, 2], [3, 4], [5])`                     |
| **`windowSliding(int size)`**                                  | Creates sliding windows of elements.                                              | `Stream.of(1, 2, 3, 4)`<br>`.gather(Gatherers.windowSliding(2))`<br>► `Stream([1, 2], [2, 3], [3, 4])`                   |
| **`scan(Supplier<R> init, BiFunction<R, T, R> scanner)`**      | Performs a "prefix scan" (like `reduce` but emits each intermediate result).      | `Stream.of(1, 2, 3, 4)`<br>`.gather(Gatherers.scan(() -> 0, Integer::sum))`<br>► `Stream(1, 3, 6, 10)`                   |
| **`fold(Supplier<R> init, BiFunction<R, T, R> folder)`**       | A stateful `reduce`. Processes all elements and emits a *single* result.          | `Stream.of(1, 2, 3, 4)`<br>`.gather(Gatherers.fold(() -> 0, Integer::sum))`<br>► `Stream(10)`                            |
| **`mapConcurrent(int maxConcurrency, Function<T, R> mapper)`** | A stateful `map` that applies the mapping function concurrently (useful for I/O). | `Stream.of(url1, url2)`<br>`.gather(Gatherers.mapConcurrent(4, this::fetchUrl))`<br>► `Stream(page1, page2)` (unordered) |

-----

## 🧑‍💻 Creating a Custom Gatherer

To create your own gatherer, you implement the `Gatherer<T, A, R>` interface.

* `T`: The **input** element type (from upstream).
* `A`: The type of the internal, mutable **state** (the "accumulator").
* `R`: The **output** element type (to downstream).

You need to implement (at minimum) `initializer()` and `integrator()`.

### The `Gatherer` Interface Methods

```java
public interface Gatherer<T, A, R> {

    // 1. Creates the mutable state object (e.g., a new ArrayList)
    Supplier<A> initializer();

    // 2. The main logic. Called for each element.
    //    - state: The current state object.
    //    - element: The new element from the stream.
    //    - downstream: A consumer to send output elements to.
    //    Return true to continue, false to short-circuit.
    Integrator<A, T, R> integrator();

    // 3. (Optional) Merges two state objects during parallel processing.
    //    Required for parallel streams to work correctly.
    BinaryOperator<A> combiner();

    // 4. (Optional) Called at the very end to perform final actions,
    //    like flushing a buffer.
    BiConsumer<A, Downstream<? super R>> finisher();
}
```

### Simple `Gatherer.ofSequential()`

For simple, non-parallel gatherers, you can use a static factory method:
`Gatherer.ofSequential(initializer, integrator, finisher)`

### Example: Grouping Consecutive Duplicates

Let's create a gatherer that groups consecutive identical elements.
`Stream(1, 1, 2, 3, 3, 3, 1)` ► `Stream([1, 1], [2], [3, 3, 3], [1])`

```java
// We need a state to hold the current group
class GroupingState<T> {
    List<T> currentGroup = new ArrayList<>();
}

public static <T> Gatherer<T, ?, List<T>> consecutiveDuplicates() {

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

// --- How to use it ---
Stream.

of(1,1,2,3,3,3,1,2,2)
    .

gather(consecutiveDuplicates())
        .

forEach(System.out::println);

// Output:
// [1, 1]
// [2]
// [3, 3, 3]
// [1]
// [2, 2]
```
