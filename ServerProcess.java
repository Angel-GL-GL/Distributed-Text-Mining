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

//Servidor de procesamiento (donde se hace la busqueda en textos)
public class ServerProcess{
    private final int port;
    private HttpServer server;
    private static final String MINING_EP = "/mining";
    private static final String STATUS_EP = "/status";
    
    public static void main(String[] args) {
        //Puerto
        int serverPort = 80;
        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }
        //Inicialización
        ServerProcess serverProcess = new ServerProcess(serverPort);
        serverProcess.startServer();
        System.out.println("Servidor escuchando en el puerto " + serverPort);
    }
    
    //Constructor
    public ServerProcess(int port) {
        this.port = port;
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
        HttpContext miningContext = server.createContext(MINING_EP);
        HttpContext statusContext = server.createContext(STATUS_EP);
        //Sus handler
        miningContext.setHandler(this::handleMiningRequest);
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
        //Memoria total y memoria libre
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

    //Handler de mining
    private void handleMiningRequest(HttpExchange exchange) throws IOException {
        // Verifica si la solicitud no es POST y la cierra si no lo es
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            exchange.close();
            return;
        }
        
        //Obtiene la solicitud y su contenido
        byte[] requestBytes = exchange.getRequestBody().readAllBytes();
        String requestBody = new String(requestBytes);

        String[] params = requestBody.split("@");
        String[] words = params[0].split(" ");

        Answers answers = startMining(words, Integer.parseInt(params[1]));

        sendResponse(SerializationUtils.serialize(answers), exchange);
    } 
    //Mineria
    private static Answers startMining(String[] words, int num){
        Answers answers = new Answers();
        //Limites
        int counter = 1, min = 16*(num-1) +1, max = 16*num;
        FileReader filereader;
        BufferedReader bufferedReader;

        try{
            //Entramos al directorio
            File directorio = new File(new File("").getAbsolutePath() + "//LIBROS_TXT");
            //Obtenemos cada archivo
            for (File ficheroEntrada : directorio.listFiles()) {
                if(counter >= min && counter <= max){ //Dentro de los limites
                    //Para leer
                    filereader = new FileReader(ficheroEntrada);
                    bufferedReader = new BufferedReader(filereader);
                    //Nombre y variables a usar
                    String name = ficheroEntrada.getName();
                    int wordCount = 0;
                    Integer[] matches = new Integer[words.length];
                    //Inicializamos con cero
                    for(int j = 0; j<matches.length;j++){
                        matches[j] = 0;
                    }

                    String str = "";
                    //Linea por linea
                    while ((str = bufferedReader.readLine()) != null) {
                        // Dividir la línea en palabras
                        String[] wordsInLine = str.split("\\s+");//Cualquier tipo de espacio
    
                        // Contar palabras
                        wordCount += wordsInLine.length;
    
                        // Verificamos si hay match
                        for (String word : wordsInLine) {
                            for(int i = 0; i<words.length; i++){
                                if(word.equals(words[i])){ 
                                    matches[i] += 1;
                                }
                            }
                        }
                    }
                    //Guardamos
                    Book book = new Book(name, wordCount, words, matches);
                    answers.addBook(book);

                    if (filereader != null) { // Cerramos todo
                        filereader.close();
                    }
                } else if(counter>max){ //Legamos al límite, no es necesario continuar
                    break;
                }
                counter++;
            }
        }catch (Exception ex){ex.printStackTrace();}

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