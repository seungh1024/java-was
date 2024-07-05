package codesquad.task;

import codesquad.exception.CustomException;
import codesquad.exception.client.ClientErrorCode;
import codesquad.request.format.ClientRequest;
import codesquad.exception.server.ServerErrorCode;
import codesquad.request.format.HttpMethod;
import codesquad.request.handler.ClientRequestHandler;
import codesquad.response.format.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.*;

import static codesquad.util.StringSeparator.*;

public class ClientTask {
    private static final Logger log = LoggerFactory.getLogger(ClientTask.class);
    private static final int bufferSize = 8*1024;
    private static final int maxInputSize = 2 * 1024 * 1024; // chrome 2MB 길이로 제한

    private Socket clientSocket;
    private ClientRequest clientRequest;
    private ClientRequestHandler clientRequestHandler;

    public ClientTask(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.clientRequestHandler = ClientRequestHandler.getInstance();
    }

    // TODO Response 객체 리턴해주면 될듯?
    public ClientResponse run() {
        ClientResponse clientResponse = null;
        try {
            clientRequest = makeClientRequest();
            log.debug("[Client Request Info] : {}", clientRequest);

            HttpMethod requestMethod = HttpMethod.fromString(clientRequest.method());
            switch (requestMethod) {
                case GET -> clientResponse = clientRequestHandler.doGet(clientRequest);

            }


        } catch (IOException exception) {
            log.error("[Server Error] : {}","IO 에러 발생");
            throw ServerErrorCode.INTERNAL_SERVER_ERROR.exception();
        } catch (CustomException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("[Server Error] : {}", exception.getMessage(), exception);
            throw ServerErrorCode.INTERNAL_SERVER_ERROR.exception();
        }

        return clientResponse;
    }

    /**
     * 사용자 요청 정보를 담는 ClientRequest 클래스를 리턴하는 메소드
     */
    public ClientRequest makeClientRequest() throws IOException {
        var inputStream = clientSocket.getInputStream();


        // buffer 단위로 요청 데이터를 받아온다.
        var buffer = new byte[bufferSize];
        var readSize = 0;
        var inputSize = 0;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // 소켓 버퍼 데이터를 빨리 가져와야 하니 우선 outputStream으로 복사한다.
        while ((readSize = inputStream.read(buffer,0,bufferSize)) != -1) {
            inputSize += readSize;
            if (inputSize > maxInputSize) {
                throw ClientErrorCode.URI_TOO_LONG.exception();
            }

            outputStream.write(buffer, 0, readSize);
            if (readSize < bufferSize) {
                break;
            }
        }

        String htmlString = outputStream.toString();
        System.out.println(htmlString);


        return getClientRequest(htmlString);
    }

    public ClientRequest getClientRequest(String htmlString) {
        var lines = htmlString.replace(CR, EMPTY_STRING).split(LF);


        var firstLine = lines[0].split(SPACE_SEPARATOR);
        var method = firstLine[0];
        var uri = firstLine[1];
        var httpVersion = firstLine[2];

        Map<String, Map<String, String>> headers = new HashMap<>();
        var bodyIdx = parsingHeader(lines, headers);
        var body = getBody(lines, bodyIdx);

        return new ClientRequest(method, uri, "static"+uri, httpVersion, headers, body);
    }

    public String getBody(String[] lines, int bodyIdx) {
        StringBuilder sb = new StringBuilder();
        for (; bodyIdx < lines.length; bodyIdx++) {
            sb.append(lines[bodyIdx]).append("\n");
        }

        return sb.toString();
    }

    public int parsingHeader(String[] lines, Map<String, Map<String, String>> headers) {
        int idx = 1;
        for (; idx < lines.length; idx++) {
            var headerLine = lines[idx];
            if (headerLine.isEmpty()) { // 비어 있다는 것은 라인 구분자가 2개라는 것으로 header 영역이 끝난다.
                idx++;
                break;
            }
            var header = headerLine.split(HEADER_SEPARATOR);
            var headerName = header[0];
            var headerString = header[1];
            var headerValues = parsingHeaderValue(headerString);
            headers.put(headerName, headerValues);
        }

        return idx;
    }

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
