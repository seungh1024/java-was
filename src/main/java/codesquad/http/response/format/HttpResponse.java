package codesquad.http.response.format;

import codesquad.exception.CustomException;
import codesquad.exception.server.ServerErrorCode;
import codesquad.http.HttpStatus;
import codesquad.http.request.dynamichandler.DynamicHandleResult;
import codesquad.util.FileExtension;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static codesquad.util.StringSeparator.*;

public record HttpResponse(
    HttpStatus httpStatus,
    String httpVersion,
    Map<String, String> headers,
    byte[] body
){

    public static HttpResponse getHtmlResponse(HttpStatus httpStatus, FileExtension fileExtension, byte[] body) {
        var responseHeaders = new HashMap<String, String>();
        responseHeaders.put("Content-Type", fileExtension.getContentType());
        responseHeaders.put("Content-Length", String.valueOf(body.length));
        responseHeaders.put("Set-Cookie", "test=zzz");

        return new HttpResponse(httpStatus, "HTTP/1.1", responseHeaders, body);
    }

    public static HttpResponse getErrorResponse(Exception exception) {
        HttpStatus httpStatus = ServerErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus();
        var statusCode = httpStatus.getStatusCode();
        var errorName = httpStatus.getName();
        var errorMessage = exception.getMessage();

        if (CustomException.class.isAssignableFrom(exception.getClass())) {
            CustomException customException = (CustomException)exception;
            statusCode = customException.getStatusCode();
            errorName = customException.getErrorName();
            for (HttpStatus value : HttpStatus.values()) {
                if (value.getStatusCode() == statusCode) {
                    httpStatus = value;
                    break;
                }
            }

        }


        String htmlMessage = "<html>" +
                "<head><meta charset=\"UTF-8\" /></head>" +
                "<body>" +
                "<h1>" + statusCode + " " + errorName + "</h1>" +
                "<p>" + errorMessage + "</p>\n" +
                "<a class=\"btn btn_contained btn_size_s\" href=\"/index.html\">메인으로</a>\n"+
                "</body>" +
                "</html>";
        byte[] byteMessage = htmlMessage.getBytes();

        var responseHeaders = new HashMap<String, String>();
        responseHeaders.put("Connection", "close");
        responseHeaders.put("Content-Type", FileExtension.HTML.getContentType());
        responseHeaders.put("Content-Length", String.valueOf(byteMessage.length));


        return new HttpResponse(httpStatus,"HTTP/1.1", responseHeaders, byteMessage);
    }

    public static HttpResponse getHtmlResponse(DynamicHandleResult dynamicHandleResult) {

        var responseHeaders = new HashMap<String, String>();
        responseHeaders.put("Content-Type", dynamicHandleResult.fileExtension().getContentType() + "; charset=UTF-8");
        for (Map.Entry<String, String> entry : dynamicHandleResult.headers().entrySet()) {
            responseHeaders.put(entry.getKey(), entry.getValue());
        }
        byte[] body = null;
        if (dynamicHandleResult.hasBody()) {
            body = dynamicHandleResult.body().toString().getBytes();
            responseHeaders.put("Content-Length", String.valueOf(body.length));
        }

        return new HttpResponse(dynamicHandleResult.httpStatus(), "HTTP/1.1", responseHeaders, body);
    }


    public byte[] toByteArray() {
        StringBuilder sb = new StringBuilder(httpVersion).append(SPACE_SEPARATOR);
        sb.append(httpStatus.getStatusCode()).append(SPACE_SEPARATOR);
        sb.append(httpStatus.getStatusMessage()).append(CR).append(LF);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(HEADER_SEPARATOR).append(SPACE_SEPARATOR).append(entry.getValue()).append(CR).append(LF);
        }
        sb.append(CR).append(LF);


        byte[] headerBytes = null;
        try{
            headerBytes = sb.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException exception) {
			throw ServerErrorCode.INTERNAL_SERVER_ERROR.exception();
		}
        byte[] result = null;
        if (Objects.isNull(body)) {
            result = headerBytes;
        } else {
            result = new byte[headerBytes.length + body.length];
            int idx = 0;
            for(byte b : headerBytes) {
                result[idx++] = b;
            }
            for (byte b : body) {
                result[idx++] = b;
            }
        }

        return result;
    }
}
