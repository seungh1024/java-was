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

import static codesquad.util.StringUtils.*;

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

        StringBuilder sb = new StringBuilder();

        InputStream inputStream = null;
        OutputStream outputStream = null;
        int enterCheck = 0;
        int bufferIndex = 0;
        int endBufferIndex = 0;

        try {
            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();

            int read = 0;

            var bos = new ByteArrayOutputStream();
            // 소켓 버퍼 데이터를 빨리 가져와야 하니 우선 outputStream으로 복사한다.
            while ((read = inputStream.read()) != -1) {
                inputSize += readSize;
                bos.write(read);

                boolean flag = false;

                if (read == 13) {
                    var line = bos.toString();
                    sb.append(line);
                    bos.reset();
                    enterCheck++;
                    if (enterCheck == 2) {
                        break;
                    }
                } else if (read != 10 && read != 13) {
                    enterCheck = 0;
                }

                if(flag){
                    break;
                }
                // sb.append(new String(buffer, 0, readSize));
                // if (readSize < bufferSize) {
                //     break;
                // }
            }
        } catch (IOException exception) {
            log.error("[Socket Error] : 데이터를 읽어오던 중 에러 발생");
            throw ServerErrorCode.INTERNAL_SERVER_ERROR.exception();
        } catch (Exception exception) {
            log.error("[Http Parsing Error]");
        }

        String headerString = sb.toString();

        return getHttpRequest(headerString, inputSize, buffer, bufferIndex, inputStream, outputStream);
    }


    public HttpRequest getHttpRequest(String headerString, int inputSize, byte[] buffer, int bufferIndex, InputStream inputStream, OutputStream outputStream) {
        var lines = headerString.replaceAll(CR, EMPTY_STRING).split(LF);

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

        var contentType = headers.get("Content-Type");
        var contentLength = headers.get("Content-Length");

        var body = "";

        if (Objects.nonNull(contentType) && contentType.contains(BOUNDARY)) {
            var split = contentType.split(BOUNDARY);
            headers.put("multipart", "--"+split[1]);
            fileExtension = FileExtension.MULTIPART;
        } else if(Objects.nonNull(contentLength)){
            log.debug("[Not Multipart Body]");
            body = getBody(Integer.parseInt(contentLength), inputSize, buffer, bufferIndex,inputStream);
        }

        if (Objects.equals(fileExtension, FileExtension.MULTIPART)) {
            try {
                int read = inputStream.read();
            } catch (Exception e) {

            }
        }

        if (uri.contains("?")) {
            var uriSplit = uri.split("\\?");
            log.debug("[URI] {}",Arrays.toString(uriSplit));
            uri = uriSplit[0];
            fileExtension = getFileExtension(uri);
            body = uriSplit[1];
        }

        var httpRequest = new HttpRequest(method, uri, fileExtension, httpVersion, headers, cookies, body, buffer,
            bufferIndex, inputStream, outputStream);

        return httpRequest;
    }

    public FileExtension getFileExtension(String uri) {
        int lastIndex = uri.lastIndexOf(".");
        var extension = uri.substring(lastIndex+1, uri.length()).toUpperCase();

        return FileExtension.fromString(extension);
    }

    public String getBody(int contentLength, int inputSize, byte[] buffer, int bufferIndex, InputStream inputStream) {
        StringBuilder sb = new StringBuilder();



        int read = 0;
        var bos = new ByteArrayOutputStream();
        try {
            for(int i = 0; i <= contentLength; i++){
                read = inputStream.read();
                inputSize ++;

                if (inputSize > maxInputSize) {
                    throw ClientErrorCode.URI_TOO_LONG.exception();
                }

                bos.write(read);

            }
        } catch (IOException exception) {
            log.error("[BODY READ ERROR] {}", exception.getMessage());
            throw ServerErrorCode.INTERNAL_SERVER_ERROR.exception();
        }
        var body = bos.toString();
        sb.append(body);
        var result = sb.toString().replace("\n", "");


        return result;
    }

    public int parsingHeader(String[] lines, Map<String, String> headers, Map<String, Cookie> cookies) {
        int idx = 1;
        for (; idx < lines.length; idx++) {
            if (Objects.equals(lines[idx], EMPTY_STRING)) {
                break;
            }
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
                    if (Objects.equals(key,SESSIONKEY)) {
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
}
