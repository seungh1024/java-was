package codesquad.db.util;

import java.sql.*;
import java.util.Objects;

import codesquad.db.user.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConnectionUtil {
	private static final Logger log = LoggerFactory.getLogger(DBConnectionUtil.class);

	public static void init() {
		var memberSql = """
				create table if not exists member(
					id bigInt not null auto_increment,
					member_id varchar(100),
					member_password varchar(100),
					member_name varchar(100),
					member_email varchar(100),
					primary key (id)
				);
				""";

		var postSql = """
				create table if not exists post(
					id bigInt not null auto_increment,
					post_title varchar(100),
					post_content varchar(100),
					user_id bigint,
					file_name varchar(100),
					file_path varchar(200),
					primary key (id)
				);
				""";

		Connection con = null;
		PreparedStatement ps = null;


		try {
			con = getConnection();
			ps = con.prepareStatement(postSql);
			ps.execute();

			ps = con.prepareStatement(memberSql);
			ps.execute();

		} catch (SQLException exception) {
			log.error("[SQLException] throw error when delete post, Class info = {}", MemberRepository.class);
			throw new RuntimeException(exception);
		}finally {
			close(con,ps, null);
		}

	}

	public static Connection getConnection() {
		try {
			var connection = DriverManager.getConnection(ConnectionConst.URL, ConnectionConst.USERNAME,
				ConnectionConst.PASSWORD);

			log.info("[Connection]  connection info = {}, class = {}", connection, connection.getClass());
			return connection;
		} catch (SQLException exception) {
			throw new RuntimeException(exception);
		}
	}

	public static void close(Connection connection, Statement stmt, ResultSet rs) {
		if (Objects.nonNull(rs)) {
			try {
				rs.close();
			} catch (SQLException exception) {
				log.error("[SQLException] Class Info = {}, Exception with ResultSet close", MemberRepository.class);
				exception.printStackTrace();
			}
		}

		if (Objects.nonNull(stmt)) {
			try {
				stmt.close();
			} catch (SQLException exception) {
				log.error("[SQLException] Class Info = {}, Exception with Statement close", MemberRepository.class);
				exception.printStackTrace();
			}
		}

		if (Objects.nonNull(connection)) {
			try {
				connection.close();
			} catch (SQLException exception) {
				log.error("[SQLException] Class Info = {}, Exception with Connection close", MemberRepository.class);
				exception.printStackTrace();
			}
		}

	}
}
