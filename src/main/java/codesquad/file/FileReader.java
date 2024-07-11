package codesquad.file;

import java.io.*;

import codesquad.exception.server.ServerErrorCode;

public class FileReader {
    private static final FileReader fileHandler = new FileReader();
    private static final int BUFFER_SIZE = 4*1024;
    private FileReader() {

    }

    public static FileReader getInstance() {
        return fileHandler;
    }

    public byte[] readFileWithByte(InputStream inputStream) {
        byte[] buffer = new byte[BUFFER_SIZE];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            int readSize = 0;
            while ((readSize = inputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, readSize);
                if (readSize < BUFFER_SIZE) {
                    break;
                }
            }
        } catch (IOException exception) {
            throw ServerErrorCode.INTERNAL_SERVER_ERROR.exception();
        }

        return byteArrayOutputStream.toByteArray();
    }

    public byte[] readHtmlDynamically() {
        return null;
    }
}
