package codesquad.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import codesquad.command.annotation.redirect.Redirect;
import codesquad.command.domainResponse.DomainResponse;
import codesquad.command.annotation.method.Command;
import codesquad.command.annotation.method.GetMapping;
import codesquad.command.annotation.method.PostMapping;
import codesquad.exception.CustomException;
import codesquad.exception.client.ClientErrorCode;
import codesquad.http.HttpStatus;
import codesquad.http.request.format.HttpMethod;

public class CommandManager {
	private static final CommandManager commandManager = new CommandManager();
	private static Map<String, Method> getMethod = new HashMap<>();
	private static Map<String, Method> postMethod = new HashMap<>();
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
					getInstanceMethod.setAccessible(false);
				} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
					throw new RuntimeException(exception);
				}
			}
			if (clazz.isAnnotationPresent(Command.class)) {
				for (Method method : clazz.getDeclaredMethods()) {
					if (method.isAnnotationPresent(GetMapping.class)) {
						GetMapping get = method.getAnnotation(GetMapping.class);
						getMethod.put(get.path(), method);
					} else if (method.isAnnotationPresent(PostMapping.class)) {
						PostMapping post = method.getAnnotation(PostMapping.class);
						postMethod.put(post.path(), method);
					}
				}
			}
		}
	}

	public DomainResponse execute(HttpMethod httpMethod, String path, String resources) {
		Method method = null;
		switch(httpMethod) {
			case GET -> method = findGetMethod(path);
			case POST -> method = findPostMethod(path);
			default -> throw ClientErrorCode.METHOD_NOT_ALLOWED.exception();
		}



		if (method != null) {
			try {
				String className = method.getDeclaringClass().getName();
				Object instance = findInstance(className);

				if (instance == null) {
					throw new ClassNotFoundException();
				}
				Object responseBody = method.invoke(instance, resources);
				Class<?> returnType = method.getReturnType();
				HttpStatus httpStatus = null;
				switch (httpMethod) {
					case GET -> httpStatus = method.getAnnotation(GetMapping.class).httpStatus();
					case POST -> httpStatus = method.getAnnotation(PostMapping.class).httpStatus();
					default -> throw ClientErrorCode.METHOD_NOT_ALLOWED.exception();
				}


				var headers = new HashMap<String,String>();

				if (isRedirect(method)) {
					Redirect annotation = method.getAnnotation(Redirect.class);
					httpStatus = annotation.httpStatus();
					headers.put("Location", annotation.redirection());
				}


				return new DomainResponse(httpStatus, headers, Objects.equals(returnType, Void.TYPE) ? false : true, returnType,
						responseBody);

			} catch (InvocationTargetException exception) {
				Exception cause = (Exception)exception.getCause();
				if (CustomException.class.isInstance(cause)) {
					throw (CustomException) cause;
				}

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


	private boolean isRedirect(Method method) {
		boolean result = false;
		if (method.isAnnotationPresent(Redirect.class)) {
			result = true;
		}

		return result;
	}

	private Method findPostMethod(String path) {
		return postMethod.get(path);
	}


	private Method findGetMethod(String path) {
		return getMethod.get(path);
	}

	private Object findInstance(String className) {
		return classInfo.get(className);
	}
}
