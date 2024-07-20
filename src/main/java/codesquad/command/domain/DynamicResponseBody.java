package codesquad.command.domain;

import codesquad.db.post.Post;
import codesquad.db.post.PostAndMember;
import codesquad.db.post.PostRepository;
import codesquad.db.user.Member;
import codesquad.exception.client.ClientErrorCode;
import codesquad.file.CustomFileReader;
import codesquad.session.SessionUserInfo;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicResponseBody {
    private static final DynamicResponseBody responseBody = new DynamicResponseBody();
    private static final PostRepository postRepository = PostRepository.getInstance();

    private static final String userDefault = "<li class=\"header__menu__item\">\n" +
            "<a class=\"btn btn_contained btn_size_s\" href=\"/login\">로그인</a>\n" +
            "</li>\n" +
            "<li class=\"header__menu__item\">\n" +
            "<a class=\"btn btn_ghost btn_size_s\" href=\"/registration\">\n" +
            "    회원 가입\n" +
            "</a>\n" +
            "</li>";

    private static final String userDynamic = "<li class=\"header__menu__item\">\n" +
        "            <a class=\"btn btn_contained btn_size_s\" href=\"/user/list\">사용자 리스트</a>\n" +
        "          </li>\n" + "<li class=\"header__menu__item\">\n" +
        "            <a class=\"btn btn_contained btn_size_s\" href=\"/article\">글쓰기</a>\n" +
        "          </li>\n" +
        "          <li class=\"header__menu__item\">\n" +
        "            <form action=\"/user/logout\" method=\"POST\" class=\"form\" enctype=\"application/x-www-form-urlencoded\">\n"
        +
        "\n" +
        "              <button id=\"logout-btn\" class=\"btn btn_ghost btn_size_s\" type = \"submit\">\n" +
        "                로그아웃\n" +
        "              </button>\n" +
        "            </form>\n" +
        "          </li>";
    private static final Logger log = LoggerFactory.getLogger(DynamicResponseBody.class);

    private DynamicResponseBody() {
    }

    public static DynamicResponseBody getInstance() {
        return responseBody;
    }


    public String getUserListHtml(String uri ,SessionUserInfo sessionUserInfo, List<Member> userInfoList) {
        var headerHtml = getHeaderHtml(sessionUserInfo);
        StringBuilder sb = new StringBuilder();
        sb.append("<table border=\"1\">").append("\n");
        sb.append("<thead>").append("\n");
        sb.append("<tr>").append("\n");
        sb.append("<th>아이디</th>").append("\n");
        sb.append("<th>이름</th>").append("\n");
        sb.append("<th>이메일</th>").append("\n");
        sb.append("</tr>").append("\n");
        sb.append("</thead>").append("\n");
        sb.append("<tbody>").append("\n");
        if (Objects.nonNull(userInfoList)) {
            for (Member userInfo : userInfoList) {
                sb.append("<tr>\n")
                    .append("<td>").append(userInfo.getMemberId()).append("</td>")
                    .append("<td>").append(userInfo.getName()).append("</td>")
                    .append("<td>").append(userInfo.getEmail()).append("</td>")
                    .append("</tr>\n");
            }
        }
        sb.append("</tbody>").append("\n");
        sb.append("</table>").append("\n");

        var html = getHtmlBody(uri);
        html = html.replace("{{dynamicButton}}", headerHtml);
        html = html.replace("{{userList}}", sb);
        return html;
    }

    private String getHeaderHtml(SessionUserInfo sessionUserInfo) {
        StringBuilder dynamicBody = new StringBuilder();

        if (Objects.nonNull(sessionUserInfo)) {
            dynamicBody.append("<li class=\"header__menu__item\">\n")
                    .append("<a class=\"btn btn_contained btn_size_s\">").append(sessionUserInfo.userName()).append(" 님").append("</a>\n").append(userDynamic);
        } else {
            dynamicBody.append(userDefault);
        }

        return dynamicBody.toString();
    }

    private String getHtmlBody(String uri) {
        ClassLoader classLoader = getClass().getClassLoader();
        var path = "static" + uri;

        URL resource = classLoader.getResource(path);
        InputStream inputStream = classLoader.getResourceAsStream(path);
        if (inputStream == null) {
            throw ClientErrorCode.NOT_FOUND.exception();
        }

        String fileName = resource.getFile();
        int index = fileName.lastIndexOf('.');
        StringBuilder sb = new StringBuilder();
        for (int i = index+1; i < fileName.length(); i++) {
            sb.append(fileName.charAt(i));
        }

        if (!Objects.equals(sb.toString().toUpperCase(), "HTML")) {
            throw ClientErrorCode.NOT_FOUND.exception();
        }

        var html = new String(CustomFileReader.getInstance().readFileWithByte(inputStream));

        return html;
    }


    public String getHtmlFile(String path, SessionUserInfo sessionUserInfo) {
        var totalPath = "static"+path;
        var inputStream = this.getClass().getClassLoader().getResourceAsStream(totalPath);
        var html = new String(CustomFileReader.getInstance().readFileWithByte(inputStream));
        var headerHtml = getHeaderHtml(sessionUserInfo);
        html = html.replace("{{dynamicButton}}", headerHtml);
        html = html.replace("{{postList}}",getPostListHtml());

        return html;
    }

    public String getPostListHtml() {
        List<PostAndMember> postList = postRepository.getPostList();
        if (Objects.isNull(postList)) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("<table border=\"1\">").append("\n");
            sb.append("<thead>").append("\n");
            sb.append("<tr>").append("\n");
            sb.append("<th>작성자</th>").append("\n");
            sb.append("<th>게시글 제목</th>").append("\n");
            sb.append("</tr>").append("\n");
            sb.append("</thead>").append("\n");
            sb.append("<tbody>").append("\n");
            for (PostAndMember pam : postList) {
                sb.append("<tr>\n")
                        .append("<td>").append(pam.memberName()).append("</td>")
                        .append("<td>").append("<a href=/post?id=").append(pam.id()).append(">").append(pam.title()).append("</td>")
                        .append("</tr>\n");
            }
            sb.append("</tbody>").append("\n");
            sb.append("</table>").append("\n");

            return sb.toString();
        }
    }

    public String getPostHtml(String path, SessionUserInfo sessionUserInfo, Post post, Member member) {
        var totalPath = "static"+path;
        var inputStream = this.getClass().getClassLoader().getResourceAsStream(totalPath);
        var html = new String(CustomFileReader.getInstance().readFileWithByte(inputStream));
        var headerHtml = getHeaderHtml(sessionUserInfo);
        html = html.replace("{{dynamicButton}}", headerHtml);
        html = html.replace("{{postTitle}}", post.getTitle());
        html = html.replace("{{memberName}}", member.getName());
        var imageName = post.getFileName();
        var imagePath = post.getFilePath();
        log.debug("[Post Info] {}",post);
        if (Objects.nonNull(imageName)) {
            var imageTag = "<img class=" + "post__img" + " src = \"" + "/post/image?filePath="+imagePath+ "\"/>";
            html = html.replace("{{postImage}}", imageTag);
        }
        html = html.replace("{{postContent}}", post.getContent());

        return html;
    }
}
