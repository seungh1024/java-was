package codesquad.exception;

import codesquad.http.HttpStatus;

public class CustomException extends RuntimeException{
    private int statusCode;
    private String errorName;

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getErrorName() {
        return this.errorName;
    }

    public CustomException(HttpStatus httpStatus, String errorMessage) {
        super(errorMessage);
        this.statusCode = httpStatus.getStatusCode();
        this.errorName = httpStatus.getName();
    }

}
