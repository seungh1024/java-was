package codesquad.http.parser;

import codesquad.exception.client.ClientErrorCode;
import codesquad.http.request.format.HttpMethod;
import codesquad.http.request.format.HttpRequest;
import codesquad.exception.server.ServerErrorCode;
import codesquad.session.Cookie;
import codesquad.util.FileExtension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.*;

import static codesquad.util.StringSeparator.*;

public class HttpRequestParser {
    private static final Logger log = LoggerFactory.getLogger(HttpRequestParser.class);
    private static final int bufferSize = 8*1024;
    private static final int maxInputSize = 2 * 1024 * 1024; // chrome 2MB 길이로 제한

    private Socket clientSocket;

    public HttpRequestParser(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public HttpRequest parse() {
        var buffer = new byte[bufferSize];
        var readSize = 0;
        var inputSize = 0;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            var inputStream = clientSocket.getInputStream();
            // 소켓 버퍼 데이터를 빨리 가져와야 하니 우선 outputStream으로 복사한다.
            while ((readSize = inputStream.read(buffer, 0, bufferSize)) != -1) {
                inputSize += readSize;
                if (inputSize > maxInputSize) {
                    throw ClientErrorCode.URI_TOO_LONG.exception();
                }

                outputStream.write(buffer, 0, readSize);
                if (readSize < bufferSize) {
                    break;
                }
            }
        } catch (IOException exception) {
            log.error("[Socket Error] : 데이터를 읽어오던 중 에러 발생");
            throw ServerErrorCode.INTERNAL_SERVER_ERROR.exception();
        } catch (Exception exception) {
            log.error("tqtqtqt");
        }

        String htmlString = outputStream.toString();


        return getHttpRequest(htmlString);
    }


    public HttpRequest getHttpRequest(String htmlString) {
        var lines = htmlString.replaceAll(CR, EMPTY_STRING).split(LF);

        var firstLine = lines[0].split(SPACE_SEPARATOR);
        var method = HttpMethod.fromString(firstLine[0]);
        var uri = firstLine[1];
        var fileExtension = getFileExtension(uri);

        if(uri.contains("/index.html")) {
            uri = uri.substring(0,uri.lastIndexOf("/index.html"));
            if (uri.length() == 0) {
                uri = "/";
            }
        }
        var httpVersion = firstLine[2];

        var headers = new HashMap<String,String>();
        var cookies = new HashMap<String, Cookie>();
        var bodyIdx = parsingHeader(lines, headers, cookies);
        var body = getBody(lines, bodyIdx);

        return new HttpRequest(method, uri, fileExtension, httpVersion, headers, cookies, body);
    }

    public FileExtension getFileExtension(String uri) {
        if (uri.contains("?")) {
            return FileExtension.DYNAMIC;
        }

        int lastIndex = uri.lastIndexOf(".");
        var extension = uri.substring(lastIndex+1, uri.length()).toUpperCase();

        return FileExtension.fromString(extension);
    }

    public String getBody(String[] lines, int bodyIdx) {
        StringBuilder sb = new StringBuilder();
        for (; bodyIdx < lines.length; bodyIdx++) {
            sb.append(lines[bodyIdx]);
            if (bodyIdx != lines.length-1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    public int parsingHeader(String[] lines, Map<String, String> headers, Map<String, Cookie> cookies) {
        int idx = 1;
        for (; idx < lines.length; idx++) {
            var headerLine = lines[idx].replaceAll(SPACE_SEPARATOR,EMPTY_STRING);
            if (headerLine.isEmpty()) { // 비어 있다는 것은 라인 구분자가 2개라는 것으로 header 영역이 끝난다.
                idx++;
                break;
            }
            var header = headerLine.split(HEADER_SEPARATOR);
            var headerName = header[0];
            var headerValue = header[1];

            if (Objects.equals(headerName, "Cookie")) {
                var cookieList = headerValue.split(SEMICOLON_SEPARATOR);
                for (String cookieInfo : cookieList) {
                    var cookieKeyValue = cookieInfo.split(EQUAL_SEPARATOR);

                    var key = cookieKeyValue[0];
                    if (Objects.equals(key,"sessionKey")) {
                        var value = cookieKeyValue[1];
                        cookies.put(key, new Cookie(key, value));
                    }
                }
            } else {
                headers.put(headerName, headerValue);
            }
        }

        return idx;
    }


    @Deprecated
    public Map<String, String> parsingHeaderValue(String headerString) {
        var headerValues = new HashMap<String, String>();
        var values = headerString.split(SPACE_SEPARATOR);

        for (var value : values) {
            if (value.isEmpty()) {
                continue;
            }

            if (value.contains(COMMA_DELIMITER)) {
                var commaSplitData = value.split(COMMA_DELIMITER);
                for (String data : commaSplitData) {
                    headerValues.put(data, null);
                }

            } else if (value.contains(EQUAL_SEPARATOR)) {
                var split = value.split(EQUAL_SEPARATOR);
                headerValues.put(split[0], split[1]);
            } else {
                headerValues.put(value, null);
            }
        }
        return headerValues;
    }
}
