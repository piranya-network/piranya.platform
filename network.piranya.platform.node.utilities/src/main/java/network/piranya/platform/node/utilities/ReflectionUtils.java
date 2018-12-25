package network.piranya.platform.node.utilities;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import network.piranya.platform.api.lang.Optional;

public abstract class ReflectionUtils {
	
	public static Optional<Method> findMethod(Class<?> objectType, Class<? extends Annotation> annotationType, Class<?>... parametersTypes) {
		for (Method method : objectType.getDeclaredMethods()) {
			if (method.isAnnotationPresent(annotationType)) {
				if (parametersTypes.length == method.getParameterTypes().length) {
					boolean isIdentical = true;
					for (int i = 0; i < parametersTypes.length; i++) {
						if (!parametersTypes[i].equals(method.getParameterTypes()[i])) {
							isIdentical = false;
						}
					}
					if (isIdentical) {
						return Optional.of(method);
					}
				}
			}
		}
		return Optional.empty();
	}
	
	public static Optional<Method> findMethod(Class<?> objectType, String methodName) {
		for (Method method : objectType.getDeclaredMethods()) {
			if (method.getName().equals(methodName)) {
				return Optional.of(method);
			}
		}
		return Optional.empty();
	}
	
	public static List<Method> findMethodsByAnnotation(Class<?> objectType, Class<? extends Annotation> annotationType) {
		List<Method> result = new ArrayList<>();
		for (Method method : objectType.getDeclaredMethods()) {
			if (method.isAnnotationPresent(annotationType) && method.getParameterTypes().length > 0) {
				result.add(method);
			}
		}
		return result;
	}
	
	public static Object invoke(Object instance, Method method, Object... args) {
		try {
			return method.invoke(instance, args);
		} catch (Throwable ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static <T> T createInstance(Class<T> clazz) {
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (Throwable ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static <T> void inject(T instance, Class<T> declaringType, String fieldName, Object value) {
		try {
			for (Field field : declaringType.getDeclaredFields()) {
				if (field.getName().equals(fieldName)) {
					field.setAccessible(true);
					field.set(instance, value);
					return;
				}
			}
			throw new RuntimeException(String.format("Field '%s' was not found", fieldName));
		} catch (Throwable ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static <T> Object getFieldValue(T instance, Class<T> declaringType, String fieldName) {
		try {
			for (Field field : declaringType.getDeclaredFields()) {
				if (field.getName().equals(fieldName)) {
					field.setAccessible(true);
					return field.get(instance);
				}
			}
			throw new RuntimeException(String.format("Field '%s' was not found", fieldName));
		} catch (Throwable ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T, Result> Result getFieldValue(T instance, Class<T> declaringType, String fieldName, Class<Result> resultType) {
		return (Result)getFieldValue(instance, declaringType, fieldName);
	}
	
	public static Class<?> getGenericParameterType(Method method, int paramIndex) {
		Type[] genericParameterTypes = method.getGenericParameterTypes();
		Type[] parameters = ((ParameterizedType)genericParameterTypes[paramIndex]).getActualTypeArguments();
		return (Class<?>)parameters[0];
	}
	
	
	private ReflectionUtils() { }
	
}
