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
		if (!Objects.isNull(userData.get(userInfo.userId()))) {
			throw ClientErrorCode.USERID_ALREADY_EXISTS.exception();
		}

		userData.compute(userInfo.userId(), (k, v) -> v == null ? userInfo : v); // null일 때만 userInfo를 넣어준다.
		userData.put(userInfo.userId(), userInfo);
	}

	public UserInfo getUserInfo(String userId) {
		return userData.get(userId);
	}
}
