package codesquad.http.request.statichandler;

import codesquad.exception.client.ClientErrorCode;
import codesquad.file.CustomFileReader;
import codesquad.http.request.format.HttpRequest;
import codesquad.util.FileExtension;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

public class StaticResourceHandler {
    private static final StaticResourceHandler requestHandler = new StaticResourceHandler();

    private StaticResourceHandler() {
    }

    public static StaticResourceHandler getInstance() {
        return requestHandler;
    }

    public byte[] getStaticResource(HttpRequest request) {
        byte[] responseBody = null;

        ClassLoader classLoader = getClass().getClassLoader();
        var path = "static"+request.uri();
        
        URL resource = classLoader.getResource(path);
        InputStream inputStream = classLoader.getResourceAsStream(path);
        if (inputStream == null) {
            throw ClientErrorCode.NOT_FOUND.exception();
        }
//            String fileName = file.getName();
        String fileName = resource.getFile();
        int index = fileName.lastIndexOf('.');
        StringBuilder sb = new StringBuilder();
        for (int i = index+1; i < fileName.length(); i++) {
            sb.append(fileName.charAt(i));
        }

        FileExtension fileExtension = FileExtension.fromString(sb.toString().toUpperCase());
        switch (fileExtension) {
            case CSS :
            case JS :
            case ICO :
            case PNG :
            case JPG:
            case SVG:
                responseBody = CustomFileReader.getInstance().readFileWithByte(inputStream);

        }

        if (Objects.isNull(responseBody)) {
            throw ClientErrorCode.NOT_FOUND.exception();
        }

        return responseBody;
    }
}
