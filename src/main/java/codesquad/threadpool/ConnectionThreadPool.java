package codesquad.threadpool;

import codesquad.task.ClientTask;

import java.net.Socket;
import java.util.concurrent.*;

public class ConnectionThreadPool {
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
    public void run(Socket clientSocket) {
        var clientTask = new ClientTask(clientSocket);
        //TODO 이거 블록되니까 CompletableFuture로 변환 필요
        threadPoolExecutor.submit(() ->
                clientTask.run());



    }
}
