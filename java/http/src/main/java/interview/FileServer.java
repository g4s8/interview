package interview;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Very primitive HTTP web-server supports two HTTP methods:
 * <ul>
 *   <li>{@code PUT} - saves request body to file in directory specified in
 *   constructor, it takes file's name from request path. On success it returns
 *   {@code 201 - Created}.</li>
 *   <li>{@code GET} - returns response with payload from file in directory specified
 *   in constructor, it takes file's name from request path. Also, it returns
 *   {@code Content-Length} header.</li>
 * </ul>
 * File path in request URI must contains only digits and letters and must be in size between 1 and 10
 * (each letter match the patter {@code [a-zA-Z0-9]).
 */
public class FileServer {
    
    private final Path dir;
    private AtomicBoolean stop = new AtomicBoolean();
    private final ExecutorService exec;

    private ServerSocket server;

    public FileServer(Path dir) {
        this.dir = dir;
        this.exec = Executors.newSingleThreadExecutor();
    }

    public void start(int port) throws IOException {
        this.server = new ServerSocket(port);
        this.exec.submit(() -> {
            try {
                while (!this.stop.get()) {
                    final var sock = server.accept();
                    System.out.printf("accepted socket: %s\n", sock);
                    try (var is = new BufferedInputStream(sock.getInputStream());
                        var os = new BufferedOutputStream(sock.getOutputStream())) {
                        this.handle(is, os);
                    }
                    System.out.printf("socket handled: %s\n", sock);
                }
            } catch (IOException iex) {
                System.out.println(iex);
                iex.printStackTrace();
                throw new UncheckedIOException(iex);
            }
            System.out.println("server sinished");
        });
        System.out.printf("server started on port %d\n", port);
    }

    private void handle(InputStream request, OutputStream response) throws IOException {
        // hint: check request.available()
        // TODO: implement request handling
    }

    public void stop() throws IOException {
        this.stop.set(true);
        this.exec.shutdown();
        this.server.close();
    }
}
