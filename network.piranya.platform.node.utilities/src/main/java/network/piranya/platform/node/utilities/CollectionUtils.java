package network.piranya.platform.node.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import network.piranya.platform.api.lang.Optional;

public final class CollectionUtils {
	
	public static <T, U> List<U> map(Collection<T> input, Function<T, U> mapper) {
		return input.stream().map(mapper).collect(Collectors.toList());
	}
	
	public static <T, U> List<U> flatMap(Collection<T> input, Function<T, Optional<U>> mapper) {
		return input.stream().map(mapper).filter(i -> i.isPresent()).map(i -> i.get()).collect(Collectors.toList());
	}
	
	public static <T> List<T> limit(Collection<T> input, int maxSize) {
		List<T> result = new ArrayList<>();
		int count = 0;
		for (T i : input) {
			result.add(i);
			if (++count >= maxSize) {
				break;
			}
		}
		return result;
	}
	
	public static <T> void foreach(Collection<T> input, Consumer<T> action) {
		input.stream().forEach(action);
	}
	
	public static <T> void foreachIgnoreExceptions(Collection<T> input, Consumer<T> action) {
		Consumer<T> handler = item -> {
			try { action.accept(item); }
			catch (Throwable ex) { }
		};
		
		input.stream().forEach(handler);
	}
	
	public static <T> List<T> sort(Collection<T> input) {
		return input.stream().sorted().collect(Collectors.toList());
	}
	
	public static <T> List<T> sort(Collection<T> input, Comparator<T> comparator) {
		return input.stream().sorted(comparator).collect(Collectors.toList());
	}
	
	public static <T, U extends Comparable<U>> List<T> sortBy(Collection<T> input, Function<T, U> extractor) {
		return input.stream().sorted((i, j) -> extractor.apply(i).compareTo(extractor.apply(j))).collect(Collectors.toList());
	}
	
	public static <T> T head(Collection<T> input) {
		return input.iterator().next();
	}
	
	public static <T> Optional<T> oHead(Collection<T> input) {
		Iterator<T> iterator = input.iterator();
		return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.empty();
	}
	
	public static <T> List<T> filter(Collection<T> input, Predicate<T> predicate) {
		return input.stream().filter(predicate).collect(Collectors.toList());
	}
	
	public static <T> Optional<T> find(Collection<T> input, Predicate<T> predicate) {
		return Optional.from(input.stream().filter(predicate).findFirst());
	}
	
	public static <T> List<T> add(List<T> input, T newItem) {
		List<T> newList = new ArrayList<>(input);
		newList.add(newItem);
		return newList;
	}
	
	public static <T, U> U summarize(Collection<T> input, Function<T, U> map, U defaultValue, BiFunction<U, U, U> agg) {
		return input.stream().map(map).reduce(defaultValue, (s, i) -> agg.apply(s, i));
	}
	
	public static <K, V> V getValue(Map<K, V> map, K key, Supplier<V> defaultValue, boolean addIfNotExists) {
		V value = map.get(key);
		if (value == null) {
			value = defaultValue.get();
			if (addIfNotExists) {
				map.put(key, value);
			}
		}
		return value;
	}
	
	@SafeVarargs
	public static <T> List<T> list(T... items) {
		return new ArrayList<>(Arrays.asList(items));
	}
	
	@SafeVarargs
	public static <T> Set<T> set(T... items) {
		return new HashSet<>(Arrays.asList(items));
	}
	
	public static <T> Set<T> set(Collection<T> items) {
		return new HashSet<>(items);
	}
	
	public static int difference(int... diffs) {
		for (int diff : diffs) {
			if (diff != 0) {
				return diff;
			}
		}
		return 0;
	}
	
	public static <T> List<T> asList(Collection<T> input) {
		return new ArrayList<>(input);
	}
	
	public static <T> List<T> subtract(Collection<T> source, Collection<T> target) {
		return filter(source, i -> !target.contains(i));
	}
	
	public static <T> List<T> concat(Collection<T> source1, Collection<T> source2) {
		List<T> result = new ArrayList<>(source1.size() + source2.size());
		result.addAll(source1);
		result.addAll(source2);
		return result;
	}
	
	@SafeVarargs
	public static <T> List<T> concat(Collection<T>... inputs) {
		List<T> result = new ArrayList<>();
		for (Collection<T> input : inputs) {
			result.addAll(input);
		}
		return result;
	}
	
	public static <K, V> Map<K, List<V>> groupBy(List<V> input, Function<V, K> keyExtractor) {
		Map<K, List<V>> result = new HashMap<>();
		for (V i : input) {
			K k = keyExtractor.apply(i);
			List<V> list = result.get(k);
			if (list == null) {
				list = new ArrayList<>();
				result.put(k, list);
			}
			list.add(i);
		}
		return result;
	}
	
	public static <T, U extends Comparable<U>> Optional<T> max(List<T> input, Function<T, U> propertyExtractor) {
		T entry = null;
		U max = null;
		for (T i : input) {
			U value = propertyExtractor.apply(i);
			if (max == null || value.compareTo(max) > 0) {
				entry = i;
			}
		}
		return entry != null ? Optional.of(entry) : Optional.empty();
	}
	
	public static <T> List<T> reverse(Collection<T> input) {
		List<T> result = new ArrayList<>(input);
		Collections.reverse(result);
		return result;
	}
	
	public static <T> Set<T> toSet(T[] items) {
		HashSet<T> set = new HashSet<>();
		for (T item : items) {
			set.add(item);
		}
		return set;
	}
	
	public static <T> List<T> toList(T[] items) {
		ArrayList<T> set = new ArrayList<>();
		for (T item : items) {
			set.add(item);
		}
		return set;
	}
	
	
	private CollectionUtils() { }
	
}
