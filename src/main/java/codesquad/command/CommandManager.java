package codesquad.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import codesquad.command.methodannotation.Command;
import codesquad.command.methodannotation.GetMapping;
import codesquad.http.request.format.HttpMethod;
import codesquad.http.request.format.HttpRequest;

public class CommandManager {
	private static final CommandManager commandManager = new CommandManager();
	private static Map<String, Method> getMethod = new HashMap<>();
	private static Map<String, Object> classInfo = new HashMap<>();

	private CommandManager(){}

	public static CommandManager getInstance() {
		return commandManager;
	}

	public void initMethod(Class<?>... classes) {
		for (Class<?> clazz : classes) {
			if (clazz.isAnnotationPresent(Command.class)) {
				try {
					Method getInstanceMethod = clazz.getDeclaredMethod("getInstance");
					getInstanceMethod.setAccessible(true);
					Object classInstance = getInstanceMethod.invoke(null);
					classInfo.put(clazz.getName(), classInstance);
				} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
					throw new RuntimeException(exception);
				}
			}
			if (clazz.isAnnotationPresent(Command.class)) {
				for (Method method : clazz.getDeclaredMethods()) {
					if (method.isAnnotationPresent(GetMapping.class)) {
						GetMapping get = method.getAnnotation(GetMapping.class);
						getMethod.put(get.path(), method);
					}
				}
			}
		}
	}

	public void execute(HttpMethod httpMethod, String path) {
		Method method = null;
		switch(httpMethod) {
			case GET -> method = findGetMethod(path);
		}

		if (method != null) {
			System.out.println("path = "+path);
			System.out.println("method not null");
			try {
				System.out.println("classInfo = "+classInfo);
				String className = method.getDeclaringClass().getName();
				Object instance = findInstance(className);
				System.out.println("className = "+className);
				System.out.println("instance = "+instance);
				if (instance == null) {
					throw new ClassNotFoundException();
				}
				System.out.println("instance getclass = "+instance.getClass());
				method.invoke(instance);
				System.out.println("invoke success");
			} catch (InvocationTargetException exception) {
				throw new RuntimeException(exception);
			} catch (IllegalAccessException exception) {
				throw new RuntimeException(exception);
			} catch (ClassNotFoundException exception) {
				throw new RuntimeException(exception);
			}
		}
	}

	private Method findGetMethod(String path) {
		System.out.println(getMethod);
		return getMethod.get(path);
	}

	private Object findInstance(String className) {
		return classInfo.get(className);
	}
}
