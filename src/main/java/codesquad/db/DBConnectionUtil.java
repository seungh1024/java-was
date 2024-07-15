package codesquad.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
}
