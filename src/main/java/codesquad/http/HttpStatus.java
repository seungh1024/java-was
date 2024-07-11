package codesquad.http;

public enum HttpStatus {
    OK(200,"OK"),
    CREATE(201,"CREATE"),
    FOUND(302, "FOUND"),
    BAD_REQUEST(400, "BAD REQUEST"),
    METHOD_NOT_ALLOWED(405,"METHOD NOT ALLOWED"),
    URI_TOO_LONG(414,"URI TOO LONG"),
    NOT_FOUND(404, "NOT FOUND"),
    UNAUTHORIZED(401, "UNAUTHORIZED"),
    CONFLICT(409,"CONFLICT"),
    INTERNAL_SERVER_ERROR(500, "INTERNAL SERVER ERROR"),
    ;

    int statusCode;
    String statusMessage;

    HttpStatus(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }
    public String getStatusMessage() {
        return statusMessage;}

    public String getName() {
        return name();
    }
}
