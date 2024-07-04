package codesquad.exception.client;

import codesquad.exception.CustomException;
import codesquad.http.HttpStatus;

public enum ClientErrorCode {
    URI_TOO_LONG(HttpStatus.URI_TOO_LONG,"네트워크에 문제가 발생했습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "올바른 HTTP 메소드가 아닙니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 요청입니다.");

    private HttpStatus httpStatus;
    private final String errorMessage;

    ClientErrorCode(HttpStatus httpStatus, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }

    public CustomException exception() {
        return new CustomException(httpStatus,errorMessage);
    }
}
