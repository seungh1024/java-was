package codesquad.command.domain;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import codesquad.command.methodannotation.Command;
import codesquad.command.methodannotation.GetMapping;
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

	@GetMapping(httpStatus = HttpStatus.CREATE, path = "/create")
	public UserInfo createUser(String queryParameter) {
		var parsingUserInfo = parsingResources(queryParameter);

		// System.out.println("parsingUserInfo = "+parsingUserInfo);

		RecordComponent[] recordComponents = UserInfo.class.getRecordComponents();

		if (parsingUserInfo.size() != recordComponents.length) {
			throw ClientErrorCode.PARAMETER_FORMAT_EXCEPTION.exception();
		}


		int idx = 0;
		for (List info : parsingUserInfo) {
			if (!Objects.equals(info.get(0), recordComponents[idx++].getName())) {
				throw ClientErrorCode.PARAMETER_FORMAT_EXCEPTION.exception();
			}
		}

		// System.out.println("?");

		var userId = parsingUserInfo.get(0).get(1);
		var password = parsingUserInfo.get(1).get(1);
		var name = parsingUserInfo.get(2).get(1);
		var email = parsingUserInfo.get(3).get(1);
		var userInfo = new UserInfo(userId, password, name, email);

		User.getInstance().addUserInfo(userInfo);
		// System.out.println("user map info = " + User.getInstance().getUserInfo(userId));
		return User.getInstance().getUserInfo(userId);
	}

	public List<List<String>> parsingResources(String resources) {
		var userData = resources.split(QUERY_PARAMETER_SEPARATOR);
		// System.out.println(Arrays.toString(userData));
		var parsingUserInfo = new ArrayList<List<String>>();


		try {
			for (String data : userData) {
				var info = data.split(EQUAL_SEPARATOR);
				if (info.length != 2) {
					throw ClientErrorCode.PARAMETER_FORMAT_EXCEPTION.exception();
				}
				parsingUserInfo.add(List.of(info[0], URLDecoder.decode(info[1], "UTF-8")));
			}
		}catch (CustomException exception){
			throw exception;
		} catch (UnsupportedEncodingException exception) {
			throw new RuntimeException(exception);
		}

		return parsingUserInfo;
	}
}
