package codesquad.util;

import codesquad.exception.client.ClientErrorCode;

import java.util.Objects;

public enum FileExtension {
    HTML("text/html"),CSS("text/css"),JS("text/javascript"),ICO("image/x-icon"),PNG("image/png"),JPG("image/jpg"),SVG("image/svg+xml");
    private String contentType;

    FileExtension(String contentType) {
        this.contentType = contentType;
    }

    public static FileExtension fromString(String extension) {
        for(FileExtension value : FileExtension.values()) {
            if (Objects.equals(extension, value.name())) {
                return value;
            }
        }

        throw ClientErrorCode.NOT_FOUND.exception();
    }

    public String getContentType() {
        return contentType;
    }
}
