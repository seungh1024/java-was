package codesquad.http.request.dynamichandler;

import codesquad.http.request.dynamichandler.methodhandler.GetHandler;
import codesquad.http.request.format.HttpRequest;

public class DynamicResourceHandler {
	private static final DynamicResourceHandler dynamicResourceHandler = new DynamicResourceHandler();

	private DynamicResourceHandler(){}

	public static DynamicResourceHandler getInstance() {
		return dynamicResourceHandler;
	}

	public DynamicHandleResult handle(HttpRequest httpRequest) {
		switch(httpRequest.method()) {
			case GET -> GetHandler.getInstance().doGet(httpRequest);
		}
		return null;
	}
}
