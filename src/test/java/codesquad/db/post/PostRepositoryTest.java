package codesquad.db.post;

import codesquad.db.user.Member;
import codesquad.db.user.MemberRepository;
import codesquad.session.SessionUserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class PostRepositoryTest {

    MemberRepository memberRepository = MemberRepository.getInstance();
    PostRepository postRepository = PostRepository.getInstance();

    String postTitle = "testPostTitle";
    String postContent = "testPostContent";
    String userId = "testId";
    long postUserId;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.findById(userId);
        if (Objects.nonNull(member)) {
            postUserId = member.getId();
        } else {
            memberRepository.save(new Member(userId, null, null, null));
            Member findMember = memberRepository.findById(userId);
            postUserId = findMember.getId();
        }

        Post post = new Post(postTitle, postContent, postUserId);
        postRepository.delete(post);
    }


    @Nested
    @DisplayName("Post CRUD 테스트")
    class PostCRUDTest {

        @Test
        @DisplayName("post 생성")
        void request_with_post_create() {
            //given
            Post post = new Post(postTitle, postContent, postUserId);
            SessionUserInfo sessionUserInfo = new SessionUserInfo(postUserId, null, null);

            //when
            long pk = postRepository.save(post);

            // then
            Post findPost = postRepository.findByPk(pk);
            postRepository.delete(findPost);

            assertNotNull(findPost);
        }

        @Test
        @DisplayName("post 삭제 테스트")
        void request_with_post_delete() {
            //given
            Post post = new Post(postTitle, postContent, postUserId);
            SessionUserInfo sessionUserInfo = new SessionUserInfo(postUserId, null, null);

            //when
            long pk = postRepository.save(post);
            Post findPost = postRepository.findByPk(pk);
            postRepository.delete(findPost);

            // then
            Post deletedPost = postRepository.findByPk(pk);

            assertNull(deletedPost);
        }

    }
}