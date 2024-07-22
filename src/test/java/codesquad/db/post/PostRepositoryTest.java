package codesquad.db.post;

import ch.qos.logback.core.db.dialect.DBUtil;
import codesquad.command.domain.post.PostDomain;
import codesquad.db.user.Member;
import codesquad.db.user.MemberRepository;
import codesquad.session.SessionUserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class PostRepositoryTest extends DBUtil {

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

            //when
            Post savePost = postRepository.save(post);

            // then
            Post findPost = postRepository.findByPk(savePost.getId());
            postRepository.delete(findPost);

            assertNotNull(findPost);
        }

        @Test
        @DisplayName("post 삭제 테스트")
        void request_with_post_delete() {
            //given
            Post post = new Post(postTitle, postContent, postUserId);

            //when
            Post savePost = postRepository.save(post);
            Post findPost = postRepository.findByPk(savePost.getId());
            postRepository.delete(findPost);

            // then
            Post deletedPost = postRepository.findByPk(savePost.getId());

            assertNull(deletedPost);
        }

        @Test
        @DisplayName("post pk 조회 테스트")
        void request_with_post_pk() {
            // given
            Post post = new Post(postTitle, postContent, postUserId);
            Post savePost = postRepository.save(post);

            // when
            Post findPost = postRepository.findByPk(savePost.getId());

            // then
            assertEquals(savePost, findPost);
        }
    }

    @Nested
    @DisplayName("게시글 조회 테스트")
    class PostListTest {

        @Test
        @DisplayName("post 리스트 조회는 post 이름과 사용자 정보를 조회할 수 있다.")
        void request_with_post_list() {
            // given
            Post post = new Post(postTitle, postContent, postUserId);
            Post savePost = postRepository.save(post);

            // when
            List<PostAndMember> postList = postRepository.getPostList();
            postRepository.delete(savePost);

            // then
            assertNotNull(postList);

        }

    }

}