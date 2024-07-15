package codesquad.db.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemberRepositoryTest {

	MemberRepository repository = new MemberRepository();

	@Nested
	@DisplayName("사용자 CRUD 테스트")
	class MemberCRUDTest {

		@Test
		@DisplayName("사용자의 정보가 알맞게 입력되면 정상 생성된다.")
		void request_with_valid_user_info() {
			Member member = new Member("test", "testPassword", "testName", "test@test");

			Member saveMember = repository.save(member);

			assertEquals(member,saveMember);
		}
	}

}