package codesquad.request.handler.get;

import codesquad.request.format.ClientRequest;

import java.io.*;

public class FileHandler{
    private static final FileHandler fileHandler = new FileHandler();
    private static final int BUFFER_SIZE = 4*1024;
    private FileHandler() {

    }

    public static FileHandler getInstance() {
        return fileHandler;
    }

    public byte[] readFileWithByte(File file) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        FileInputStream fileInputStream = new FileInputStream(file);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int readSize = 0;
        while ((readSize = fileInputStream.read(buffer)) > 0) {
            byteArrayOutputStream.write(buffer, 0, readSize);
            if(readSize < BUFFER_SIZE) {
                break;
            }
        }

        return byteArrayOutputStream.toByteArray();
    }
}
