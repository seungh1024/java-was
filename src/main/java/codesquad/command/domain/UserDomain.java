package codesquad.command.domain;

import codesquad.command.methodannotation.Command;
import codesquad.command.methodannotation.GetMapping;

@Command
public class UserDomain {
	private static final UserDomain userDomain = new UserDomain();
	private UserDomain(){}

	public static UserDomain getInstance() {
		return userDomain;
	}

	@GetMapping(path = "/create")
	public void createUser() {
		System.out.println("createUser success!");

	}
}
