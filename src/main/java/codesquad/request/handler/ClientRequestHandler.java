package codesquad.request.handler;

import codesquad.request.format.ClientRequest;

import java.io.File;

public class ClientRequestHandler {
    private static final ClientRequestHandler requestHandler = new ClientRequestHandler();
    private final String rootPath;

    private ClientRequestHandler() {
        rootPath = System.getProperty("user.dir");
    }

    public static ClientRequestHandler getInstance() {
        return requestHandler;
    }

    public String doGet(ClientRequest request) {
        String result = null;
        if (request.uri().contains(".")) { // 파일을 원하는 경우
            File file = new File(request.uri());
            String fileName = file.getName();
            int index = fileName.lastIndexOf('.');
        }
        return result;
    }
}
