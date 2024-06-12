import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class Aggregator {
    private WebClient webClient;

    public Aggregator() {
        this.webClient = new WebClient();
    }

    //Recibe la lista de las direcciones
    public List<String> sendTasksToWorkers(List<String> workersAddresses) {
        //Futures es para las tareas asincronas
        CompletableFuture<String>[] futures = new CompletableFuture[workersAddresses.size()];

        for (int i = 0; i < workersAddresses.size(); i++) {
            //Se obtiene la direccion
            String workerAddress = workersAddresses.get(i);
            //Se envia y asocia a cada future
            futures[i] = webClient.sendTask(workerAddress);
        }

        boolean bandera = true;
        // Evalúa continuamente si uno de los servidores ha terminado.
        while(bandera){
            for(int j = 0; j < workersAddresses.size(); j++){
                if (true == futures[j].isDone())
                    bandera = false;
            }
        }

        //lista de resultados asíncronos
        List<String> results = new ArrayList();
        for (int i = 0; i < workersAddresses.size(); i++) {
            results.add(futures[i].join());
        }

        return results;
    }
}
