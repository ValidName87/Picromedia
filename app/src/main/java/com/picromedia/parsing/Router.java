package com.picromedia.parsing;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.Arrays;

import com.picromedia.controllers.Controller;

public class Router {

    public static HTTPResponse getResponse(HTTPRequest request, Connection sqlConnection) {
        HTTPResponse response = request.getPath().startsWith("/api/") ? getAPIResponse(request, sqlConnection) : getPageResponse(request);
        if (!response.getHeaders().containsKey("Content-Length")) {
            response.putHeader("Content-Length", String.valueOf(response.getBody().length));
        }
        response.putHeader("Access-Control-Allow-Origin", "*");
        return response;
    }

    private static HTTPResponse getPageResponse(HTTPRequest request) {
        HTTPResponse response = new HTTPResponse();
        if (!request.getVersion().startsWith("HTTP/1.")) {
            response.setStatusCode("505 HTTP Version Not Supported");
            return response;
        }
        switch (request.getVerb()) {
            case "GET":
                return getFileResponse(request.getPath());
            case "HEAD":
                response = getFileResponse(request.getPath());
                response.setBody(new byte[] {});
                return response;
            case "POST":
            case "PUT":
            case "DELETE":
                response.setStatusCode("405 Method Not Allowed");
                response.putHeader("Allow", "GET, HEAD");
                return response;
            default:
                response.set400();
                return response;
        }
    }

    private static HTTPResponse getFileResponse(String path) {
        HTTPResponse response = new HTTPResponse();
        String[] fileName = path.split("\\.");
        String contentType = fileName.length > 1 ? getContentType(fileName[fileName.length - 1]) : "text/html";
        path = path.contentEquals("/") ? "src/main/website/home" : "src/main/website" + path;
        if (fileName.length == 1) {
            path = path + ".html";
        }
        System.out.println(">> Going to read from path: " + path);
        if (contentType == null) {
            response.set500();
            return response;
        }
        if (contentType.equals("UNKNOWN")) {
            response.setStatusCode("501 Not Implemented");
            return response;
        }
        try (FileInputStream fileStream = new FileInputStream(path)) {
            byte[] bytes = fileStream.readAllBytes();
            response.setBody(bytes);
            response.putHeader("Content-Type", contentType);
            response.putHeader("Content-Length", String.valueOf(bytes.length));
        } catch (IOException e) {
            response.set404();
        } catch (SecurityException e) {
            response.set403();
        }
        return response;
    }

    private static String getContentType(String extension) {
        try (FileInputStream extStream = new FileInputStream("src/main/resources/MIME Types.csv")) {
            String extensions = new String(extStream.readAllBytes(), StandardCharsets.UTF_8);
            return Arrays.stream(extensions.split("\n"))
                    .filter((String s) -> s.startsWith(extension.toLowerCase())).findFirst()
                    .orElse(",UNKNOWN")
                    .split(",")[1];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static HTTPResponse getAPIResponse(HTTPRequest request, Connection sqlConnection) {
        ApiURL url;
        try {
            url = new ApiURL(request.getPath());
        } catch (IllegalArgumentException e) {
            HTTPResponse response = new HTTPResponse();
            response.set400();
            return response;
        }
        Controller controller = url.getController();
        if (controller == null) {
            HTTPResponse response = new HTTPResponse();
            response.set400();
            return response;
        }

        url.getOptions().put("Authorization", request.getHeaderValue("Authorization"));

        controller.lock();
        try {
            switch (request.getVerb()) {
                case "GET":
                    return controller.GET(url.getOptions(), sqlConnection);
                case "POST":
                    return controller.POST(url.getOptions(), request.getBody(), sqlConnection);
                case "PUT":
                    return controller.PUT(url.getOptions(), request.getBody(), sqlConnection);
                case "DELETE":
                    return controller.DELETE(url.getOptions(), sqlConnection);
                case "HEAD":
                    return controller.HEAD(url.getOptions(), sqlConnection);
                default:
                    HTTPResponse response = new HTTPResponse();
                    response.setStatusCode("501 Not Implemented");
                    return response;
            }
        } finally {
            controller.unlock();
        }
    }
}
