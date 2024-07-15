package codesquad.command.domain.board;

import codesquad.command.annotation.method.Command;
import codesquad.command.annotation.method.GetMapping;
import codesquad.command.annotation.preprocess.PreHandle;
import codesquad.command.domain.DynamicResponseBody;
import codesquad.command.domainResponse.HttpClientRequest;
import codesquad.command.interceptor.AuthHandler;
import codesquad.http.HttpStatus;
import codesquad.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command
public class BoardDomain {
    private static final Logger log = LoggerFactory.getLogger(BoardDomain.class);
    private static final BoardDomain boardDomain = new BoardDomain();

    private BoardDomain() {
    }

    public static BoardDomain getInstance(){
        return boardDomain;
    }

    @PreHandle(target = AuthHandler.class)
    @GetMapping(httpStatus = HttpStatus.OK, path = "/article")
    public String getBoardPage(HttpClientRequest request) {
        log.info("[GET] /article 호출");
        var sessionKey = request.getCookie("sessionKey");
        var sessionUserInfo = Session.getInstance().getSession(sessionKey.value());
        return DynamicResponseBody.getInstance().getHtmlFile("/article/write.html",sessionUserInfo);
    }
}
