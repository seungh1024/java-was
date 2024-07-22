package codesquad.ioTest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import codesquad.command.domain.post.PostFileWriter;

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

		doMultiThreadIO(5,5);
		Thread.sleep(5000);
		doSingleThreadIO(5,5);

	}

	void doSingleThreadIO(int size, int range) throws IOException, InterruptedException {
		int sequentialExceptionCount = 0;

		File[] files2 = new File[size];
		FileOutputStream[] foss2 = new FileOutputStream[size];
		InputStream[] iss2 = new InputStream[size];
		for (int i = 0; i < size; i++) {
			files2[i] = new File(rootPath + File.separator + "single/" + (i%range)+"giphy.webp");
			if (files2[i].exists()) {
				files2[i].delete();
			}
			files2[i].createNewFile();
			foss2[i] = new FileOutputStream(files2[i]);
			iss2[i] = classLoader.getResourceAsStream("static/test/"+(i%range)+"giphy.webp");
		}

		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity));
		CountDownLatch countDownLatch = new CountDownLatch(size);
		AtomicInteger exceptionCount = new AtomicInteger(0);

		long start = System.currentTimeMillis();
		for (int i = 0; i < size; i++) {
			int idx = i;
			threadPoolExecutor.execute(()->{
				int readSize = 0;
				byte[] buffer= new byte[4*1024];
				try {
					while ((readSize = iss2[idx].read(buffer)) != -1) {
						PostFileWriter.getInstance().writeBlockingBuffer(foss2[idx],buffer,0,readSize);
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

		System.out.println("sequential IO time = "+(end-start));
		System.out.println("sequentail exception count = "+exceptionCount.get());

		for (int i = 0; i < size; i++) {
			if (files2[i].exists()) {
				// files2[i].delete();
			}
			foss2[i].close();
		}
	}

	void doMultiThreadIO(int size, int range) throws IOException, InterruptedException {
		File[] files = new File[size];
		FileOutputStream[] foss = new FileOutputStream[size];
		InputStream[] iss = new InputStream[size];
		AtomicInteger[] ai = new AtomicInteger[size];
		for (int i = 0; i < size; i++) {
			files[i] = new File(rootPath + File.separator+"multi/" + (i%range)+"giphy.webp");
			if (!files[i].exists()) {
				files[i].createNewFile();
			}
			foss[i] = new FileOutputStream(files[i]);
			iss[i] = classLoader.getResourceAsStream("static/test/"+(i%range)+"giphy.webp");
			ai[i] = new AtomicInteger(0);
		}


		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity));
		CountDownLatch countDownLatch = new CountDownLatch(size);
		AtomicInteger exceptionCount = new AtomicInteger(0);

		long start = System.currentTimeMillis();
		for (int i = 0; i < size; i++) {
			int idx = i;
			var atomic = ai[idx];
			threadPoolExecutor.execute(()->{
				int readSize = 0;
				byte[] buffer= new byte[4*1024];
				var bos = new ByteArrayOutputStream();
				try {
					while ((readSize = iss[idx].read(buffer)) != -1) {
						bos.write(buffer);
						var copyBuffer = bos.toByteArray();
						bos.reset();
						atomic.getAndIncrement();
						PostFileWriter.getInstance().writeBuffer(foss[idx],copyBuffer,0,readSize,false);
					}
					PostFileWriter.getInstance().writeBuffer(foss[idx],buffer,0,readSize,true);

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
		System.out.println("multi exception count = "+ exceptionCount.get());

		for (int i = 0; i < size; i++) {
			if (files[i].exists()) {
				// files[i].delete();
			}
			// foss[i].close();
		}
	}
}
