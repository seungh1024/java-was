package codesquad.command.domain.post;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class PostFileWriter {
    private static final PostFileWriter fileWriter = new PostFileWriter();
    private final ExecutorService[] threadPoolExecutor;
    private final ReentrantLock[] locks;

    private int corePoolSize;
    private int maxPoolSize;
    private int keepAliveTime;
    private int queueCapacity;

    private PostFileWriter() {
        this.corePoolSize = 1;
        this.maxPoolSize = 1;
        this.keepAliveTime = 10;
        this.queueCapacity = 100;

        threadPoolExecutor = new ExecutorService[10];
        locks = new ReentrantLock[10];
        for (int i = 0; i < 10; i++) {
            threadPoolExecutor[i] = Executors.newSingleThreadExecutor();
            locks[i] = new ReentrantLock();
        }
    }

    public static PostFileWriter getInstance() {
        return fileWriter;
    }

    public void writeBuffer(FileOutputStream fos, byte[] buffer, int offset, int length){
        var hashCode = fos.hashCode()%10;

        threadPoolExecutor[hashCode].execute(()->{
            try {
                fos.write(buffer,offset,length);
                fos.flush();

            } catch (IOException exception) {
                exception.printStackTrace();
                throw new RuntimeException(exception);
            }
        });

    }
}
