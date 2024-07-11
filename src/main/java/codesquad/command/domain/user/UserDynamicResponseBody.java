package codesquad.command.domain.user;

import codesquad.command.model.UserInfo;
import codesquad.exception.client.ClientErrorCode;
import codesquad.file.FileReader;
import codesquad.session.SessionUserInfo;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public class UserDynamicResponseBody {
    private static final UserDynamicResponseBody userDynamicResponseBody = new UserDynamicResponseBody();
    private final String rootPath;

    private static final int BUFFER_SIZE = 4*1024;
    private static final String userDefault = "<li class=\"header__menu__item\">\n" +
            "<a class=\"btn btn_contained btn_size_s\" href=\"/login/index.html\">로그인</a>\n" +
            "</li>\n" +
            "<li class=\"header__menu__item\">\n" +
            "<a class=\"btn btn_ghost btn_size_s\" href=\"/registration/index.html\">\n" +
            "    회원 가입\n" +
            "</a>\n" +
            "</li>";

    private static final String userDynamic = "<li class=\"header__menu__item\">\n" +
            "            <a class=\"btn btn_contained btn_size_s\" href=\"/article\">글쓰기</a>\n" +
            "          </li>\n" +
            "          <li class=\"header__menu__item\">\n" +
            "            <form action=\"/user/logout\" method=\"POST\" class=\"form\" enctype=\"application/x-www-form-urlencoded\">\n" +
            "\n" +
            "              <button id=\"logout-btn\" class=\"btn btn_ghost btn_size_s\" type = \"submit\">\n" +
            "                로그아웃\n" +
            "              </button>\n" +
            "            </form>\n" +
            "          </li>";

    private UserDynamicResponseBody() {
        rootPath = System.getProperty("user.dir");
    }

    public static UserDynamicResponseBody getInstance() {
        return userDynamicResponseBody;
    }

    public String getMainHtml(String uri , SessionUserInfo sessionUserInfo) {
        var headerHtml = getHeaderHtml(sessionUserInfo);
        var html = getHtmlBody(uri);

        return html.replace("{{dynamicButton}}", headerHtml);
    }

    public String getUserListHtml(String uri ,SessionUserInfo sessionUserInfo, List<UserInfo> userInfoList) {
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
        for (UserInfo userInfo : userInfoList) {
            sb.append("<tr>\n")
                    .append("<td>").append(userInfo.userId()).append("</td>")
                    .append("<td>").append(userInfo.name()).append("</td>")
                    .append("<td>").append(userInfo.email()).append("</td>")
                    .append("</tr>\n");
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

        var html = new String(FileReader.getInstance().readFileWithByte(inputStream));

        return html;
    }
}
