package codesquad;

import codesquad.webserver.ConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static ConnectionHandler connectionThreadPool;

    public static void main(String[] args) throws IOException {
        connectionThreadPool = ConnectionHandler.getInstance();

        var serverSocket = new ServerSocket(8080); // 8080 포트에서 서버를 엽니다.
        log.debug("Listening for connection on port 8080 ....");


        while (true) { // 무한 루프를 돌며 클라이언트의 연결을 기다립니다.
            Socket clientSocket = serverSocket.accept();
            log.debug("Client connected");
            connectionThreadPool.run(clientSocket);
        }

    }
}
