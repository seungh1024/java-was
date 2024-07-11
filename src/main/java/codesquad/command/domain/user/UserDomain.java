package codesquad.command.domain.user;

import java.util.List;
import java.util.Objects;

import codesquad.command.annotation.custom.RequestParam;
import codesquad.command.annotation.method.Command;
import codesquad.command.annotation.method.GetMapping;
import codesquad.command.annotation.method.PostMapping;
import codesquad.command.annotation.preprocess.PreHandle;
import codesquad.command.annotation.redirect.Redirect;
import codesquad.command.domainResponse.HttpClientRequest;
import codesquad.command.domainResponse.HttpClientResponse;
import codesquad.command.interceptor.AuthHandler;
import codesquad.command.model.User;
import codesquad.command.model.UserInfo;
import codesquad.exception.client.ClientErrorCode;
import codesquad.http.HttpStatus;
import codesquad.session.Session;
import codesquad.session.SessionUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Command
public class UserDomain {
	private static final Logger log = LoggerFactory.getLogger(UserDomain.class);
	private static final UserDomain userDomain = new UserDomain();

	private UserDomain() {

	}

	public static UserDomain getInstance() {
		return userDomain;
	}

	@Redirect(redirection = "/index.html")
	@PostMapping(httpStatus = HttpStatus.FOUND, path = "/user/create")
	public UserInfo createUser(@RequestParam(name = "userId") String userId, @RequestParam(name = "password") String password, @RequestParam(name = "name") String name, @RequestParam(name = "email") String email) {

		var userInfo = new UserInfo(userId, password, name, email);
		User.getInstance().addUserInfo(userInfo);

		return User.getInstance().getUserInfo(userId);
	}

	@Redirect(redirection = "/main/index.html")
	@PostMapping(httpStatus = HttpStatus.FOUND, path = "/user/login")
	public void login(@RequestParam(name = "userId") String userId, @RequestParam(name = "password") String password, HttpClientResponse response) {

		UserInfo userInfo = User.getInstance().getUserInfo(userId);
		if (Objects.isNull(userInfo)) {
			throw ClientErrorCode.USER_NOT_FOUND.exception();
		}
		if (!Objects.equals(userInfo.password(), password)) {
			log.debug("[Login] Invalid username or password");
			return;
		}

		String sessionKey = Session.getInstance().setSession(new SessionUserInfo(userInfo.userId(), userInfo.name()));
		response.setCookie("sessionKey", sessionKey);
		log.debug("[Login] Login successful");

	}

	@Redirect(redirection = "/index.html")
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
		SessionUserInfo sessionUserInfo = Session.getInstance().getSession(sessionKey.value());
		var userList = User.getInstance().getUserList();
		return UserDynamicResponseBody.getInstance().getUserListHtml("/dynamic/userList.html", sessionUserInfo, userList);
	}


}
