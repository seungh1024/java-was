package codesquad.request.format;

import java.util.Map;

public record ClientRequest(
        String method,
        String uri,
        String httpVersion,
        Map<String,Map<String,String>> headers,
        String body

) {
}
