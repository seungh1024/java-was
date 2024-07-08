package codesquad.command.annotation.redirect;

import codesquad.http.HttpStatus;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Redirect {
    String redirection();
    HttpStatus httpStatus() default HttpStatus.FOUND;
}
