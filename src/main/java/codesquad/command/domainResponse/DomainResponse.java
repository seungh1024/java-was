package codesquad.command.domainResponse;

import codesquad.http.HttpStatus;

import java.util.Map;

public record DomainResponse(
	HttpStatus httpStatus,
	Map<String,String> headers,
	Map<String,String> cookie,
	boolean hasBody,
	Class<?> classType,
	Object returnValue
) {

	public void setCookie(String key, String returnValue) {
		cookie.put(key, returnValue);
	}
}
