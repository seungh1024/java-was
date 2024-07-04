package codesquad.threadpool;

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

        CompletableFuture.supplyAsync(() ->  clientTask.run(), threadPoolExecutor)
                .exceptionally(throwable ->{
                    Exception exception = (Exception) (throwable instanceof Exception ? throwable : new Exception(throwable));
                    try {
                        doErrorResponse(clientSocket,exception.getMessage());
                    } catch (IOException e) {
                        log.error("[Client Socket Error] : {} ",exception.getMessage(),exception);
                    }
                    return null;
                })
                .thenAccept(responseData ->{
                    System.out.println(responseData);
                    try {
                        doResponse(clientSocket, responseData);
                    } catch (IOException exception) {
                        log.error("[Client Socket Error] : {} ",exception.getMessage(),exception);
                    }
                }).whenComplete((messageBody,throwable)->{
                    // TODO 작업 결과와 상관 없이 처리하기 -> 소켓 연결 해제 등
                    try {
                        clientSocket.close();
                    } catch (Exception e) {
                        log.error("Error closing connection", e);
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

    public void doErrorResponse(Socket socket, String message) throws IOException {
        var outputStream = socket.getOutputStream();

        String htmlMessage = "<html>" +
                "<head><title>" + 400 + "</title></head>" +
                "<body>" +
                "<h1>" + 400 + "</h1>" +
                "<p>" + message + "</p>" +
                "</body>" +
                "</html>";
        String responseHeader = "HTTP/1.1 " + 400 + "\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + htmlMessage.length() + "\r\n" +
                "\r\n";
        outputStream.write(responseHeader.getBytes());
        outputStream.write(htmlMessage.getBytes());
        outputStream.flush();
    }
}
