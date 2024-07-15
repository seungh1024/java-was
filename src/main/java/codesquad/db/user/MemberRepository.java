package codesquad.db.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codesquad.db.DBConnectionUtil;

public class MemberRepository {
	private final Logger log = LoggerFactory.getLogger(MemberRepository.class);

	public Member save(Member member) {
		var sql = """
			insert into member(member_id, member, member_password, member_name, member_email)
			values(?,?,?,?)
			""";

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = getConnection();
			ps = con.prepareStatement(sql);
			ps.setString(1, member.userId());
			ps.setString(2, member.password());
			ps.setString(3, member.name());
			ps.setString(4, member.email());

			return member;
		} catch (SQLException exception) {
			log.error("[SQLException] throw error when save, Class Info = {}", MemberRepository.class);
			throw new RuntimeException(exception);
		}finally {
			close(con, ps, null);
		}

	}

	public Member findById(String userId) {
		var sql = """
			select * from member
			where member_id = ?
			""";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = getConnection();
			ps = con.prepareStatement(sql);
			ps.setString(1, userId);

			rs = ps.executeQuery();
			if (rs.next()) {
				Member member = new Member();
				member.setId(rs.getLong("id"));
				member.setUserId(rs.getString("member_id"));
				member.setPassword(rs.getString("member_password"));
				member.setName(rs.getString("member_name"));
				member.setEmail(rs.getString("member_email"));

				return member;
			} else {
				log.info("[MemberRepository] 사용자 정보가 없습니다.");
				return null;
			}

		} catch (SQLException exception) {
			log.error("[SQLException] throw error when findById, Class info = {}", MemberRepository.class);
			throw new RuntimeException(exception);
		}finally {
			close(con,ps,rs);
		}
	}

	private void close(Connection connection, Statement stmt, ResultSet rs) {
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

	private Connection getConnection() {
		return DBConnectionUtil.getConnection();
	}

}
