package codesquad.http;

public enum HttpStatus {
    OK(200),
    METHOD_NOT_ALLOWED(405),
    URI_TOO_LONG(414),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500),
    ;

    int statusCode;

    HttpStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getName() {
        return name();
    }
}
