package codesquad.exception.client;

import codesquad.exception.CustomException;
import codesquad.http.HttpStatus;

public enum ClientErrorCode {
    URI_TOO_LONG(HttpStatus.URI_TOO_LONG,"네트워크에 문제가 발생했습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "올바른 HTTP 메소드가 아닙니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 요청입니다. 요청 경로를 확인해 주세요."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다. 회원가입을 해주세요"),
    USERID_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다."),
    PARAMETER_FORMAT_EXCEPTION(HttpStatus.BAD_REQUEST, "입력 데이터 형식이 일치하지 않습니다. 입력값을 다시 확인해 주세요."),
    ;

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
