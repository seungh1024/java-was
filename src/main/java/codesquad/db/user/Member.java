package codesquad.db.user;

import java.util.Objects;

import codesquad.db.XSSUtil;
import codesquad.exception.client.ClientErrorCode;

public class Member {
	private long id;
	private String memberId;
	private String password;
	private String name;
	private String email;

	public Member(){}

	public Member(String userId, String password, String name, String email) {
		this.memberId = userId;
		this.password = password;
		this.name = name;
		this.email = email;
	}

	public void isValid() {
		if (Objects.isNull(memberId) || Objects.isNull(password) || Objects.isNull(name) || Objects.isNull(email)) {
			throw ClientErrorCode.INVALID_ARGUMENT.exception();
		}
		XSSUtil.list.stream()
			.forEach(s -> {
				if (memberId.contains(s) || name.contains(s) || email.contains(s)) {
					throw ClientErrorCode.INVALID_ARGUMENT.exception();
				}
			});
	}

	public long getId() {
		return id;
	}

	public String getMemberId() {
		return memberId;
	}

	public String getPassword() {
		return password;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "id = "+id + ", memeberId = "+memberId+ ", password = "+password + ", name = "+name + ", email = "+email;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Member member = (Member) o;
		return Objects.equals(memberId, member.memberId) && Objects.equals(password, member.password) && Objects.equals(name, member.name) && Objects.equals(email, member.email);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, memberId, password, name, email);
	}
}
