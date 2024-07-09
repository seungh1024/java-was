package codesquad.http.request.dynamichandler;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import codesquad.command.domainResponse.DomainResponse;
import codesquad.http.HttpStatus;
import codesquad.util.FileExtension;

public record DynamicHandleResult(
	HttpStatus httpStatus,
	Map<String,String> headers,
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

		for (Map.Entry<String, String> entry : domainResponse.cookie().entrySet()) {
			domainResponse.headers().put("Set-Cookie", entry.getKey()+"="+entry.getValue()+ "; Path=/");
		}

		return new DynamicHandleResult(httpStatus, domainResponse.headers(), hasBody, fileExtension, body);
	}
}
