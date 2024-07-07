package codesquad.http.request.dynamichandler;

import codesquad.http.HttpStatus;
import codesquad.util.FileExtension;

public record DynamicHandleResult(
	HttpStatus httpStatus,
	FileExtension fileExtension,
	byte[] body
) {

}
