package codesquad.command.domain;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.RecordComponent;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import codesquad.command.annotation.RequestParam;
import codesquad.command.annotation.method.Command;
import codesquad.command.annotation.method.PostMapping;
import codesquad.command.annotation.redirect.Redirect;
import codesquad.command.domainResponse.HttpClientResponse;
import codesquad.command.model.User;
import codesquad.command.model.UserInfo;
import codesquad.exception.CustomException;
import codesquad.exception.client.ClientErrorCode;
import codesquad.http.HttpStatus;

import static codesquad.util.StringSeparator.*;

@Command
public class UserDomain {
	private static final UserDomain userDomain = new UserDomain();
	private UserDomain(){}

	public static UserDomain getInstance() {
		return userDomain;
	}

	@Redirect(redirection = "/index.html")
	@PostMapping(httpStatus = HttpStatus.FOUND, path = "/user/create")
	public UserInfo createUser(@RequestParam(name = "userId") String userId, @RequestParam(name = "password")String password, @RequestParam(name = "name")String name, @RequestParam(name = "email")String email) {
		var userInfo = new UserInfo(userId, password, name, email);
		User.getInstance().addUserInfo(userInfo);

		return User.getInstance().getUserInfo(userId);
	}

	@PostMapping(httpStatus = HttpStatus.OK, path = "/user/login")
	public void login(@RequestParam(name = "userId") String userId, @RequestParam(name = "password") String password, HttpClientResponse response) {
		
	}

}
