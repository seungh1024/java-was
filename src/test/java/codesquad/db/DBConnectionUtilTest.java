package codesquad.db;

import java.sql.Connection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DBConnectionUtilTest {

	@Nested
	@DisplayName("H2 DB Connection Test")
	class ConnectionTest{

		@Test
		@DisplayName("커넥션은 null이 아니어야 한다.")
		void connection_not_null() {
			Connection connection = DBConnectionUtil.getConnection();

			assertNotNull(connection);
		}
	}
}