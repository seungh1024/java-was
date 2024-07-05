package codesquad.request.handler;

import codesquad.Main;
import codesquad.exception.client.ClientErrorCode;
import codesquad.http.HttpStatus;
import codesquad.request.format.ClientRequest;
import codesquad.request.handler.get.FileHandler;
import codesquad.response.format.ClientResponse;
import codesquad.util.FileExtension;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ClientRequestHandler {
    private static final ClientRequestHandler requestHandler = new ClientRequestHandler();
    private final String rootPath;
//    private static final String filePath = "/src/main/resources/static";

    private ClientRequestHandler() {
        rootPath = System.getProperty("user.dir");
    }

    public static ClientRequestHandler getInstance() {
        return requestHandler;
    }

    public ClientResponse doGet(ClientRequest request) throws IOException {
        byte[] responseBody;
        ClientResponse clientResponse = null;

        if (request.uri().contains(".")) { // 파일을 원하는 경우
//            String path = rootPath + filePath + request.uri();

//            File file = new File(path);
            System.out.println("??");
            ClassLoader classLoader = Main.class.getClassLoader();
            System.out.println("classLoader = " + classLoader);
            URL resource = classLoader.getResource(request.staticUri());
            InputStream inputStream = classLoader.getResourceAsStream(request.staticUri());
            if (inputStream == null) {
                throw ClientErrorCode.NOT_FOUND.exception();
            }
//            String fileName = file.getName();
            String fileName = resource.getFile();
            System.out.println("fileName = "+fileName);
            int index = fileName.lastIndexOf('.');
            StringBuilder sb = new StringBuilder();
            for (int i = index+1; i < fileName.length(); i++) {
                sb.append(fileName.charAt(i));
            }

            FileExtension fileExtension = FileExtension.fromString(sb.toString().toUpperCase());
            switch (fileExtension) {
                case HTML:
                case CSS :
                case JS :
                case ICO :
                case PNG :
                case JPG:
                case SVG:
                    responseBody = FileHandler.getInstance().readFileWithByte(inputStream);
                    var map = new HashMap<String,Map<String,String>>();
                    clientResponse = new ClientResponse(HttpStatus.OK, fileExtension.getContentType(),map, responseBody);

            }

        }


        return clientResponse;
    }
}
