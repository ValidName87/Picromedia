import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import parsing.HTTPRequest;
import parsing.HTTPResponse;
import parsing.Router;

public class ConnectionHandler implements Runnable {
    private final Socket socket;

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream out = socket.getOutputStream();

            List<String> input = new ArrayList<>();
            do {
                input.add(in.readLine());
            } while (in.ready());
            HTTPRequest request = new HTTPRequest(input);

            HTTPResponse response = Router.getResponse(request);

            System.out.println(request);
            System.out.println(response.toStringBodyless() + new String(response.getBody(), StandardCharsets.UTF_8));

            out.write(response.toStringBodyless().getBytes(StandardCharsets.UTF_8));
            out.write(response.getBody());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("The socket couldn't close");
            }
        }
    }

}
