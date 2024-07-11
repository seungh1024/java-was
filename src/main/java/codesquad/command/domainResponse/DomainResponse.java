package codesquad.command.domainResponse;

import codesquad.http.HttpStatus;

import java.util.List;
import java.util.Map;

public record DomainResponse(
	HttpStatus httpStatus,
	Map<String,String> headers,
	Map<String,String> cookie,
	Map<String, List<String>> cookieOptions,
	boolean hasBody,
	Class<?> classType,
	Object returnValue
) {
}
