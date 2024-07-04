package codesquad.exception.client;

import codesquad.exception.CustomException;
import codesquad.http.HttpStatus;

public enum ClientErrorCode {
    URI_TOO_LONG(HttpStatus.URI_TOO_LONG,"네트워크에 문제가 발생했습니다.");

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
