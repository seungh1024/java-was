package codesquad.command.domainResponse;

import codesquad.http.HttpStatus;

import java.util.Map;

public record DomainResponse(
	HttpStatus httpStatus,
	Map<String,String> headers,
	boolean hasBody,
	Class<?> classType,
	Object returnValue
) {
}
