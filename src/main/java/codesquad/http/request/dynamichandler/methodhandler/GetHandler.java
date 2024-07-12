package codesquad.http.request.dynamichandler.methodhandler;

import codesquad.command.CommandManager;
import codesquad.command.domainResponse.DomainResponse;
import codesquad.http.request.dynamichandler.DynamicHandleResult;
import codesquad.http.request.format.HttpRequest;

/**
 * query parameter, path parameter 구분
 */
public class GetHandler {
	private static final GetHandler handler = new GetHandler();

	private GetHandler(){}

	public static GetHandler getInstance() {
		return handler;
	}

	public DynamicHandleResult doGet(HttpRequest httpRequest) {
		DynamicHandleResult dynamicHandleResult = null;

		if (httpRequest.uri().contains("?")) {
			DomainResponse domainResponse = CommandManager.getInstance().execute(httpRequest);
			dynamicHandleResult = DynamicHandleResult.of(domainResponse);
		} else {
			DomainResponse domainResponse = CommandManager.getInstance().execute(httpRequest);
			dynamicHandleResult = DynamicHandleResult.of(domainResponse);
		}

		return dynamicHandleResult;
	}
}
