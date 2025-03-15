package pl.zajacp.playground.gatheres;

import java.util.stream.Gatherer;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

// https://dev.java/learn/api/streams/gatherers/

@SuppressWarnings("preview")
public class GatherersPractice {
    public static void main(String[] args) {
        var fold = Gatherers.scan(
                () -> "{",
                (string, element) -> {
                    System.out.println("string = " + string + ", element = " + element);
                    return string + element;
                });


        var finish = Gatherer.<String, String>ofSequential(
                (_, element, downstream) -> {
                    System.out.println("element = " + element);
                    element += "}";
                    if (element.length() > 4) return true;
                    return downstream.push(element);
                });


        var result = Stream.of(1, 2, 3, 4)
                .map(Object::toString)
                .gather(fold.andThen(finish))
                .toList();
        System.out.println("result = " + result);
    }
}
