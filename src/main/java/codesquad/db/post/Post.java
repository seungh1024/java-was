package codesquad.db.post;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import codesquad.db.XSSUtil;
import codesquad.exception.client.ClientErrorCode;

public class Post {
    private long id;
    private String title;
    private String content;
    private long userId;
    private String fileName;
    private String filePath;

    public Post(){}

    public Post(String title, String content, long userId) {
        this.title = title;
        this.content = content;
        this.userId = userId;
    }

    public Post(String title, String content, long userId, String fileName, String filePath) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public void characterChange() {
        XSSUtil.list.stream()
            .forEach(s -> {
                if (title.contains(s)) {
                    title =title.replace(s,XSSUtil.map.get(s));
                }
                if (content.contains(s)) {
                    System.out.println("s = "+s);
                    content =content.replace(s,XSSUtil.map.get(s));
                }
                if (fileName != null && fileName.contains(s)) {
                    fileName =fileName.replace(s,XSSUtil.map.get(s));
                }
                if (filePath != null && filePath.contains(s)) {
                    filePath = filePath.replace(s,XSSUtil.map.get(s));
                }
            });
    }

    public void isValid() {
        if (Objects.isNull(title) || Objects.isNull(content) || Objects.isNull(userId) || (fileName!= null && fileName.length() > 100) || (filePath != null && filePath.length() > 100) || title.length() > 100) {
            throw ClientErrorCode.INVALID_ARGUMENT.exception();
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public long getUserId() {
        return userId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Post{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", content='" + content + '\'' +
            ", userId=" + userId +
            ", fileName='" + fileName + '\'' +
            ", filePath='" + filePath + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Post post = (Post)o;
        return getId() == post.getId() && getUserId() == post.getUserId() && Objects.equals(getTitle(),
            post.getTitle()) && Objects.equals(getContent(), post.getContent()) && Objects.equals(
            getFileName(), post.getFileName()) && Objects.equals(getFilePath(), post.getFilePath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getContent(), getUserId(), getFileName(), getFilePath());
    }
}
