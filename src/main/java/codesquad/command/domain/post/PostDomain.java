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

    public static PostDomain getInstance(){
        return boardDomain;
    }

    @PreHandle(target = AuthHandler.class)
    @GetMapping(httpStatus = HttpStatus.OK, path = "/article")
    public String getPostPage(HttpClientRequest request) {
        log.info("[GET] /article 호출");
        var sessionKey = request.getCookie("sessionKey");
        var sessionUserInfo = Session.getInstance().getSession(sessionKey.value());
        return DynamicResponseBody.getInstance().getHtmlFile("/article/write.html",sessionUserInfo);
    }

    @PreHandle(target = AuthHandler.class)
    @PostMapping(httpStatus = HttpStatus.OK, path = "/post/create")
    public String createPost(@RequestParam(name = "title") String postTitle, @RequestParam(name = "content") String postContent, HttpClientRequest request) {
        log.info("[POST] /create/post 호출");
        var sessionKey = request.getCookie("sessionKey");
        var sessionUserInfo = Session.getInstance().getSession(sessionKey.value());

        var post = new Post(postTitle, postContent,sessionUserInfo.id());
        PostRepository.getInstance().save(post);

        return DynamicResponseBody.getInstance().getHtmlFile("/main/index.html", sessionUserInfo);
    }


}
