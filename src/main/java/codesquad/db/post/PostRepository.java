package codesquad.db.post;

import codesquad.db.user.MemberRepository;
import codesquad.db.util.DBConnectionUtil;
import codesquad.session.SessionUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PostRepository {
    private final Logger log = LoggerFactory.getLogger(PostRepository.class);
    private static final PostRepository postRepository = new PostRepository();
    private PostRepository() {}

    public static PostRepository getInstance() {
        return postRepository;
    }

    public long save(Post post) {
        log.info("[Post Save], post = {}", post);

        var sql = """
            insert into post(post_title,post_content,user_id)
            values(?,?,?)
            """;

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            ps = con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS); // pk 반환 옵션
            ps.setString(1,post.getTitle());
            ps.setString(2,post.getContent());
            ps.setLong(3,post.getUserId());
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            long pk = 0;
            if (rs.next()) {
                pk = rs.getLong(1);
            }
            return pk;
        } catch (SQLException exception) {
            log.error("[SQLException] throw error when post save, Class Info = {}", PostRepository.class);
            throw new RuntimeException(exception);
        }finally {
            close(con, ps, rs);
        }
    }

    public Post findByPk(long id){
        log.info("[Post FindById], id = {}", id);
        var sql = """
                select * from post
                where id = ?
                """;

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            ps = con.prepareStatement(sql);
            ps.setLong(1, id);

            rs = ps.executeQuery();
            if (rs.next()) {
                Post post = new Post();
                post.setId(rs.getLong(1));
                post.setTitle(rs.getString(2));
                post.setContent(rs.getString(3));
                post.setUserId(rs.getLong(4));

                return post;
            } else {
                return null;
            }
        } catch (SQLException exception) {
            log.error("[SQLException] throw error when Post findByPk, Class info = {}", MemberRepository.class);
            throw new RuntimeException(exception);
        }finally {
            close(con,ps,rs);
        }
    }

    public void delete(Post post) {
        log.info("[Post Delete], post = {}", post);

        var sql = """
			delete from post where id = ?
			""";

        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = getConnection();
            ps = con.prepareStatement(sql);
            ps.setLong(1, post.getId());
            ps.executeUpdate();

        } catch (SQLException exception) {
            log.error("[SQLException] throw error when delete post, Class info = {}", MemberRepository.class);
            throw new RuntimeException(exception);
        }finally {
            close(con,ps, null);
        }
    }

    /**
     * 게시글과 사용자 정보 Join하여 가져옴
     * @return
     */
    public List<PostAndMember> getPostList() {
        log.info("[Post List] getPostList");

        var postSql = """
                select * from post
                """;

        var userSql = """
                select id,member_name from member
                where id = ?
                """;

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            ps = con.prepareStatement(postSql);
            rs = ps.executeQuery();

            var postAndMember = new ArrayList<PostAndMember>();
            var postList = new ArrayList<Post>();

            if (rs.next()) {
                do {
                    Post post = new Post();
                    post.setId(rs.getLong(1));
                    post.setTitle(rs.getString(2));
                    post.setContent(rs.getString(3));
                    post.setUserId(rs.getLong(4));
                    postList.add(post);
                } while (rs.next());
            }

            for (Post post : postList) {
                ps = con.prepareStatement(userSql);
                ps.setLong(1,post.getUserId());
                System.out.println(post);
                rs = ps.executeQuery();

                if (rs.next()) {
                    do {
                        var memberPk = rs.getLong(1);
                        var memberName = rs.getString(2);
                        var pam = new PostAndMember(post.getId(), post.getTitle(), post.getContent(), memberPk, memberName);
                        postAndMember.add(pam);
                    } while (rs.next());
                }
            }

            System.out.println(postAndMember);

            if (postAndMember.size() == 0) {
                postAndMember = null;
            }

            return postAndMember;

        } catch (SQLException exception) {
            log.error("[SQLException] throw error when delete post, Class info = {}", MemberRepository.class);
            throw new RuntimeException(exception);
        }finally {
            close(con,ps, null);
        }

    }

    private void close(Connection connection, Statement stmt, ResultSet rs) {
        DBConnectionUtil.close(connection,stmt,rs);
    }

    private Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }
}
