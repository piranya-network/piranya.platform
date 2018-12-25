package network.piranya.platform.api.utilities;

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
import network.piranya.platform.api.lang.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CollectionUtils {
	
	public <T, U> List<U> map(Collection<T> input, Function<T, U> mapper) {
		return input.stream().map(mapper).collect(Collectors.toList());
	}
	
	public <T, U> List<U> flatMap(Collection<T> input, Function<T, Optional<U>> mapper) {
		return input.stream().map(mapper).filter(i -> i.isPresent()).map(i -> i.get()).collect(Collectors.toList());
	}
	
	public <T> void foreach(Collection<T> input, Consumer<T> action) {
		input.stream().forEach(action);
	}
	
	public <T> void foreachIgnoreExceptions(Collection<T> input, ConsumerWithException<T> action) {
		Consumer<T> handler = item -> {
			try { action.accept(item); }
			catch (Throwable ex) { }
		};
		
		input.stream().forEach(handler);
	}
	
	public <T> List<T> sort(Collection<T> input) {
		return input.stream().sorted().collect(Collectors.toList());
	}
	
	public <T> List<T> sort(Collection<T> input, Comparator<T> comparator) {
		return input.stream().sorted(comparator).collect(Collectors.toList());
	}
	
	public <T, U extends Comparable<U>> List<T> sortBy(Collection<T> input, Function<T, U> extractor) {
		return input.stream().sorted((i, j) -> extractor.apply(i).compareTo(extractor.apply(j))).collect(Collectors.toList());
	}
	
	public <T> T head(Collection<T> input) {
		return input.iterator().next();
	}
	
	public <T> Optional<T> oHead(Collection<T> input) {
		Iterator<T> iterator = input.iterator();
		return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.empty();
	}
	
	public <T> List<T> filter(Collection<T> input, Predicate<T> predicate) {
		return input.stream().filter(predicate).collect(Collectors.toList());
	}
	
	public <T> Optional<T> find(Collection<T> input, Predicate<T> predicate) {
		return Optional.from(input.stream().filter(predicate).findFirst());
	}
	
	public <T> int indexOf(Collection<T> input, Predicate<T> predicate) {
		int index = 0;
		for (T i : input) {
			if (predicate.test(i)) {
				return index;
			} else {
				++index;
			}
		}
		return -1;
	}
	
	public <T> List<T> add(List<T> input, T newItem) {
		List<T> newList = new ArrayList<>(input);
		newList.add(newItem);
		return newList;
	}
	
	public <T, U> U summarize(Collection<T> input, Function<T, U> map, U defaultValue, BiFunction<U, U, U> agg) {
		return input.stream().map(map).reduce(defaultValue, (s, i) -> agg.apply(s, i));
	}
	
	public <K, V> V getValue(Map<K, V> map, K key, Supplier<V> defaultValue, boolean addIfNotExists) {
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
	public final <T> List<T> list(T... items) {
		return new ArrayList<>(Arrays.asList(items));
	}
	
	@SafeVarargs
	public final <T> Set<T> set(T... items) {
		return new HashSet<>(Arrays.asList(items));
	}
	
	public <T> SortedSet<T> sortedSet() {
		return new TreeSet<>();
	}
	
	public <T> SortedSet<T> sortedSet(Comparator<T> comparator) {
		return new TreeSet<>(comparator);
	}
	
	public int difference(int... diffs) {
		for (int diff : diffs) {
			if (diff != 0) {
				return diff;
			}
		}
		return 0;
	}
	
	public <T> List<T> asList(Collection<T> input) {
		return new ArrayList<>(input);
	}
	
	public <T> List<T> subtract(Collection<T> source, Collection<T> target) {
		return filter(source, i -> !target.contains(i));
	}
	
	public <T> List<T> concat(Collection<T> source1, Collection<T> source2) {
		List<T> result = new ArrayList<>(source1.size() + source2.size());
		result.addAll(source1);
		result.addAll(source2);
		return result;
	}
	
	@SafeVarargs
	public final <T> List<T> concat(Collection<T>... inputs) {
		List<T> result = new ArrayList<>();
		for (Collection<T> input : inputs) {
			result.addAll(input);
		}
		return result;
	}
	
	public <K, V> Map<K, List<V>> groupBy(List<V> input, Function<V, K> keyExtractor) {
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
	
	public <T, U extends Comparable<U>> Optional<T> max(List<T> input, Function<T, U> propertyExtractor) {
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
	
	public <T> List<T> reverse(Collection<T> input) {
		List<T> result = new ArrayList<>(input);
		Collections.reverse(result);
		return result;
	}
	
	
	public <K, V> Map<K, V> asMap(Collection<V> input, Function<V, K> keyExtractor) {
		Map<K, V> result = new HashMap<>();
		for (V value : input) {
			result.put(keyExtractor.apply(value), value);
		}
		return result;
	}
	
	public <K, V> Map<K, V> map() {
		return new HashMap<>();
	}
	
	public <T> Set<T> toSet(T[] items) {
		HashSet<T> set = new HashSet<>();
		for (T item : items) {
			set.add(item);
		}
		return set;
	}
	
	public <T> List<T> toList(T[] items) {
		ArrayList<T> set = new ArrayList<>();
		for (T item : items) {
			set.add(item);
		}
		return set;
	}
	
	
	public CollectionUtils() { }
	
	
	public static interface ConsumerWithException<T> {
		
		void accept(T t) throws Exception;
		
	}
	
}
