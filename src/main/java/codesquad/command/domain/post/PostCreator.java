package codesquad.command.domain.post;

import codesquad.command.domainResponse.HttpClientRequest;
import codesquad.exception.client.ClientErrorCode;
import codesquad.exception.server.ServerErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class PostCreator {
    private static final Logger log = LoggerFactory.getLogger(PostCreator.class);
    private static final int BUFFER_SIZE = 1024*4;
    private static final int MAX_READ_SIZE = 31*1024*1024;
    private static final PostCreator postCreator = new PostCreator();
    private static final String rootPath = System.getProperty("user.home")+File.separator+"post";
    private PostCreator() {}

    public static PostCreator getInstance() {
        return postCreator;
    }

    public void save(HttpClientRequest request) {
        var readSize = 0;
        var inputSize = 0;
        var buffer = new byte[BUFFER_SIZE];
        var inputStream = request.getInputStream();
        var key = request.getBoundary();
        System.out.println(key);
        var endKey = key+"--";
        if (key == null) {
            throw ClientErrorCode.INVALID_MULTIPART_FORMAT.exception();
        }

        var userInfo = request.getUserInfo();
        var directory = new File(rootPath + File.separator + userInfo.id());
        log.info("[Directory Path] {}", directory.getPath());
        if(!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw ServerErrorCode.CANNOT_CREATE_POST.exception();
            }
        }



        StringBuilder sb = new StringBuilder();
        int enterCheck = 0;
        int bufferIndex = 0;
        int endBufferIndex = 0;
        var totalContent = new HashMap<String, String>(); // 전체 컨텐츠 저장
        var content = new HashMap<String, String>(); // 컨텐츠 저장. 이름, 값, body, 파일 경로 등
        FileOutputStream fos = null;

        try {
            boolean isFile = false;
            String contentKey = null;
            var body = new StringBuilder();

            var fileBuffer = new byte[BUFFER_SIZE];
            var fileIndex = 0;
            boolean isBody = false;
            boolean fileBody = false;

            while ((readSize = inputStream.read(buffer)) != -1) {
                inputSize += readSize;
                if (inputSize > MAX_READ_SIZE) {
                    throw ClientErrorCode.TOO_MUCH_DATA.exception();
                }

                for (int i = 0; i < readSize; i++) {
                    if(fileBody){
                        if(fileIndex == BUFFER_SIZE) {
                            PostFileWriter.getInstance().writeBuffer(fos,buffer, 0, BUFFER_SIZE);
                            fileIndex = 0;
//                            fileBody = false;
                        }
                        fileBuffer[fileIndex++] = buffer[i];
                    }



                    if (buffer[i] == 13) {
                        var line = sb.toString();
                        log.debug("[line] {}", line);
                        if (sb.length() == 0) {
                            log.debug("[Body Line Start]");
                            isBody = true;
                            continue;
                        }


                        if (Objects.equals(line, key)) {
                            log.debug("[Content Line] {}", line);
                            if (isBody) {
                                log.debug("[Body Info] {}",body);
                                if (body.toString().contains("\r")) {
                                    System.out.println("??");
                                }
                            }
                            isBody = false;
                        } else if (Objects.equals(line, endKey)) {
                            log.debug("[Content End Line] {}", line);
                            isBody = false;
                        } else if (line.contains("Content-Desposition: ")) {
                            log.debug("[Content Desposition] {}", line);
                        } else if (line.contains("Content-Type: ")) {
                            log.debug("[Content Type] {}", line);
                            // content type 저장
                            line = line.replace("Content-Type: ", "");
                            content.put("type", line);

                            // 확장자 저장
                            var extension = line.substring(line.lastIndexOf("/")).replace("/", ".");
                            content.put("extension", extension);

                            fileBody = true;
                            log.debug("[Content Type Save] content = {}, fileBody = {}",content,fileBody);

                            // File open
                            var file = new File(rootPath+File.separator+userInfo.id()+File.separator+UUID.randomUUID()+extension);
                            fos = new FileOutputStream(file);
                        }

                        sb = new StringBuilder();
                    } else if (buffer[i] != 10) {
                        sb.append((char) buffer[i]);
                    }


                    if (isBody && !fileBody) {
                        body.append((char) buffer[i]);
                    }

                }




                if (readSize < BUFFER_SIZE) {
                    break;
                }
            }

            if (fos != null) {
                fos.close();
            }
            log.debug("[Total Content]  {} ",totalContent);


        } catch (IOException exception) {
            log.error("[Socket Error] : Post Multipart 데이터를 읽어오던 중 에러 발생");
            throw ServerErrorCode.INTERNAL_SERVER_ERROR.exception();
        }
    }

}
