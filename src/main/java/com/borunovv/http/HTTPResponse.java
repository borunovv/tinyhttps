package com.borunovv.http;

public class HTTPResponse {
    private static final String HTTP_NL = "\r\n";

    public final int status;
    public final String content;
    public final String contentType;

    public HTTPResponse(int status) {
        this.status = status;
        this.content = null;
        this.contentType = null;
    }

    public HTTPResponse(int status, String content, String contentType) {
        this.status = status;
        this.content = content;
        this.contentType = contentType;
    }

    public static HTTPResponse plainText(String text) {
        return new HTTPResponse(200, text, "text/plain; charset=UTF-8");
    }

    public static HTTPResponse html(String html) {
        return new HTTPResponse(200, html, "text/html; charset=UTF-8");
    }

    public String marshall() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 )").append(status).append(getStatusShortName(status)).append(HTTP_NL);
        if (contentType != null) {
            sb.append("Content-Type: ").append(contentType).append(HTTP_NL);
        }
        if (content != null) {
            sb.append("Content-Length: ").append(content.length()).append(HTTP_NL);
        }
        // To allow Cross Domain requests (CORS)
        sb.append("Access-Control-Allow-Origin:*").append(HTTP_NL);

        sb.append(HTTP_NL);
        if (content != null) {
            sb.append(content);
        }
        return sb.toString();
    }

    private String getStatusShortName(int status) {
        switch (status) {
            case 200:
                return "OK";
            case 404:
                return "Not found";
            default:
                return "";
        }
    }

    @Override
    public String toString() {
        return "HTTPResponse{" +
                "status=" + status +
                ", content_length='" + (content != null ? content.length() : 0) + '\'' +
                ", contentType='" + contentType + '\'' +
                '}';
    }
}
