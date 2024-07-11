package codesquad.command.annotation.preprocess;

import codesquad.command.interceptor.PreHandler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PreHandle {
    Class<?> target();
}
