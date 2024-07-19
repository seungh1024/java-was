package codesquad.util;

import codesquad.http.request.format.HttpRequest;

import java.util.Objects;

public enum FileExtension {
    HTML("text/html"),CSS("text/css"),JS("text/javascript"),ICO("image/x-icon"),PNG("image/png"),JPG("image/jpg"),SVG("image/svg+xml"),NONE("none"), DYNAMIC("dynamic"), MULTIPART("multipart/form-data");
    private String contentType;

    FileExtension(String contentType) {
        this.contentType = contentType;
    }

    public static FileExtension fromString(String extension) {
        var result = DYNAMIC;
        for(FileExtension value : FileExtension.values()) {
            if (!Objects.equals(value, HTML) && Objects.equals(extension, value.name())) {
                result = value;
                break;
            }
        }

        return result;
    }

    public String getContentType() {
        return contentType;
    }
}
