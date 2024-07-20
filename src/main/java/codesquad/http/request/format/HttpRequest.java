package codesquad.http.request.format;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import codesquad.session.Cookie;
import codesquad.util.FileExtension;

public record HttpRequest(
    HttpMethod method,
    String uri,
	FileExtension fileExtension,
    String httpVersion,
    Map<String, String> headers,
	Map<String, Cookie> cookie,
    String body,
	byte[] buffer,
	int bufferIndex,
	InputStream inputStream,
	OutputStream outputStream
) {
}
