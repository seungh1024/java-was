package codesquad.http.request.dynamichandler.methodhandler;

import codesquad.command.CommandManager;
import codesquad.command.domainResponse.DomainResponse;
import codesquad.http.request.dynamichandler.DynamicHandleResult;
import codesquad.http.request.format.HttpRequest;

public class PostHandler {
    private static final PostHandler handler = new PostHandler();

    private PostHandler() {}

    public static PostHandler getInstance() {
        return handler;
    }

    public DynamicHandleResult doPost(HttpRequest httpRequest) {
        DomainResponse domainResponse = CommandManager.getInstance().execute(httpRequest.method(), httpRequest.uri(), httpRequest.body());
        return DynamicHandleResult.of(domainResponse);
    }
}
