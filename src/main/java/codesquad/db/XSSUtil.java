package codesquad.db;

import java.util.List;
import java.util.Map;

public class XSSUtil {
	public static final List<String> list = List.of("&", "<", ">", "(", ")", "\"", "'", "/");
	public static final Map<String, String> map = Map.of("&", "&amp;", "<", "&lt;", ">", "&gt;", "(", "&#40;", "\"",
		"&quot;", "'", "&#x27;", "/", "&#x2F;", ")", "&#41;");
}
