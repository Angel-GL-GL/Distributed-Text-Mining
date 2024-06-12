import java.io.*;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Objects;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.Duration;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

//Servidor que interactua con el cliente
public class ServerMain {
    private final int port;
    private final String[] add;
    private HttpServer server;
    private static final String SEARCH_EP = "/search";
    private static final String STATUS_EP = "/status";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    public static void main(String[] args) {
        //Puerto
        int serverPort = 80;
        String[] address = {"localhost:8091","localhost:8092","localhost:8093"};
        //Address
        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }else if(args.length == 3){
            address[0] = args[0];
            address[1] = args[1];
            address[2] = args[2];
        }
        //Inicialización
        ServerMain serverM = new ServerMain(serverPort, address);
        serverM.startServer();
        System.out.println("Servidor escuchando en el puerto " + serverPort);
    }
    
    //Constructor
    public ServerMain(int port, String[] add) {
        this.port = port;
        this.add = add;
    }
    
    //Iniciamos
    public void startServer() {
        try { //Creamos un servidor
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        //El context de los endpoint
        HttpContext searchContext = server.createContext(SEARCH_EP);
        HttpContext statusContext = server.createContext(STATUS_EP);
        //Sus handler
        searchContext.setHandler(this::handleSearchRequest);
        statusContext.setHandler(this::handleStatusRequest);
        //Definimos el pool e iniciamos
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }

    //Handler de status
    private void handleStatusRequest(HttpExchange exchange) throws IOException {
        // Verifica si la solicitud no es GET y la cierra si no lo es
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }
        String res = "";
        //Memoria total y libre
        res = res.concat((Runtime.getRuntime().totalMemory()/ (1024 * 1024))+"mb@");
        res = res.concat((Runtime.getRuntime().freeMemory()/ (1024 * 1024))+"mb@");
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            // Obtén información más detallada, como el porcentaje de uso de la CPU.
            String cpuUsage = ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad() * 100+"";
            res = res.concat(cpuUsage);
        }

        sendResponse(res.getBytes(), exchange);
    } 

    //Handler de search
    private void handleSearchRequest(HttpExchange exchange) throws IOException {
        // Verifica si la solicitud no es POST y la cierra si no lo es
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            exchange.close();
            return;
        }
        
        //Obtiene la solicitud y su contenido
        byte[] requestBytes = exchange.getRequestBody().readAllBytes();
        String requestBody = new String(requestBytes);
        //Las tres respuestas
        Answers answers1 = checkServer(add[0],requestBody+"@1");
        Answers answers2 = checkServer(add[1],requestBody+"@2");
        Answers answers3 = checkServer(add[2],requestBody+"@3");
        //Número de libros analizados
        int numerBooks = answers1.getAnsList().size() +  answers2.getAnsList().size() +  answers3.getAnsList().size();
        //Fusionamos
        answers1.fusionList(answers2.getAnsList());
        answers1.fusionList(answers3.getAnsList());
        //Obtenemos las palabras
        String[] words = requestBody.split(" ");
        //Para conocer en cuantos libros aparece cada palabra aunque sea una vez
        int[] nt = new int[words.length];
        for(int i = 0; i<nt.length; i++) nt[i] = 0;
        //Contamos en cuantos libros se repitio al menos una vez cada palabra
        for(Book book: answers1.getAnsList()){
            for(int i = 0; i<words.length; i++){
                if(book.getTF(words[i]) > (double)0) nt[i]+=1;
            }
        }
        //Obtenemos el total de cada uno y ordenamos
        answers1.setEachTotal(numerBooks, nt, words);

        answers1.order();

        sendResponse(SerializationUtils.serialize(answers1), exchange);
    } 

    
    //Envia el request a los otros servers
    private Answers checkServer(String add, String input) {
        Answers answers = new Answers();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+add+"/mining"))
                .header("Content-Type", "text/plain; charset=UTF-8")
                .POST(BodyPublishers.ofString(input))
                .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            //Queremos el objeto, así que deserializamos
            answers = (Answers)SerializationUtils.deserialize(response.body());
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
        return answers;
    }

    private static void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
        exchange.close();
    }
}