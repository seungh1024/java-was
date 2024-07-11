package codesquad.http.request.dynamichandler;

import codesquad.command.domainResponse.DomainResponse;
import codesquad.exception.client.ClientErrorCode;
import codesquad.http.request.dynamichandler.methodhandler.GetHandler;
import codesquad.http.request.dynamichandler.methodhandler.PostHandler;
import codesquad.http.request.format.HttpRequest;

public class DynamicResourceHandler {
	private static final DynamicResourceHandler dynamicResourceHandler = new DynamicResourceHandler();

	private DynamicResourceHandler(){}

	public static DynamicResourceHandler getInstance() {
		return dynamicResourceHandler;
	}

	public DynamicHandleResult handle(HttpRequest httpRequest) {
		DynamicHandleResult dynamicHandleResult = null;

		switch(httpRequest.method()) {
			case GET -> dynamicHandleResult = GetHandler.getInstance().doGet(httpRequest);
			case POST -> dynamicHandleResult = PostHandler.getInstance().doPost(httpRequest);
			default -> throw ClientErrorCode.METHOD_NOT_ALLOWED.exception();
		}


		return dynamicHandleResult;
	}
}
