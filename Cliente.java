import java.io.*;
import java.util.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.Duration;

//Es la clase principal
public class Cliente{
    private static GUI_Cliente gui_Cliente;
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static String address;

    public static void main(String[] args) {
        address = "localhost:80";
        if(args.length == 1) address = args[0];
        gui_Cliente = new GUI_Cliente();
        gui_Cliente.startView();

        gui_Cliente.searchButton.addActionListener((e) -> {
            String input = gui_Cliente.search();
            gui_Cliente.resetFields();
            try{
                search(input);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        });
    }

    
    //Busqueda
    public static void search(String input) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://"+address+"/search"))
            .header("Content-Type", "text/plain; charset=UTF-8")
            .POST(BodyPublishers.ofString(input))
            .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        //Deserializamos
        Answers answers = (Answers)SerializationUtils.deserialize(response.body());

        gui_Cliente.addAnswer(answers.toString());
    }
}
