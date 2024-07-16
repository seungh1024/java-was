package codesquad.db.post;

import codesquad.db.util.DBConnectionUtil;
import codesquad.session.SessionUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class PostRepository {
    private final Logger log = LoggerFactory.getLogger(PostRepository.class);
    private static final PostRepository postRepository = new PostRepository();
    private PostRepository() {}

    public static PostRepository getInstance() {
        return postRepository;
    }

    public Post save(Post post, SessionUserInfo userInfo) {
        log.info("[Post Save], post = {}", post);

        var sql = """
            insert into post(post_title,post_content,user_id)
            values(?,?,?)
            """;

        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1,post.getTitle());
            ps.setString(2,post.getContent());
            ps.setLong(3,post.getUserId());
            ps.executeUpdate();

            return post;
        } catch (SQLException exception) {
            log.error("[SQLException] throw error when post save, Class Info = {}", PostRepository.class);
            throw new RuntimeException(exception);
        }finally {
            close(con, ps, null);
        }
    }

    private void close(Connection connection, Statement stmt, ResultSet rs) {
        DBConnectionUtil.close(connection,stmt,rs);
    }

    private Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }
}
