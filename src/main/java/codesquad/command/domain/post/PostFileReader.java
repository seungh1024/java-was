package codesquad.command.domain.post;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import codesquad.exception.server.ServerErrorCode;

import static codesquad.util.StringUtils.*;

public class PostFileReader {
	private static final PostFileReader fileReader = new PostFileReader();
	private static final int BUFFER_SIZE = 4*1024;


	private PostFileReader(){}

	public static PostFileReader getInstance() {
		return fileReader;
	}

	public void readFile(OutputStream outputStream, String filePath) {
		byte[] buffer = new byte[BUFFER_SIZE];


		StringBuilder sb = new StringBuilder("HTTP/1.1").append(SPACE_SEPARATOR);
		sb.append(200).append(SPACE_SEPARATOR);
		sb.append("OK").append(CR).append(LF);
		sb.append("Content-Type")
			.append(HEADER_SEPARATOR)
			.append(SPACE_SEPARATOR)
			.append("application/octet-stream")
			.append(CR)
			.append(LF);
		sb.append("Transfer-Encoding")
			.append(HEADER_SEPARATOR)
			.append(SPACE_SEPARATOR)
			.append("chunked")
			.append(CR)
			.append(LF);
		sb.append(CR).append(LF);

		var bos = new ByteArrayOutputStream();
		try {
			var headerByte = sb.toString().getBytes("UTF-8");
			outputStream.write(headerByte);

			var file = new File(filePath);

			var inputStream = new FileInputStream(file);
			int readSize = 0;
			while ((readSize = inputStream.read(buffer)) > 0) {
				var chunkSize = Integer.toHexString(readSize);
				bos.write((chunkSize + "\r\n").getBytes());
				bos.write(buffer, 0, readSize);
				bos.write(("\r\n").getBytes());

				outputStream.write(bos.toByteArray());
				outputStream.flush();
				bos.reset();

				if (readSize < BUFFER_SIZE) {
					break;
				}
			}

			bos.write((0 + "\r\n\r\n").getBytes());
			outputStream.write(bos.toByteArray());
			outputStream.flush();
			outputStream.close();
			bos.close();


		} catch (IOException exception) {
			exception.printStackTrace();
			throw ServerErrorCode.INTERNAL_SERVER_ERROR.exception();
		}

	}
}
