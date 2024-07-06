package codesquad.http.request.format;

import codesquad.exception.client.ClientErrorCode;

import java.util.Objects;

public enum HttpMethod {
    GET, POST, PUT, DELETE;

    public static HttpMethod fromString(String httpMethod) {
        for (HttpMethod value : HttpMethod.values()) {
            if(Objects.equals(httpMethod, value.name())) {
                return value;
            }
        }

        throw ClientErrorCode.METHOD_NOT_ALLOWED.exception();
    }
}
