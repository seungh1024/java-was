package codesquad.command.methodannotation;

import codesquad.http.HttpStatus;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PostMapping {
    HttpStatus httpStatus();
    String path();
}
