package codesquad.ioTest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import codesquad.command.domain.post.PostFileWriter;
import codesquad.db.XSSUtil;

public class FileIoTest {

	ClassLoader classLoader = getClass().getClassLoader();

	String rootPath = System.getProperty("user.home")+File.separator+"post";

	int corePoolSize = 10;
	int maxPoolSize = 50;
	int keepAliveTime = 10;
	int queueCapacity = 100000;

	@Test
	@DisplayName("IO 멀티스레드 처리 테스트")
	void ioTest() throws IOException, InterruptedException {
		File[] files = new File[6];
		FileOutputStream[] foss = new FileOutputStream[6];
		InputStream[] iss = new InputStream[6];
		for (int i = 1; i < 6; i++) {
			files[i] = new File(rootPath + File.separator + i+"giphy.webp");
			if (!files[i].exists()) {
				files[i].createNewFile();
			}
			foss[i] = new FileOutputStream(files[i]);
			iss[i] = classLoader.getResourceAsStream("static/test/"+i+"giphy.webp");
		}


		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity));
		CountDownLatch countDownLatch = new CountDownLatch(5);
		AtomicInteger exceptionCount = new AtomicInteger(0);
		AtomicInteger ai = new AtomicInteger(0);

		long start = System.currentTimeMillis();
		for (int i = 1; i < 6; i++) {
			int idx = i;
			threadPoolExecutor.execute(()->{
				int readSize = 0;
				byte[] buffer= new byte[4*1024];
				var bos = new ByteArrayOutputStream();
				try {
					while ((readSize = iss[idx].read(buffer)) != -1) {
						bos.write(buffer);
						var copyBuffer = bos.toByteArray();
						bos.reset();
						ai.getAndIncrement();
						PostFileWriter.getInstance().writeBuffer(foss[idx],copyBuffer,0,readSize,ai);

					}
				} catch (Exception e) {
					e.printStackTrace();
					exceptionCount.getAndIncrement();
				}finally {
					countDownLatch.countDown();
				}
			});
		}

		countDownLatch.await();

		long end = System.currentTimeMillis();

		System.out.println("multi IO time = "+(end-start));
		System.out.println(exceptionCount);

		Thread.sleep(1000);

		for (int i = 1; i < 6; i++) {
			files[i] = new File(rootPath + File.separator + i+"giphy.webp");
			if (files[i].exists()) {
				files[i].delete();
			}
			foss[i].close();
		}


		int sequentialExceptionCount = 0;

		File[] files2 = new File[6];
		FileOutputStream[] foss2 = new FileOutputStream[6];
		InputStream[] iss2 = new InputStream[6];
		for (int i = 1; i < 6; i++) {
			files2[i] = new File(rootPath + File.separator + i+"giphy.webp");
			if (!files2[i].exists()) {
				files2[i].createNewFile();
			}
			foss2[i] = new FileOutputStream(files2[i]);
			iss2[i] = classLoader.getResourceAsStream("static/test/"+i+"giphy.webp");
		}
		long sequentialStart = System.currentTimeMillis();
		for (int i = 1; i < 6; i++) {
			int readSize = 0;
			byte[] buffer= new byte[4*1024];
			try {
				while ((readSize = iss2[i].read(buffer)) != -1) {
					PostFileWriter.getInstance().writeBlockingBuffer(foss2[i],buffer,0,readSize);
				}
			} catch (Exception e) {
				e.printStackTrace();
				sequentialExceptionCount++;
			}
		}
		long sequentialEnd = System.currentTimeMillis();

		System.out.println("sequential IO time = "+(sequentialEnd-sequentialStart));

		for (int i = 1; i < 6; i++) {
			files2[i] = new File(rootPath + File.separator + i+"giphy.webp");
			if (files2[i].exists()) {
				files2[i].delete();
			}
			foss2[i].close();
		}
	}
}
