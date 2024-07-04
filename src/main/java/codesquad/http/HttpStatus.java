package codesquad.http;

public enum HttpStatus {
    OK(200),
    URI_TOO_LONG(414),
    INTERNAL_SERVER_ERROR(500),
    ;

    int statusCode;

    HttpStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusName() {
        return name();
    }
}
