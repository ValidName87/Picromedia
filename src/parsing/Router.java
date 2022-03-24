package parsing;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Router {
    private final HTTPRequest request;

    public Router(HTTPRequest request) {
        this.request = request;
    }

    public HTTPResponse getResponse() {
        return request.getPath().startsWith("api/") ? getAPIResponse() : getPageResponse();
    }

    public HTTPResponse getPageResponse() {
        HTTPResponse response;
        if (!request.getVersion().startsWith("HTTP/1.")) {
            response = new HTTPResponse();
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
                response = new HTTPResponse();
                response.setStatusCode("405 Method Not Allowed");
                response.putHeader("Allow", "GET");
                return response;
            default:
                response = new HTTPResponse();
                response.set400();
                return response;
        }
    }

    public HTTPResponse getFileResponse(String path) {
        HTTPResponse response = new HTTPResponse();
        String[] fileName = path.split("\\.");
        String contentType = fileName.length > 1 ? getContentType(fileName[fileName.length - 1]) : "text/html";
        path = path.contentEquals("/") ? "src/pages/index" : "src/pages" + path;
        if (fileName.length == 1) {
            path = path + ".html";
        }
        System.out.println(">> Going to read from path: " + path);
        if (contentType == null) {
            response.setStatusCode("500 Internal Server Error");
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

    public HTTPResponse getAPIResponse() {
        HTTPResponse response = new HTTPResponse();
        response.setStatusCode("501 Not Implemented");
        return response;
    }

    public String getContentType(String extension) {
        try (FileInputStream extStream = new FileInputStream("src/resources/MIME Types.csv")) {
            String extensions = new String(extStream.readAllBytes(), StandardCharsets.UTF_8);
            return Arrays.stream(extensions.split("\n"))
                    .filter((String s) -> s.startsWith(extension)).findFirst()
                    .orElse(",UNKNOWN")
                    .split(",")[1];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
