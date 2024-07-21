package codesquad.webserver;

import codesquad.command.CommandManager;
import codesquad.exception.client.ClientErrorCode;
import codesquad.http.HttpStatus;
import codesquad.http.request.dynamichandler.DynamicHandleResult;
import codesquad.http.request.format.HttpMethod;
import codesquad.http.request.format.HttpRequest;
import codesquad.http.request.statichandler.StaticResourceHandler;
import codesquad.http.response.format.HttpResponse;
import codesquad.http.parser.HttpRequestParser;
import codesquad.util.FileExtension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionHandler {
    private static final Logger log = LoggerFactory.getLogger(ConnectionHandler.class);
    private static ConnectionHandler connectionThreadPool;

    private final ThreadPoolExecutor threadPoolExecutor;
    
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
                    if(Objects.equals(httpRequest.fileExtension(),FileExtension.MULTIPART)){
                        var domainResponse = CommandManager.getInstance().execute(httpRequest);
                        var dynamicHandleResult = DynamicHandleResult.of(domainResponse);

                        return HttpResponse.getHtmlResponse(dynamicHandleResult);
                    }
                    else if (Objects.equals(httpRequest.fileExtension(), FileExtension.DYNAMIC)) { // 동적 요청 처리
                        var domainResponse = CommandManager.getInstance().execute(httpRequest);
                        var dynamicHandleResult = DynamicHandleResult.of(domainResponse);

                        return HttpResponse.getHtmlResponse(dynamicHandleResult);
                    } else { // 정적 요청 처리애
                        if (Objects.equals(httpRequest.method(), HttpMethod.GET)) {
                            var body = StaticResourceHandler.getInstance().getStaticResource(httpRequest);
                            return HttpResponse.getHtmlResponse(HttpStatus.OK, httpRequest
                                    .fileExtension(), body);
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

            }).whenComplete((applyResult,throwable)->{
                HttpResponse response = applyResult;

                if (!Objects.isNull(applyResult) && Objects.isNull(throwable)) { // 정상 응답 처리
                    if (response.headers().get("chunked") == null) {
                        doResponse(clientSocket, response);
                    } else {
                        log.debug("IMAGE!");
                        while (clientSocket.isConnected()) {

                        }
                        return;
                    }
                } else {
                    try {
                        Exception exception = (Exception)throwable.getCause();
                        exception.printStackTrace();
                        var errorResponse = HttpResponse.getErrorResponse(exception);
                        doResponse(clientSocket, errorResponse);
                    } catch (Exception e) {
                        log.error("[Server Error] 예외 응답 처리 중 예외 발생");
                        e.printStackTrace();
                    }
                }

                try{

                    if (!Objects.isNull(clientSocket) && !clientSocket.isClosed() && !clientSocket.getKeepAlive()) {
                        clientSocket.close();
                        log.debug("[SOCKET] client socket closed");
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
            var offset = 0;
            var chunkSize = 4 * 1024;
            while (offset < responseData.length) {
                var length = Math.min(chunkSize, responseData.length - offset);

                outputStream.write(responseData, offset, length);
                outputStream.flush();
                offset += length;
            }
        } catch (IOException exception) {
            log.error("[Server Error] : data 전송 중 에러 발생");
        }

    }
}
