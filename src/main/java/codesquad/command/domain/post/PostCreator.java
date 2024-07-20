package codesquad.command.domain.post;

import codesquad.command.domainReqRes.HttpClientRequest;
import codesquad.exception.client.ClientErrorCode;
import codesquad.exception.server.ServerErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PostCreator {
    private static final Logger log = LoggerFactory.getLogger(PostCreator.class);
    private static final int BUFFER_SIZE = 1024*4;
    private static final int MAX_READ_SIZE = 100*1024*1024;
    private static final PostCreator postCreator = new PostCreator();
    private static final String rootPath = System.getProperty("user.home")+File.separator+"post";
    private PostCreator() {}

    public static PostCreator getInstance() {
        return postCreator;
    }

    public Map<String,String> save(HttpClientRequest request) {
        var readSize = 0;
        var inputSize = 0;
        var buffer = new byte[BUFFER_SIZE];
        var inputStream = request.getInputStream();
        var boundary = request.getBoundary();


        var userInfo = request.getUserInfo();
        var directory = new File(rootPath + File.separator + userInfo.id());
        log.info("[Directory Path] {}", directory.getPath());
        if(!directory.exists() && Objects.nonNull(boundary)) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw ServerErrorCode.CANNOT_CREATE_POST.exception();
            }
        }



        var lineBuffer = new ByteArrayOutputStream();
        var totalContent = new HashMap<String, String>(); // 전체 컨텐츠 저장
        var content = new HashMap<String, String>(); // 컨텐츠 저장. 이름, 값, body, 파일 경로 등

        try {
            boolean isFile = false;
            var fileBuffer = new byte[BUFFER_SIZE];
            var fileIndex = 0;
            boolean isBody = false;

            File file = null;
            FileOutputStream fos = null;
            var bos = new ByteArrayOutputStream();
            var fileBos = new ByteArrayOutputStream();
            var ai = new AtomicInteger(0);

            while ((readSize = inputStream.read(buffer)) != -1) {
                inputSize += readSize;
                if (inputSize > MAX_READ_SIZE) {
                    if (fos != null) {
                        fos.close();
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                    throw ClientErrorCode.TOO_MUCH_DATA.exception();
                }

                for (int i = 0; i < readSize; i++) {


                        if (isFile) {
                            fileBos.write(buffer[i]);
                            if (fileBos.size() == BUFFER_SIZE) {
                                ai.getAndIncrement();
                                PostFileWriter.getInstance().writeBuffer(fos, fileBos.toByteArray(), 0, BUFFER_SIZE,ai);
                                fileBos.reset();
                            }
                        } else if(isBody) {
                            bos.write(buffer[i]);
                        }



                    if (buffer[i] == 10) {
                        var line = lineBuffer.toString();

                        if (lineBuffer.size()==0 && !isBody &&!isFile) {
                            if (content.get("type") != null) {
                                isFile = true;
                                isBody = false;
                            } else {
                                isBody = true;
                                isFile = false;
                            }
                            continue;
                        }

                        if (line.contains(boundary)) {
                            if (isFile) {
                                if (fileBos.size() - boundary.length() > 0) {
                                    ai.getAndIncrement();
                                    PostFileWriter.getInstance()
                                        .writeBuffer(fos, fileBos.toByteArray(), 0, fileBos.size() - boundary.length(),ai);
                                    fileBos.reset();
                                }

                                var fileName = content.get("filename");
                                var filePath =  content.get("filePath");
                                var fileType = content.get("type");
                                totalContent.put("fileName", fileName);
                                totalContent.put("filePath", filePath);
                                totalContent.put("type", fileType);
                            } else if (isBody) {
                                var bs = bos.toString();
                                bs = bs.replaceAll("\r", "");
                                var bodyInfos = bs.split("\n\n" + boundary);
                                var body = bodyInfos[0];
                                var contentName = content.get("name");
                                totalContent.put(contentName,body);
                            }
                            isBody = false;
                            isFile = false;
                            bos.reset();
                            content.clear();

                            log.debug("[Line End] total content = {}",totalContent);
                        } else if (line.contains("Content-Disposition: ")) {
                            log.debug("[Content Disposition] {}",line);
                            line = line.replace("ContentDisposition: ", "");
                            var dispositions = line.split("; ");
                            for (var disposition : dispositions) {
                                if (disposition.contains("=")) {
                                    var dispositionKeyValue = disposition.split("=");
                                    var key = dispositionKeyValue[0];
                                    var value = dispositionKeyValue[1].replaceAll("\"","");
                                    content.put(key, value);
                                }
                            }

                            var fileName = content.get("filename");
                            if (fileName != null) {
                                var extension = fileName.substring(fileName.lastIndexOf("."));
                                content.put("extension", extension);
                            }

                            log.debug("[Content Info] {}",content);
                        } else if (line.contains("Content-Type: ")) {
                            log.debug("[Content Type] {}", line);
                            line = line.replace("Content-Type: ", "");
                            content.put("type", line);
                            // var extension = line.substring(line.lastIndexOf("/")).replace("/",".");
                            // content.put("extension", extension);
                            // isFile = true;
                            isBody = false;

                            file = new File(
                                rootPath + File.separator + userInfo.id() + File.separator + UUID.randomUUID()
                                    + content.get("extension"));
                            content.put("filePath", file.getPath());
                            fos = new FileOutputStream(file);
                        } else {
                            bos.write(buffer[i]);
                        }
                        lineBuffer.reset();
                    } else if (buffer[i] != 13) {
                        lineBuffer.write(buffer[i]);
                    }
                }
                if (readSize < BUFFER_SIZE) {
                    break;
                }
            }

            log.debug("[Total Content]  {} ",totalContent);


        } catch (IOException exception) {
            log.error("[Socket Error] : Post Multipart 데이터를 읽어오던 중 에러 발생");
            throw ServerErrorCode.INTERNAL_SERVER_ERROR.exception();
        }

        return totalContent;
    }

}
