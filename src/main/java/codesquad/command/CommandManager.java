package codesquad.command;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import codesquad.command.annotation.RequestParam;
import codesquad.command.annotation.redirect.Redirect;
import codesquad.command.domainResponse.DomainResponse;
import codesquad.command.annotation.method.Command;
import codesquad.command.annotation.method.GetMapping;
import codesquad.command.annotation.method.PostMapping;
import codesquad.command.domainResponse.HttpClientRequest;
import codesquad.command.domainResponse.HttpClientResponse;
import codesquad.exception.CustomException;
import codesquad.exception.client.ClientErrorCode;
import codesquad.exception.server.ServerErrorCode;
import codesquad.http.HttpStatus;
import codesquad.http.request.format.HttpMethod;
import codesquad.http.request.format.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.MacSpi;

import static codesquad.util.StringSeparator.EQUAL_SEPARATOR;
import static codesquad.util.StringSeparator.QUERY_PARAMETER_SEPARATOR;

public class CommandManager {
	private static final Logger log = LoggerFactory.getLogger(CommandManager.class);

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

	public DomainResponse execute(HttpRequest httpRequest) {
		log.debug("[Executing] {}", httpRequest);
		var httpMethod = httpRequest.method();
		var path = httpRequest.uri();
		var resources = httpRequest.body();

		Method method = null;
		switch(httpMethod) {
			case GET -> method = findGetMethod(path);
			case POST -> method = findPostMethod(path);
			default -> throw ClientErrorCode.METHOD_NOT_ALLOWED.exception();
		}



		if (method != null) {
			log.info("[Execute] find method success");
			try {
				var className = method.getDeclaringClass().getName();
				var instance = findInstance(className);
				System.out.println("instance = "+instance);

				var userInputData = parsingQueryParameterResources(resources);
				HttpClientRequest httpClientRequest = new HttpClientRequest(httpRequest);
				System.out.println(httpRequest.headers());
				HttpClientResponse httpClientResponse = new HttpClientResponse();
				var parameters = makeParameterArgs(method, userInputData, httpClientRequest, httpClientResponse);

				System.out.println("parameters = "+ Arrays.toString(parameters));
				var responseBody = method.invoke(instance, parameters);
				System.out.println("responseBody = "+responseBody);

				var returnType = method.getReturnType();

				HttpStatus httpStatus = null;

				if (instance == null) {
					throw new ClassNotFoundException();
				}
				switch (httpMethod) {
					case GET -> httpStatus = method.getAnnotation(GetMapping.class).httpStatus();
					case POST -> httpStatus = method.getAnnotation(PostMapping.class).httpStatus();
					default -> throw ClientErrorCode.METHOD_NOT_ALLOWED.exception();
				}



				if (isRedirect(method)) {
					Redirect annotation = method.getAnnotation(Redirect.class);
					httpStatus = annotation.httpStatus();
					httpClientResponse.setHeader("Location", annotation.redirection());
				}


				return new DomainResponse(httpStatus, httpClientResponse.getHeaders(), httpClientResponse.getCookie(),httpClientResponse.getCookieOptions(), Objects.equals(returnType, Void.TYPE) ? false : true, returnType,
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

	/**
	 * 파라미터 배열을 만들어주는 메소드. invoke() 호출 시 넘겨준다.
	 *
	 * @param method
	 * @param resources
	 * @return
	 */
	private Object[] makeParameterArgs(Method method, Map<String,String> resources, HttpClientRequest httpClientRequest, HttpClientResponse httpClientResponse) {
		var parameters = method.getParameters();

		// 파라미터의 수가 더 작아야 한다. HttpClientResponse 객체가 넘어갈 수 있기 때문
		if (parameters.length < resources.size()) {
			throw ClientErrorCode.PARAMETER_FORMAT_EXCEPTION.exception();
		}

		var result = new Object[parameters.length];
		var idx = 0;
		for (Parameter parameter : parameters) {
			var parameterType = parameter.getType();
			var requestParamAnnotation = parameter.getAnnotation(RequestParam.class);
			if (Objects.isNull(requestParamAnnotation) && !(Objects.equals(parameterType, HttpClientResponse.class) || (Objects.equals(parameterType, HttpClientRequest.class)))) {
				log.error("[Server Error] Domain의 파라미터와 입력 값의 파라미터 형식이 일치하지 않습니다.");
				throw ServerErrorCode.INTERNAL_SERVER_ERROR.exception();
			}

			if (Objects.equals(parameterType, HttpClientResponse.class)) {
				result[idx++] = httpClientResponse;
			} else if(Objects.equals(parameterType, HttpClientRequest.class)){
				result[idx++] = httpClientRequest;
			}else if (!Objects.isNull(resources.get(requestParamAnnotation.name()))) {
				setParameterArgs(result, idx++, parameterType, resources.get(requestParamAnnotation.name()));
			} else {
				throw ClientErrorCode.PARAMETER_FORMAT_EXCEPTION.exception();
			}
		}

		return result;
	}

	/**
	 * 호출할 메소드의 파라미터 타입에 맞게 저장해주는 메소드
	 * @param args invoke()할 파라미터 배열
	 * @param idx 현재 파라미터 배열의 인덱스
	 * @param type 파라미터 타입
	 * @param value 넣어줄 값
	 */
	private void setParameterArgs(Object[] args, int idx, Class type, String value) {
		if (type == String.class) {
			args[idx] = value;
		} else if (type == int.class || type == Integer.class) {
			args[idx] = Integer.parseInt(value);
		} else if (type == boolean.class || type == Boolean.class) {
			args[idx] = Boolean.parseBoolean(value);
		} else if (type == long.class || type == Long.class) {
			args[idx] = Long.parseLong(value);
		} else if (type == double.class || type == Double.class) {
			args[idx] = Double.parseDouble(value);
		} else if (type == float.class || type == Float.class) {
			args[idx] = Float.parseFloat(value);
		} else if (type == short.class || type == Short.class) {
			args[idx] = Short.parseShort(value);
		} else if (type == byte.class || type == Byte.class) {
			args[idx] = Byte.parseByte(value);
		} else {
			// 기본형이 아닌 경우 문자열 처리
			args[idx] = value;
		}
	}

	/**
	 * queryParameter 형태로 들어온 값을 파싱하는 메소드
	 * @param resources
	 * @return
	 */
	private Map<String, String> parsingQueryParameterResources(String resources){
		Map<String, String> map = new HashMap<>();

		if (resources.isEmpty()) {
			return map;
		}

		var userData = resources.split(QUERY_PARAMETER_SEPARATOR);
		for (String keyValue : userData) {
			var data = keyValue.split(EQUAL_SEPARATOR);
			try {
				map.put(data[0], URLDecoder.decode(data[1], "UTF-8"));
			} catch (UnsupportedEncodingException exception) {
				throw new RuntimeException(exception);
			}
		}

		System.out.println("parsingQueryParameterResources: "+map);
		return map;
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
