package codesquad.threadpool;

import codesquad.exception.CustomException;
import codesquad.exception.server.ServerErrorCode;
import codesquad.response.format.ClientResponse;
import codesquad.task.ClientTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.concurrent.*;

public class ConnectionThreadPool {
    private static final Logger log = LoggerFactory.getLogger(ConnectionThreadPool.class);
    private static ConnectionThreadPool connectionThreadPool;

    private ThreadPoolExecutor threadPoolExecutor;
    
    private int corePoolSize;
    private int maxPoolSize;
    private int keepAliveTime;
    private int queueCapacity;

    private ConnectionThreadPool() {
        this.corePoolSize = 10;
        this.maxPoolSize = 50;
        this.keepAliveTime = 10;
        this.queueCapacity = 10;

        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity));
    }

    private ConnectionThreadPool(int corePoolSize, int maxPoolSize, int keepAliveTime, int queueCapacity) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.queueCapacity = queueCapacity;

        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity));
    }

    // TODO new 제거
    public static ConnectionThreadPool getInstance() {
        if (connectionThreadPool == null) {
            return new ConnectionThreadPool();
        }
        return connectionThreadPool;
    }

    public static ConnectionThreadPool getInstance(int corePoolSize, int maxPoolSize, int keepAliveTime, int queueCapacity) {
        if (connectionThreadPool == null) {
            return new ConnectionThreadPool(corePoolSize, maxPoolSize, keepAliveTime, queueCapacity);
        }

        return connectionThreadPool;
    }

    // TODO future 로 값 받아와서 리턴하기, timeout 설정하기
    public void run(Socket clientSocket) throws IOException {
        var clientTask = new ClientTask(clientSocket);
        clientSocket.setSoTimeout(3000);


        CompletableFuture.supplyAsync(() -> clientTask.run(), threadPoolExecutor)
            .whenComplete((clientResponse, throwable) -> {
                try {
                    if (throwable != null) {
                        doErrorResponse(clientSocket, (Exception)throwable.getCause());
                    } else {
                        doResponse(clientSocket, clientResponse);
                    }
                } catch (Exception exception) {
                    log.error("[Server Error] 응답에 실패했습니다.", exception);
                } finally {
                    try {
                        clientSocket.close();
                    } catch (Exception e) {
                        log.error("Error closing connection", e);
                    }
                }
            });
    }


    public void doResponse(Socket socket, ClientResponse response) throws IOException {
        var outputStream = socket.getOutputStream();
        byte[] byteArray = response.toByteArray();
        int offset = 0;
        int chunkSize = 4*1024;
        while(offset < byteArray.length) {
            int length = Math.min(chunkSize, byteArray.length - offset);

            outputStream.write(byteArray, offset, length);
            outputStream.flush();
            offset += length;
        }

    }



    public void doErrorResponse(Socket socket, Exception exception) throws IOException{

        int statusCode = ServerErrorCode.INTERNAL_SERVER_ERROR.getHttpStatusCode();
        String errorName = ServerErrorCode.INTERNAL_SERVER_ERROR.name();
        String errorMessage = exception.getMessage();

        if (exception.getClass().isAssignableFrom(CustomException.class)) {
            CustomException customException = (CustomException)exception;
            statusCode = customException.getStatusCode();
            errorName = customException.getErrorName();
        }

        String htmlMessage = "<html>" +
            "<head><meta charset=\"UTF-8\" /></head>" +
            "<body>" +
            "<h1>" + statusCode + " " + errorName + "</h1>" +
            "<p>" + errorMessage+ "</p>" +
            "</body>" +
            "</html>";
        byte[] byteMessage = htmlMessage.getBytes();

        String responseHeader = "HTTP/1.1 " + 400 + "\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + byteMessage.length + "\r\n" +
                "\r\n";
        var outputStream = socket.getOutputStream();
        outputStream.write(responseHeader.getBytes());
        outputStream.write(byteMessage);
        outputStream.flush();

    }
}
