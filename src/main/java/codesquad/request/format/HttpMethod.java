package codesquad.request.format;

import codesquad.exception.client.ClientErrorCode;

import java.util.Objects;

public enum HttpMethod {
    GET, POST, PUT, DELETE;

    public static HttpMethod fromString(String method) {
        for (HttpMethod value : HttpMethod.values()) {
            if(Objects.equals(method, value.name())) {
                return value;
            }
        }

        throw ClientErrorCode.METHOD_NOT_ALLOWED.exception();
    }
}
