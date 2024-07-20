package codesquad.command.domain.main;

import codesquad.command.annotation.method.Command;
import codesquad.command.annotation.method.GetMapping;
import codesquad.command.domain.DynamicResponseBody;
import codesquad.command.domainReqRes.HttpClientRequest;
import codesquad.http.HttpStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command
public class MainDomain {
    private static final Logger log = LoggerFactory.getLogger(MainDomain.class);
    private static final MainDomain mainDomain = new MainDomain();
    private MainDomain(){}

    public static MainDomain getInstance() {
        return mainDomain;
    }

    @GetMapping(httpStatus = HttpStatus.OK, path = "/")
    public String getMainPage(HttpClientRequest request) {
        log.info("[GET] / 호출");
        return DynamicResponseBody.getInstance().getHtmlFile("/main/index.html", request.getUserInfo());
    }

    @GetMapping(httpStatus = HttpStatus.OK, path = "/main")
    public String getLoginMainPage(HttpClientRequest request) {
        log.info("[GET] /main 호출");
        return DynamicResponseBody.getInstance().getHtmlFile("/main/index.html",request.getUserInfo());
    }
}
