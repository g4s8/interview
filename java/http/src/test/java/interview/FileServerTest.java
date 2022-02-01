package interview;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * Test case for {@link FileSErver}.
 */
final class FileServerTest {

    private FileServer server;
    private int port;
    private Path root;

    @BeforeEach
    void setup(@TempDir Path dir) throws Exception {
        this.port = 8899;
        this.root = dir;
        this.server = new FileServer(dir);
        this.server.start(this.port);
    }

    @AfterEach
    void tearDown() throws Exception {
        this.server.stop();
    }

    @Test
    public void uploadPayload() throws Exception {
        final var payload = payload(1);
        final var path = "upload-1";

        final var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        var response = http.send(HttpRequest.newBuilder().uri(this.uri(path)).POST(BodyPublishers.ofByteArray(payload)).build(),
            BodyHandlers.discarding());

        Assertions.assertEquals(201, response.statusCode(), "Response status is not `CREATED`");
        Assertions.assertArrayEquals(payload, Files.readAllBytes(this.root.resolve(path)), "Upload has wrong data");
    }

    @Test
    public void downloadFile() throws Exception {
        final var data = payload(2);
        final var path = "download-2";
        Files.write(this.root.resolve(path), data, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        final var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        var response = http.send(HttpRequest.newBuilder().uri(this.uri(path)).GET().build(),
            BodyHandlers.ofByteArray());
        
        Assertions.assertEquals(200, response.statusCode(), "Response status is not `OK`");
        Assertions.assertArrayEquals(data, response.body(), "Download body has wrong data");
    }

    private URI uri(String path) {
        return URI.create(String.format("http://localhost:%d/%s", this.port, path));
    }

    private static byte[] payload(int seed) {
        final var arr = new byte[1024*4];
        final var path = "upload-1";
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (byte) (i + seed);
        }
        return arr;
    }
}
