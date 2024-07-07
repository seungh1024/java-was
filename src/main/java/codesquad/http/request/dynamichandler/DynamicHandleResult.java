package codesquad.http.request.dynamichandler;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

import codesquad.command.domainResponse.DomainResponse;
import codesquad.http.HttpStatus;
import codesquad.util.FileExtension;

public record DynamicHandleResult(
	HttpStatus httpStatus,
	boolean hasBody,
	FileExtension fileExtension,
	Object body
) {

	public static DynamicHandleResult of(DomainResponse domainResponse) {
		var httpStatus = domainResponse.httpStatus();
		var hasBody = domainResponse.hasBody();
		var body = domainResponse.returnValue();
		FileExtension fileExtension = null;
		if (!Objects.equals(domainResponse.classType(), File.class)) {
			fileExtension = FileExtension.HTML;
		}

		return new DynamicHandleResult(httpStatus, hasBody, fileExtension, body);
	}
}
