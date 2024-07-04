package codesquad.exception;

import codesquad.http.HttpStatus;

public class CustomException extends RuntimeException{
    private int statusCode;
    private String statusName;

    public CustomException(HttpStatus httpStatus, String errorMessage) {
        super(errorMessage);
        this.statusCode = httpStatus.getStatusCode();
        this.statusName = httpStatus.getStatusName();
    }
}
