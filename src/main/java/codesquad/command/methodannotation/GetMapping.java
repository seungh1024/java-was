package codesquad.command.methodannotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import codesquad.http.HttpStatus;

@Retention(RetentionPolicy.RUNTIME)
public @interface GetMapping {
	HttpStatus httpStatus();
	String path();
}
