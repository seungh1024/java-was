package codesquad.response.format;

import codesquad.http.HttpStatus;
import codesquad.util.StringSeparator;
import com.sun.net.httpserver.HttpServer;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static codesquad.util.StringSeparator.*;

public record ClientResponse (
    HttpStatus httpStatus,
    String contentType,
    Map<String, Map<String,String>> headers,
    byte[] body
){
    public void setDefaultHeaderOptions() {
        System.out.println(headers);
        headers.put("Server",Map.of("Apache",null));
    }

    public byte[] toByteArray(){
        StringBuilder sb = new StringBuilder("HTTP/1.1 ");
        sb.append(httpStatus.getStatusCode()).append(CR).append(LF);
        sb.append(httpStatus.getStatusName()).append(CR).append(LF);
        sb.append("Content-Type: ").append(contentType).append(CR).append(LF);
        sb.append("Content-Length: ").append(body.length).append(CR).append(LF);
        sb.append(CR).append(LF);

        byte[] headerBytes = sb.toString().getBytes(StandardCharsets.US_ASCII);
        byte[] result = new byte[headerBytes.length + body.length];
        int idx = 0;
        for(byte b : headerBytes) {
            result[idx++] = b;
        }
        for (byte b : body) {
            result[idx++] = b;
        }

        return result;
    }
}
