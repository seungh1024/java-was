package codesquad.util;

import ch.qos.logback.core.db.dialect.DBUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileExtensionTest extends DBUtil {

    @Nested
    @DisplayName("파일 확장자 검색 테스트")
    class FileExtensionSearchTest{

        @Test
        @DisplayName("html을 제외한 존재하는 파일 확장자를 요청하면 해당하는 확장자를 응답해야 한다.")
        void request_with_exists_file_extension() {
            // given
            String extension = "CSS";

            // when
            FileExtension fileExtension = FileExtension.fromString(extension);

            // then
            assertEquals(FileExtension.CSS, fileExtension);
        }

        @Test
        @DisplayName("html 파일 확장자를 요청하면 DYNAMIC을 응답해야 한다.")
        void request_with_html_file_extension() {
            // given
            String extension = "HTML";

            // when
            FileExtension fileExtension = FileExtension.fromString(extension);

            // then
            assertEquals(FileExtension.DYNAMIC, fileExtension);
        }

        @Test
        @DisplayName("존재하지 않는 파일 확장자를 요청하면 DYNAMIC을 응답해야 한다.")
        void request_with_non_exists_file_extension() {
            // given
            String extension = "/test";

            // when
            FileExtension fileExtension = FileExtension.fromString(extension);

            // then
            assertEquals(FileExtension.DYNAMIC, fileExtension);
        }
    }
}