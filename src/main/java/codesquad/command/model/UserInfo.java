package codesquad.command.model;

public record UserInfo(
	String userId,
	String name,
	String password,
	String email
) {
}
