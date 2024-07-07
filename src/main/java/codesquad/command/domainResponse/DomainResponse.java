package codesquad.command.domainResponse;

import codesquad.http.HttpStatus;

public record DomainResponse(
	HttpStatus httpStatus,
	boolean hasBody,
	Class<?> classType,
	Object returnValue
) {
}
