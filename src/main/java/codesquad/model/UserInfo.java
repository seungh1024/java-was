package codesquad.model;

public record UserInfo(
	String userId,
	String password,
	String name,
	String email
) {
}
