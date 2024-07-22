package codesquad;

import codesquad.db.util.DBConnectionUtil;
import org.junit.jupiter.api.BeforeAll;

public class DBInit {

    @BeforeAll
    static void setUp() {
        DBConnectionUtil.init();
    }
}
