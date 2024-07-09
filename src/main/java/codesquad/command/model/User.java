package codesquad.command.model;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import codesquad.exception.client.ClientErrorCode;

public class User {
	private static final User user = new User();
	private static Map<String, UserInfo> userData = new ConcurrentHashMap<>();

	private User(){};

	public static User getInstance() {
		return user;
	}

	public void addUserInfo(UserInfo userInfo) {
		UserInfo value = userData.putIfAbsent(userInfo.userId(), userInfo);

		// null이 아니면 기존에 값이 있다는 것을 의미
		if (!Objects.isNull(value)) {
			throw ClientErrorCode.USERID_ALREADY_EXISTS.exception();
		}

	}

	public UserInfo getUserInfo(String userId) {
		return userData.get(userId);
	}
}
