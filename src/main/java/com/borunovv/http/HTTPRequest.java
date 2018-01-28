package com.borunovv.http;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPRequest {
    private static final Pattern URI_PATTERN = Pattern.compile("[\\w]+[\\s]+([^\\s]+)\\s+HTTP/", Pattern.MULTILINE);

    private String method = "";
    private String url = "";
    private Map<String, String> headers = new LinkedHashMap<>();
    private byte[] content = null;

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getContent() {
        return content;
    }

    public static HTTPRequest unmarshall(byte[] data) {
        HTTPRequest result = tryUnmarshall(data);
        if (result == null) {
            throw new IllegalArgumentException("Failed to unmarshall request.");
        }
        return result;
    }


    public static HTTPRequest tryUnmarshall(byte[] data) {
        int contentDelimIndex = 0;
        for (int i = 0; i < data.length - 3; ++i) {
            if (data[i] == 13 && data[i + 1] == 10 && data[i + 2] == 13 && data[i + 3] == 10) {
                contentDelimIndex = i;
                break;
            }
        }
        if (contentDelimIndex == 0) {
            return null;
        }

        String requestFirstPart;
        try {
            requestFirstPart = new String(data, 0, contentDelimIndex, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        HTTPRequest req = new HTTPRequest();
        if (!requestFirstPart.contains("\r\n")) {
            return null;
        }

        String firstLine = requestFirstPart.substring(0, requestFirstPart.indexOf("\r\n"));

        Map<String, String> headers = parseHeaders(requestFirstPart);
        String method = firstLine.substring(0, firstLine.indexOf(" ")).toUpperCase();

        req.url = getUrl(firstLine);
        req.method = method;
        req.headers = headers;

        switch (method) {
            case "GET":
            case "HEAD":
                return req;
            case "POST":
            case "PUT":
            case "DELETE": {
                if (!headers.containsKey("Content-Length")) {
                    return req;
                } else {
                    int contentLength = Integer.parseInt(req.getHeader("Content-Length"));
                    int actualContentLength = data.length - contentDelimIndex - 4;
                    if (contentLength == actualContentLength) {
                        req.content = Arrays.copyOfRange(data, contentDelimIndex + 4, data.length);
                        return req;
                    } else {
                        return null;
                    }
                }
            }
            default:
                throw new IllegalArgumentException("Unsupported method: " + method);
        }
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    private static Map<String, String> parseHeaders(String request) {
        String[] hdrs = request.substring(
                request.indexOf("\r\n") + 2)
                .split("\r\n");

        Map<String, String> result = new HashMap<>();
        for (String hdr : hdrs) {
            if (hdr.contains(":")) {
                String name = hdr.substring(0, hdr.indexOf(":"));
                String value = hdr.substring(hdr.indexOf(":") + 1).trim();
                result.put(name, value);
            }
        }
        return result;
    }

    private static String getUrl(String firstLine) {
        Matcher m = URI_PATTERN.matcher(firstLine);
        if (m.find()) {
            return m.group(1);
        } else {
            throw new RuntimeException("Can't parse URL from request:\n" + firstLine);
        }
    }

    @Override
    public String toString() {
        return "HTTPRequest{" +
                "method='" + method + '\'' +
                ", url='" + url + '\'' +
                ", headers_count=" + headers.size() +
                ", content_length=" + (content != null ? content.length : 0) +
                '}';
    }
}

