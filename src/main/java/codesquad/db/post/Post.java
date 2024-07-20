package codesquad.db.post;

import java.util.Objects;

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

    public void isValid() {
        if (Objects.isNull(title) || Objects.isNull(content) || Objects.isNull(userId) || fileName.length() > 100 || filePath.length() > 100 || title.length() > 100) {
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
                ", post='" + content + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post1 = (Post) o;
        return Objects.equals(title, post1.title) && Objects.equals(content, post1.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, content);
    }
}
