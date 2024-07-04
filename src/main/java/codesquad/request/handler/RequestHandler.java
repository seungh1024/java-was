package codesquad.request.handler;

import codesquad.request.format.ClientRequest;

public interface RequestHandler {
    String filePath = "src/main/resources/static";
    String handler(ClientRequest request);
}
