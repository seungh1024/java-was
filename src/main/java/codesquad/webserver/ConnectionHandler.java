package codesquad.webserver;

import codesquad.exception.client.ClientErrorCode;
import codesquad.http.HttpStatus;
import codesquad.http.request.format.HttpMethod;
import codesquad.http.request.format.HttpRequest;
import codesquad.http.request.statichandler.StaticResourceHandler;
import codesquad.http.response.HttpResponse;
import codesquad.http.parser.HttpRequestParser;
import codesquad.util.FileExtension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.*;

public class ConnectionHandler {
    private static final Logger log = LoggerFactory.getLogger(ConnectionHandler.class);
    private static ConnectionHandler connectionThreadPool;

    private ThreadPoolExecutor threadPoolExecutor;
    
    private int corePoolSize;
    private int maxPoolSize;
    private int keepAliveTime;
    private int queueCapacity;

    private ConnectionHandler() {
        this.corePoolSize = 10;
        this.maxPoolSize = 50;
        this.keepAliveTime = 10;
        this.queueCapacity = 10;

        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity));
    }

    private ConnectionHandler(int corePoolSize, int maxPoolSize, int keepAliveTime, int queueCapacity) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.queueCapacity = queueCapacity;

        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity));
    }

    public static ConnectionHandler getInstance() {
        if (connectionThreadPool == null) {
            return new ConnectionHandler();
        }
        return connectionThreadPool;
    }

    public static ConnectionHandler getInstance(int corePoolSize, int maxPoolSize, int keepAliveTime, int queueCapacity) {
        if (connectionThreadPool == null) {
            return new ConnectionHandler(corePoolSize, maxPoolSize, keepAliveTime, queueCapacity);
        }

        return connectionThreadPool;
    }

    // TODO future 로 값 받아와서 리턴하기, timeout 설정하기
    public void run(Socket clientSocket) {
        var clientTask = new HttpRequestParser(clientSocket);


        CompletableFuture.supplyAsync(() -> clientTask.parse(), threadPoolExecutor)
            .handle((parsingResult, throwable) -> { // parsing 결과 핸들링
                var httpRequest = parsingResult;
                log.debug("[Http Request] = {} ",httpRequest);

                if (Objects.isNull(parsingResult) || !Objects.isNull(throwable)) {
                    throw (RuntimeException)throwable;
				}

                return httpRequest;
            }).handle((handleResult, throwable) -> {
                HttpRequest httpRequest = (HttpRequest)handleResult;

                if (!Objects.isNull(httpRequest) && Objects.isNull(throwable)) {
                    if (Objects.equals(httpRequest.fileExtension(), FileExtension.DYNAMIC)) { // 동적 요청 처리

                    } else { // 정적 요청 처리애
                        if (Objects.equals(httpRequest.method(), HttpMethod.GET) && !Objects.equals(httpRequest.fileExtension(),FileExtension.NONE)) {
                            var body = StaticResourceHandler.getInstance().getStaticResource(httpRequest);
                            return HttpResponse.getHtmlResponse(HttpStatus.OK, httpRequest.fileExtension(), body);
                        } else if (!Objects.equals(httpRequest.method(),
                            HttpMethod.GET)) { // throw method not allowed error
                            throw ClientErrorCode.METHOD_NOT_ALLOWED.exception();
                        } else {
                            throw ClientErrorCode.NOT_FOUND.exception();
                        }
                    }

                } else {
                    throw (RuntimeException) throwable;
                }

                return null;
            }).whenComplete((applyResult,throwable)->{
                if (Objects.isNull(applyResult)) {

                    // return;
                }
                HttpResponse response = applyResult;
                if (!Objects.isNull(applyResult) && Objects.isNull(throwable)) { // 정상 응답 처리
                    doResponse(clientSocket, response);
                } else {
                    try {
                        Exception exception = (Exception)throwable.getCause();
                        var errorResponse = HttpResponse.getErrorResponse(exception);
                        doResponse(clientSocket, errorResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try{

                    if (!Objects.isNull(clientSocket) && !clientSocket.isClosed() && !clientSocket.getKeepAlive()) {
                        clientSocket.close();
                    }
                } catch (IOException exception) {
                    log.error("[Socket Error] : Client Socket Already Closed");
                }
			});

    }


    public void doResponse(Socket socket, HttpResponse response) {
        byte[] responseData = response.toByteArray();

        try {
            var outputStream = socket.getOutputStream();
            int offset = 0;
            int chunkSize = 4 * 1024;
            while (offset < responseData.length) {
                int length = Math.min(chunkSize, responseData.length - offset);

                outputStream.write(responseData, offset, length);
                outputStream.flush();
                offset += length;
            }
        } catch (IOException exception) {
            log.error("[Server Error] : data 전송 중 에러 발생");
        }

    }



    public void doErrorResponse(Socket socket, Exception exception) throws IOException{


        // String responseHeader = "HTTP/1.1 " + 400 + "\r\n" +
        //         "Content-Type: text/html\r\n" +
        //         "Content-Length: " + byteMessage.length + "\r\n" +
        //         "\r\n";
        // var outputStream = socket.getOutputStream();
        // outputStream.write(responseHeader.getBytes());
        // outputStream.write(byteMessage);
        // outputStream.flush();

    }
}
