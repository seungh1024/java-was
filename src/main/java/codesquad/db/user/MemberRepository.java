package codesquad.db.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import codesquad.exception.client.ClientErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codesquad.db.util.DBConnectionUtil;

public class MemberRepository {
	private final Logger log = LoggerFactory.getLogger(MemberRepository.class);
	private static final MemberRepository memberRepository = new MemberRepository();
	private MemberRepository() {}
	public static MemberRepository getInstance() {
		return memberRepository;
	}

	public Member save(Member member) {
		log.info("[Member Save], member = {}",member);
		Member originMember = findById(member.getMemberId());
		if(originMember != null) {
			throw ClientErrorCode.USERID_ALREADY_EXISTS.exception();
		}
		var sql = """
			insert into member(member_id, member_password, member_name, member_email)
			values(?,?,?,?)
			""";

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = getConnection();
			ps = con.prepareStatement(sql);
			ps.setString(1, member.getMemberId());
			ps.setString(2, member.getPassword());
			ps.setString(3, member.getName());
			ps.setString(4, member.getEmail());
			ps.executeUpdate();

			return member;
		} catch (SQLException exception) {
			log.error("[SQLException] throw error when member save, Class Info = {}", MemberRepository.class);
			throw new RuntimeException(exception);
		}finally {
			close(con, ps, null);
		}

	}

	public Member findByPk(long pk) {
		log.info("[Member FindById], userId = {}",pk);
		var sql = """
			select * from member
			where id = ?
			""";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = getConnection();
			ps = con.prepareStatement(sql);
			ps.setLong(1, pk);

			rs = ps.executeQuery();
			if (rs.next()) {
				Member member = new Member();
				member.setId(rs.getLong("id"));
				member.setMemberId(rs.getString("member_id"));
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

	public Member findById(String userId) {
		log.info("[Member FindById], userId = {}",userId);
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
				member.setMemberId(rs.getString("member_id"));
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

	public List<Member> findMemberList() {
		var sql = """
			select * from member
			""";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = getConnection();
			ps = con.prepareStatement(sql);

			rs = ps.executeQuery();
			if (rs.next()) {
				var memberList = new ArrayList<Member>();
				do{
					Member member = new Member();
					member.setId(rs.getLong("id"));
					member.setMemberId(rs.getString("member_id"));
					member.setPassword(rs.getString("member_password"));
					member.setName(rs.getString("member_name"));
					member.setEmail(rs.getString("member_email"));
					memberList.add(member);
				}while(rs.next());

				return memberList;
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

	public void delete(Member member) {
		log.info("[Member Delete], memberId = {}",member);

		var sql = """
			delete from member where member_id = ?
			""";

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = getConnection();
			ps = con.prepareStatement(sql);
			ps.setString(1, member.getMemberId());
			ps.executeUpdate();

		} catch (SQLException exception) {
			log.error("[SQLException] throw error when findById, Class info = {}", MemberRepository.class);
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
