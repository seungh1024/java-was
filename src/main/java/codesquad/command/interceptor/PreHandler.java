package codesquad.command.interceptor;

import codesquad.http.request.format.HttpRequest;

import java.lang.reflect.Method;

public interface PreHandler {
    boolean handle(HttpRequest httpRequest);
}
