package codesquad.util;

import codesquad.exception.client.ClientErrorCode;

import java.util.Objects;

public enum FileExtension {
    HTML,CSS,JS,ICO,PNG,JPG;

    public static FileExtension fromString(String extension) {
        for(FileExtension value : FileExtension.values()) {
            if (Objects.equals(extension, value.name())) {
                return value;
            }
        }

        throw ClientErrorCode.NOT_FOUND.exception();
    }
}
