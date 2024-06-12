import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

//Para enviar los request del GUI_Monitor
public class WebClient {
    private HttpClient client;

    public WebClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }
    //Recibe la direccion con la que realizara la conexion
    public CompletableFuture<String> sendTask(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .header("X-debug","true")
                .uri(URI.create(url))
                .build();
        //Envia el http request
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(respuesta -> { return respuesta.body().toString();});
    }
}
