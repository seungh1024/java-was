package codesquad.db.util;

import java.sql.*;
import java.util.Objects;

import codesquad.db.user.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConnectionUtil {
	private static final Logger log = LoggerFactory.getLogger(DBConnectionUtil.class);

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
