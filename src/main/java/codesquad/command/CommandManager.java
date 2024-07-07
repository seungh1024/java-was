package codesquad.command;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import codesquad.command.domainResponse.DomainResponse;
import codesquad.command.methodannotation.Command;
import codesquad.command.methodannotation.GetMapping;
import codesquad.exception.CustomException;
import codesquad.exception.client.ClientErrorCode;
import codesquad.http.HttpStatus;
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

	public DomainResponse execute(HttpMethod httpMethod, String path, String resources) {
		Method method = null;
		switch(httpMethod) {
			case GET -> method = findGetMethod(path);
		}

		if (method != null) {
			try {
				String className = method.getDeclaringClass().getName();
				Object instance = findInstance(className);

				if (instance == null) {
					throw new ClassNotFoundException();
				}
				System.out.println("className = " + className);
				System.out.println("instance = " + instance);
				Object responseBody = method.invoke(instance, resources);
				System.out.println("!");
				Class<?> returnType = method.getReturnType();
				HttpStatus httpStatus = method.getAnnotation(GetMapping.class).httpStatus();

				return new DomainResponse(httpStatus, Objects.equals(returnType, Void.TYPE) ? false : true, returnType,
					responseBody);

			} catch (InvocationTargetException exception) {
				System.out.println(exception.getCause());
				Exception cause = (Exception)exception.getCause();
				if (CustomException.class.isInstance(cause)) {
					throw (CustomException) cause;
				}
				// String exceptionName = exception.getCause().getClass();

				throw new RuntimeException(exception);
			} catch (IllegalAccessException exception) {
				throw new RuntimeException(exception);
			} catch (ClassNotFoundException exception) {
				throw new RuntimeException(exception);
			}
		}

		// 핸들링할 수 있는 메소드가 없으니 요청 경로가 잘못된 것
		throw ClientErrorCode.NOT_FOUND.exception();
	}

	private Method findGetMethod(String path) {
		return getMethod.get(path);
	}

	private Object findInstance(String className) {
		return classInfo.get(className);
	}
}
