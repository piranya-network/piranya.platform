package network.piranya.platform.node.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class StringUtils {
	
	public static boolean hasText(String text) {
		return text != null && text.trim().length() > 0;
	}
	
	public static boolean isEmpty(String text) {
		return !hasText(text);
	}
	
	public static String[] splitString(String text, char separator) {
		String[] parts = text.split("[" + separator + "]");
		List<String> result = new ArrayList<String>(parts.length);
		for (String part : parts) {
			result.add(part.trim());
		}
		return result.toArray(new String[0]);
	}
	
	public static String concatStringArray(String[] items, String separator, boolean addWhiteSpace) {
		StringBuffer result = new StringBuffer();
		
		for (int i = 0; i < items.length; i++) {
			result.append(items[i]);
			if (i < items.length - 1) {
				result.append(separator);
				if (addWhiteSpace) {
					result.append(' ');
				}
			}
		}
		
		return result.toString();
	}
	
	public static String concatStringCollection(Collection<String> items, String separator, boolean addWhiteSpace) {
		StringBuffer result = new StringBuffer();
		
		for (String item : items) {
			result.append(item);
			result.append(separator);
			if (addWhiteSpace) {
				result.append(' ');
			}
		}
		
		if (items.size() > 0) {
			return addWhiteSpace
					? result.substring(0, result.length() - 1 - separator.length())
					: result.substring(0, result.length() - separator.length());
		} else {
			return result.toString();
		}
	}
	
	public static void replace(StringBuffer sb, String pattern, String replacement) {
		final int len = pattern.length();
		int found = -1;
		int start = 0;
		
		while((found = sb.indexOf(pattern, start)) != -1) {
			sb.replace(found, found + len, replacement);
		}
    }
	
	public static String replace(String source, String pattern, String replacement) {
		if (source != null) {
			final int len = pattern.length();
			StringBuffer sb = new StringBuffer();
			int found = -1;
			int start = 0;
			
			while( (found = source.indexOf(pattern, start) ) != -1) {
				sb.append(source.substring(start, found));
				sb.append(replacement);
				start = found + len;
			}
			
			sb.append(source.substring(start));
			return sb.toString();
		} else {
			return null;
		}
    }
	
	public static String[] convertToStringArray(Object[] objects) {
		String[] result = new String[objects.length];
		for (int i = 0; i < objects.length; i++) {
			result[i] = objects[i] != null ? objects[i].toString() : null;
		}
		return result;
	}
	
	public static String splitToCamelCase(String str, String separator) {
		return str.replaceAll(String.format("%s|%s|%s",
				"(?<=[A-Z])(?=[A-Z][a-z])",
				"(?<=[^A-Z])(?=[A-Z])",
				"(?<=[A-Za-z])(?=[^A-Za-z])"
				), separator);
	}

	
	
	private StringUtils() { }
	
}
