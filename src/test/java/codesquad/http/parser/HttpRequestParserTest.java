package codesquad.http.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import codesquad.http.request.format.HttpRequest;
import codesquad.util.FileExtension;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestParserTest {

	String parsingData = "GET /index.html HTTP/1.1\n"
		+ "Host: localhost:8080\n"
		+ "Connection: keep-alive\n"
		+ "Accept: image/webp,image/apng,image/*,*/*;q=0.8\n"
		+ "Cookie: test=zzz; test=zzz; sessionKey=298deaad-db51-47d7-8fff-7e9dc1183fd9\n\n"
		+ "body";

	@Nested
	@DisplayName("HTTP 요청 파싱 테스트")
	class ParsingTest{
		@Test
		@DisplayName("HTTP 파싱")
		void parsing_http_request() throws IOException {
			// given
			var socket = new Socket();
			var httpRequestParser = new HttpRequestParser(socket);

			// when
			var httpRequest = httpRequestParser.getHttpRequest(parsingData,0,null,0,null);

			// then
			assertEquals("HTTP/1.1",httpRequest.httpVersion());
			assertEquals("body",httpRequest.body());
			assertEquals(FileExtension.DYNAMIC, httpRequest.fileExtension());
		}
	}
}