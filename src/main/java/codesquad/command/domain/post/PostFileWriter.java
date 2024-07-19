package codesquad.command.domain.post;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PostFileWriter {
    private static final PostFileWriter fileWriter = new PostFileWriter();
    private final ThreadPoolExecutor threadPoolExecutor;

    private int corePoolSize;
    private int maxPoolSize;
    private int keepAliveTime;
    private int queueCapacity;

    private PostFileWriter() {
        this.corePoolSize = 10;
        this.maxPoolSize = 50;
        this.keepAliveTime = 10;
        this.queueCapacity = 10;

        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity));
    }

    public static PostFileWriter getInstance() {
        return fileWriter;
    }

    public void writeBuffer(FileOutputStream fos, byte[] buffer, int offset, int length){
        threadPoolExecutor.execute(()->{
            try {
                synchronized (fos){
                    fos.write(buffer,offset,length);
                    fos.flush();
                }
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }

        });
    }
}
