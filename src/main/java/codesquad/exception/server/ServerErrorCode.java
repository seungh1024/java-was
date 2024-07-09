package codesquad.exception.server;

import codesquad.exception.CustomException;
import codesquad.http.HttpStatus;

public enum ServerErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"서버에 문제가 발생했습니다."),
    SOCKET_CLOSED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"네트워크에 문제가 발생했습니다."),
    CANNOT_CREATE_SESSION(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 생겨 로그인을 할 수 없습니다. 잠시 후에 다시 시도해 주세요.");

    private HttpStatus httpStatus;
    private final String errorMessage;

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    public int getHttpStatusCode() {
        return httpStatus.getStatusCode();
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    ServerErrorCode(HttpStatus httpStatus, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }

    public CustomException exception() {
        return new CustomException(httpStatus,errorMessage);
    }
}
