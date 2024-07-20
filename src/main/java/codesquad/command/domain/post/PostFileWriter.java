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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class PostFileWriter {
    private static final PostFileWriter fileWriter = new PostFileWriter();
    private final ExecutorService[] threadPoolExecutor;

    private int corePoolSize;
    private int maxPoolSize;
    private int keepAliveTime;
    private int queueCapacity;

    private PostFileWriter() {
        this.corePoolSize = 1;
        this.maxPoolSize = 1;
        this.keepAliveTime = 10;
        this.queueCapacity = 100;

        threadPoolExecutor = new ExecutorService[100];
        for (int i = 0; i < 100; i++) {
            threadPoolExecutor[i] = Executors.newSingleThreadExecutor();
        }
    }

    public static PostFileWriter getInstance() {
        return fileWriter;
    }

    public void writeBuffer(FileOutputStream fos, byte[] buffer, int offset, int length, AtomicInteger ai){
        var hashCode = fos.hashCode()%100;

        threadPoolExecutor[hashCode].execute(()->{
            try {
                fos.write(buffer,offset,length);
                fos.flush();
                int andDecrement = ai.getAndDecrement();
                if (andDecrement == 0) {
                    fos.close();
                }

            } catch (IOException exception) {
                exception.printStackTrace();
                throw new RuntimeException(exception);
            }
        });

    }

    public void writeBlockingBuffer(FileOutputStream fos, byte[] buffer, int offset, int length){
        try {
            fos.write(buffer,offset,length);
            fos.flush();

        } catch (IOException exception) {
            exception.printStackTrace();
            throw new RuntimeException(exception);
        }
    }
}
