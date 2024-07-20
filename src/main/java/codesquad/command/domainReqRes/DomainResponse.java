package codesquad.command.domainReqRes;

import codesquad.http.HttpStatus;


public record DomainResponse(
	HttpStatus httpStatus,
	HttpClientResponse httpClientResponse,
	boolean hasBody,
	Class<?> classType,
	Object returnValue
) {
}
