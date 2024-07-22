package codesquad.db.user;

import ch.qos.logback.core.db.dialect.DBUtil;
import codesquad.exception.CustomException;
import codesquad.exception.client.ClientErrorCode;
import codesquad.session.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class MemberRepositoryTest extends DBUtil {

	MemberRepository memberRepository = MemberRepository.getInstance();

	String userId = "testId";
	String password = "password";
	String userName = "testName";
	String userEmail = "testEmail@naver.com";

	@BeforeEach
	void init() {
		Session.getInstance().removeSession("value");

		Member member = MemberRepository.getInstance().findById(userId);
		if (member != null) {
			MemberRepository.getInstance().delete(member);
		} else {
			MemberRepository.getInstance().save(new Member(userId, password, userName, userEmail));
		}
	}


	@Nested
	@DisplayName("사용자 CRUD 테스트")
	class MemberCRUDTest {

		@Test
		@DisplayName("사용자의 정보가 알맞게 입력되면 정상 생성된다.")
		void request_with_valid_user_info() {
			//given
			Member member = new Member(userId, password, userName, userEmail);

			//when
			Member saveMember = memberRepository.save(member);

			//then
			Member findMember = memberRepository.findById(userId);
			assertEquals(findMember, saveMember);
		}

		@Test
		@DisplayName("사용자의 정보가 중복되면 예외가 발생한다.")
		void request_with_duplicated_user_id() {
			//given
			Member member = new Member(userId, password, userName, userEmail);
			memberRepository.save(member);

			//when
			CustomException customException = assertThrows(CustomException.class, () -> {
				memberRepository.save(member);
			});

			// then
			assertEquals(ClientErrorCode.USERID_ALREADY_EXISTS.exception().getErrorName(), customException.getErrorName());
			assertEquals(ClientErrorCode.USERID_ALREADY_EXISTS.exception().getStatusCode(), customException.getStatusCode());
		}

		@Test
		@DisplayName("사용자 리스트 조회 테스트")
		void request_with_get_member_list() {
			// given
			Member member1 = new Member(userId, password, userName, userEmail);
			Member member2 = new Member(userId + 1, password, userName, userEmail);
			memberRepository.delete(member1);
			memberRepository.delete(member2);

			// when
			memberRepository.save(member1);
			memberRepository.save(member2);

			// then
			List<Member> memberList = memberRepository.findMemberList();
			int count = 0;
			for (Member member : memberList) {
				if (Objects.equals(member1, member) || Objects.equals(member2, member)) {
					count++;
				}
			}

			assertEquals(2, count);
		}

		@Test
		@DisplayName("사용자 정보 삭제 테스트")
		void request_with_delete_member() {
			// given
			Member member = new Member(userId, password, userName, userEmail);
			memberRepository.delete(member);
			memberRepository.save(member);

			// when
			memberRepository.delete(member);

			// then
			Member findMember = memberRepository.findById(userId);
			assertNull(findMember);
		}
	}

}