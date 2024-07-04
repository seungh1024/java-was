package codesquad.request.handler;

import codesquad.request.format.ClientRequest;
import codesquad.request.handler.get.FileHandler;
import codesquad.util.FileExtension;

import java.io.File;
import java.io.IOException;

public class ClientRequestHandler {
    private static final ClientRequestHandler requestHandler = new ClientRequestHandler();
    private final String rootPath;
    private static final String filePath = "/src/main/resources/static";

    private ClientRequestHandler() {
        rootPath = System.getProperty("user.dir");
    }

    public static ClientRequestHandler getInstance() {
        return requestHandler;
    }

    public byte[] doGet(ClientRequest request) throws IOException {
        byte[] result = null;
        if (request.uri().contains(".")) { // 파일을 원하는 경우
            String path = rootPath + filePath + request.uri();
            System.out.println("path = "+path);
            File file = new File(path);
            if (file.exists()) {
                System.out.println("exist");
            }
            String fileName = file.getName();
            int index = fileName.lastIndexOf('.');
            StringBuilder sb = new StringBuilder();
            for (int i = index+1; i < fileName.length(); i++) {
                sb.append(fileName.charAt(i));
            }

            FileExtension fileExtension = FileExtension.fromString(sb.toString().toUpperCase());
            System.out.println(sb.toString().toUpperCase());
            System.out.println(fileExtension);

            switch (fileExtension) {
                case HTML:
                case CSS :
                case JS :
                case ICO :
                case PNG :
                case JPG:
                    result = FileHandler.getInstance().readFileWithByte(file);

            }

        }
        return result;
    }
}
