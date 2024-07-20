package codesquad.command.domain.post;

import codesquad.command.domain.DynamicResponseBody;
import codesquad.command.domainReqRes.HttpClientRequest;
import codesquad.db.post.Post;
import codesquad.db.post.PostRepository;
import codesquad.db.user.Member;
import codesquad.db.user.MemberRepository;
import codesquad.http.request.format.HttpMethod;
import codesquad.http.request.format.HttpRequest;
import codesquad.session.SessionUserInfo;
import codesquad.util.FileExtension;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PostDomainTest {

    String userId = "testId";
    String password = "password";
    String userName = "testName";
    String userEmail = "testEmail@naver.com";

    Member member = null;
    HttpClientRequest request = null;

    File file;
    File outFile;
    FileInputStream fis;
    FileOutputStream fos;

    @BeforeEach
    void setUp() throws IOException {
        member = new Member("loginTest", "password", "name", "email");
        Member findMember = MemberRepository.getInstance().findById(member.getMemberId());
        if (findMember != null) {
            MemberRepository.getInstance().delete(findMember);
        }
        member = MemberRepository.getInstance().save(member);
        file = new File("test.png");
        if (!file.exists()) {
            file.createNewFile();
        }
        fis = new FileInputStream(file);

        outFile = new File("testOut.png");
        if (!outFile.exists()) {
            outFile.createNewFile();
        }
        fos = new FileOutputStream(outFile);
        request = new HttpClientRequest(
                new HttpRequest(HttpMethod.POST, "testUri", FileExtension.HTML, "http 1.1", Map.of(),
                        Map.of(), "body",null,0,fis,fos));
        var userInfo = new SessionUserInfo(member.getId(), member.getMemberId(), member.getName());
        request.setUserInfo(userInfo);
    }

    @AfterEach
    void shutDown() throws IOException {
        if (file.exists()) {
            file.delete();
        }
        if (outFile.exists()) {
            outFile.delete();
        }

        if (fis != null) {
            fis.close();
        }
        if (fos != null) {
            fos.close();
        }
    }
    @Nested
    @DisplayName("게시글 페이지 요청 테스트")
    class GetPostPageTest {

        @Test
        @DisplayName("로그인한 사용자는 article 페이지를 응답받는다")
        void request_with_login_user() {
            // when
            String postPage = PostDomain.getInstance().getPostPage(request);

            // then
            String htmlFile = DynamicResponseBody.getInstance().getHtmlFile("/article/write.html", request.getUserInfo());
            assertEquals(htmlFile, postPage);
        }
    }

    @Nested
    @DisplayName("게시글 생성 테스트")
    class CreatePostTest {

        @Test
        @DisplayName("게시글 생성을 성공하면 /main/index.html 페이지를 응답받는다.")
        void request_create_post_return_main_page() {
            // given
            var postTitle = "testTitle";
            var postContent = "testContent";

            // when
            String post = PostDomain.getInstance().createPost(request);

            // then
            String htmlFile = DynamicResponseBody.getInstance().getHtmlFile("/main/index.html", request.getUserInfo());
            assertEquals(htmlFile, post);
        }

        @Test
        @DisplayName("post 상세 조회를 하면 게시글 내용을 보여준다.")
        void request_with_post_info() {
            // given
            var postTitle = "testTitle";
            var postContent = "testContent";
            Post post = new Post(postTitle, postContent, member.getId());
            Post savePost = PostRepository.getInstance().save(post);
            var findMemeber = MemberRepository.getInstance().findByPk(savePost.getUserId());

            // when
            String findPost = PostDomain.getInstance().getPost(savePost.getId(), request);

            // then
            String postHtml = DynamicResponseBody.getInstance().getPostHtml("/article/post.html", request.getUserInfo(), savePost, findMemeber);
            assertEquals(postHtml, findPost);
        }
    }
}