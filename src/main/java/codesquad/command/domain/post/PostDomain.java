package codesquad.command.domain.post;

import codesquad.command.annotation.custom.RequestParam;
import codesquad.command.annotation.method.Command;
import codesquad.command.annotation.method.GetMapping;
import codesquad.command.annotation.method.PostMapping;
import codesquad.command.annotation.preprocess.PreHandle;
import codesquad.command.domain.DynamicResponseBody;
import codesquad.command.domainResponse.HttpClientRequest;
import codesquad.command.interceptor.AuthHandler;
import codesquad.db.post.Post;
import codesquad.db.post.PostRepository;
import codesquad.db.user.Member;
import codesquad.db.user.MemberRepository;
import codesquad.http.HttpStatus;
import codesquad.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command
public class PostDomain {
    private static final Logger log = LoggerFactory.getLogger(PostDomain.class);
    private static final PostDomain boardDomain = new PostDomain();

    private PostDomain() {
    }

    public static PostDomain getInstance() {
        return boardDomain;
    }

    @PreHandle(target = AuthHandler.class)
    @GetMapping(httpStatus = HttpStatus.OK, path = "/article")
    public String getPostPage(HttpClientRequest request) {
        log.info("[GET] /article 호출");
        return DynamicResponseBody.getInstance().getHtmlFile("/article/write.html", request.getUserInfo());
    }

    @PreHandle(target = AuthHandler.class)
    @PostMapping(httpStatus = HttpStatus.OK, path = "/post/create")
    public String createPost(@RequestParam(name = "title") String postTitle, @RequestParam(name = "content") String postContent, HttpClientRequest request) {
        log.info("[POST] /create/post 호출");

        var userInfo = request.getUserInfo();
        var post = new Post(postTitle, postContent, userInfo.id());
        PostRepository.getInstance().save(post);

        return DynamicResponseBody.getInstance().getHtmlFile("/main/index.html", userInfo);
    }

    @GetMapping(httpStatus = HttpStatus.OK, path = "/post")
    public String getPost(@RequestParam(name = "id") long postId, HttpClientRequest request) {
        log.info("[POST] /post 호출");

        Post post = PostRepository.getInstance().findByPk(postId);
        Member member = MemberRepository.getInstance().findByPk(post.getUserId());

        return DynamicResponseBody.getInstance().getPostHtml("/article/post.html", request.getUserInfo(), post,member);
    }
}
