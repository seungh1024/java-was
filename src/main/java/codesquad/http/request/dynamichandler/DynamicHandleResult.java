package codesquad.http.request.dynamichandler;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import codesquad.command.domainResponse.DomainResponse;
import codesquad.http.HttpStatus;
import codesquad.util.FileExtension;
import codesquad.util.StringSeparator;

import static codesquad.util.StringSeparator.*;

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

		var cookieOptions = domainResponse.cookieOptions();
		for (Map.Entry<String, String> entry : domainResponse.cookie().entrySet()) {
			var key = entry.getKey();
			var value =entry.getValue();
			var cookieValue = new StringBuilder(key).append(EQUAL_SEPARATOR).append(value).append(";");
			var options = cookieOptions.get(key);
			if (!Objects.isNull(options)) {
				for (String option : options) {
					cookieValue.append(SPACE_SEPARATOR).append(option).append(";");
				}
			}

			cookieValue.append(" Path=/");
			domainResponse.headers().put("Set-Cookie", cookieValue.toString());
		}

		return new DynamicHandleResult(httpStatus, domainResponse.headers(), hasBody, fileExtension, body);
	}
}
