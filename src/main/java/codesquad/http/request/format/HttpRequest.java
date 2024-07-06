package codesquad.http.request.format;

import java.util.Map;

import codesquad.util.FileExtension;

public record HttpRequest(
    HttpMethod method,
    String uri,
	FileExtension fileExtension,
    String httpVersion,
    Map<String, String> headers,
    String body

) {
}
