package codesquad.command.domain.user;

import java.util.Objects;

import codesquad.command.annotation.custom.RequestParam;
import codesquad.command.annotation.method.Command;
import codesquad.command.annotation.method.GetMapping;
import codesquad.command.annotation.method.PostMapping;
import codesquad.command.annotation.preprocess.PreHandle;
import codesquad.command.annotation.redirect.Redirect;
import codesquad.command.domain.DynamicResponseBody;
import codesquad.command.domainResponse.HttpClientRequest;
import codesquad.command.domainResponse.HttpClientResponse;
import codesquad.command.interceptor.AuthHandler;
import codesquad.db.user.MemberRepository;
import codesquad.db.user.Member;
import codesquad.exception.client.ClientErrorCode;
import codesquad.http.HttpStatus;
import codesquad.session.Session;
import codesquad.session.SessionUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Command
public class MemberDomain {
	private static final Logger log = LoggerFactory.getLogger(MemberDomain.class);
	private static final MemberDomain userDomain = new MemberDomain();

	private MemberDomain() {

	}

	public static MemberDomain getInstance() {
		return userDomain;
	}

	@Redirect(redirection = "/")
	@PostMapping(httpStatus = HttpStatus.FOUND, path = "/user/create")
	public Member createUser(@RequestParam(name = "userId") String userId, @RequestParam(name = "password") String password, @RequestParam(name = "name") String name, @RequestParam(name = "email") String email) {
		log.info("[Create User] create user start");
		var userInfo = new Member(userId, password, name, email);
		return MemberRepository.getInstance().save(userInfo);
	}

	@Redirect(redirection = "/main")
	@PostMapping(httpStatus = HttpStatus.FOUND, path = "/user/login")
	public void login(@RequestParam(name = "userId") String userId, @RequestParam(name = "password") String password, HttpClientResponse response) {

		 Member member = MemberRepository.getInstance().findById(userId);
		 if (Objects.isNull(member)) {
		 	throw ClientErrorCode.USER_NOT_FOUND.exception();
		 }
		 if (!Objects.equals(member.getPassword(), password)) {
		 	log.debug("[Login] Invalid username or password");
		 	throw  ClientErrorCode.INVALID_PASSWORD.exception();
		 }

		 String sessionKey = Session.getInstance().setSession(new SessionUserInfo(member.getId(),member.getMemberId(), member.getName()));
		 response.setCookie("sessionKey", sessionKey);
		 log.debug("[Login] Login successful");

	}

	@Redirect(redirection = "/")
	@PostMapping(httpStatus = HttpStatus.FOUND, path = "/user/logout")
	public void logout(HttpClientRequest request, HttpClientResponse response) {
		var cookie = request.getCookie("sessionKey");
		if (!Objects.isNull(cookie)) {
			Session.getInstance().removeSession(cookie.value());
			response.setCookie("sessionKey", "");
			response.setMaxAge(cookie.key(), 0);
		}
	}

	@PreHandle(target = AuthHandler.class)
	@GetMapping(httpStatus = HttpStatus.OK, path = "/user/list")
	public String getUserList(HttpClientRequest request) {
		var sessionKey = request.getCookie("sessionKey");
		var sessionUserInfo = Session.getInstance().getSession(sessionKey.value());
		var userList = MemberRepository.getInstance().findMemberList();

		return DynamicResponseBody.getInstance().getUserListHtml("/dynamic/userList.html", sessionUserInfo, userList);
	}


	@GetMapping(httpStatus = HttpStatus.OK, path = "/registration")
	public String getRegistrationHtml() {
		return DynamicResponseBody.getInstance().getHtmlFile("/registration/index.html", null);
	}

	@GetMapping(httpStatus = HttpStatus.OK, path = "/login")
	public String getLoginHtml() {
		return DynamicResponseBody.getInstance().getHtmlFile("/login/index.html", null);
	}
}
