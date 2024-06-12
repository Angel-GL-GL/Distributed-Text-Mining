import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;

//Monitoreo de los servidores
public class GUI_Monitor extends JFrame {
    private static int alto, ancho;
    private static ArrayList<String>[] graphs1; 
    private static ArrayList<String>[] graphs2;
    private static ArrayList<String> maxMem;
    private static double c1, c2, ct;

    public static void main(String[] args) {
        c1 = 0.0;
        c2 = 0.0;
        ct = 0.0;
        graphs1 = new ArrayList[4];
        graphs2 = new ArrayList[4];
        maxMem = new ArrayList<>();
        //Inicializamos los AL con "0"
        for(int i = 0; i<4; i++){
            ArrayList<String> al1 = new ArrayList<>();
            ArrayList<String> al2 = new ArrayList<>();
            for(int j = 0; j<10; j++){
                al1.add("0");
                al2.add("0");
            }
            graphs1[i] = al1;
            graphs2[i] = al2;
            
        }
        String[] address = { "localhost:8090", "localhost:8091", "localhost:8092", "localhost:8093" };
        // Address
        if (args.length == 4) {
            address[0] = args[0];
            address[1] = args[1];
            address[2] = args[2];
            address[3] = args[3];
        }

        for (int i = 0; i < 4; i++) {
            address[i] = "http://" + address[i] + "/status";
        }
        // Dimensiones
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        ancho = pantalla.width;
        alto = pantalla.height;

        try {
            GUI_Monitor last = null; //es la que se va a mostrar
            boolean flag = false;
            while (true) {
                if(flag){  //A partir del segundo ciclo se cumple
                    last.setVisible(true);
                    last.setLocation(0, 0);
                    
                }
                Aggregator aggregator = new Aggregator();
                 //Envio de datos
                List<String> results = aggregator.sendTasksToWorkers(Arrays.asList(address));
                //Resultados
                for (int i = 0; i<results.size(); i++) {
                    String[] res = results.get(i).split("@");
                    //Memoria máxima para cada servidor
                    maxMem.add(res[0].substring(0,res[0].length()-2));
                    //Memoria ocupada en este momento
                    String memory = 
                    (
                        Double.parseDouble(res[0].substring(0,res[0].length()-2))
                        - Double.parseDouble(res[1].substring(0,res[1].length()-2))
                    )
                    +"";
                    //Quitamos el primer elemento, ya que vamos a estar recorriendo
                    graphs1[i].remove(0);
                    graphs2[i].remove(0);
                    //Los guardamos
                    graphs1[i].add(res[2]);
                    graphs2[i].add(memory);
                }
                
                GUI_Monitor gui_Monitor = new GUI_Monitor();
                gui_Monitor.setVisible(true);
                Thread.sleep(500); //Primer sleep
                gui_Monitor.setVisible(false);
                if(flag){ //A partir del segundo ciclo
                    Thread.sleep(1000);
                    last.dispose(); //Quitamos
                }
                flag = true; //Ya tenemos al menos un ciclo
                last = gui_Monitor; //Es la que mostraremos en el siguiente
                gui_Monitor.dispose();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public GUI_Monitor() { // Constructor, donde decimos que la pantalla se abre al maximo, si se cierra se
        // detiene el programa e inicializamos un panel
        setBounds(0, 0, ancho, alto); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Panel  p = new Panel();
        add(p);
    }

    private class Panel  extends JPanel { // El panel de dibujo
        @Override
        public void paintComponent(Graphics g) {
            //Costo por unidad de tiempo (en este caso centavos cada 1.5 segundos)
            double cost = ((double)(0.01)*(double)(1.5)/(double)(3600.00))*100.0;
            super.paintComponent(g);
            //Factor f1 para costo 1
            double[] f1 = {
                Double.parseDouble(graphs1[0].get(9)),
                Double.parseDouble(graphs1[1].get(9)),
                Double.parseDouble(graphs1[2].get(9)),
                Double.parseDouble(graphs1[3].get(9)),
            };
            //Factor f2 para costo 2
            double[] f2 = {
                1.0-f1[0],
                1.0-f1[1],
                1.0-f1[2],
                1.0-f1[3],
            };
            //Costos totales hasta ahorita
            double cost1 = 
                f1[0]*cost + f1[1]*cost + f1[2]*cost + f1[3]*cost;

            double cost2 = 
                f2[0]*cost + f2[1]*cost + f2[2]*cost + f2[3]*cost;

            c1 += cost1;
            c2 += cost2;
            ct += 4*cost;
            
            // Textos
            g.drawString("Servidor Principal", ancho / 90, alto / 50);
            g.drawString("Servidor Procesamiento 1", 21 * ancho / 90, alto / 50);
            g.drawString("Servidor Procesamiento 2", 41 * ancho / 90, alto / 50);
            g.drawString("Servidor Procesamiento 3", 61 * ancho / 90, alto / 50);
            g.drawString("Costos", 81 * ancho / 90, alto / 50);
            g.drawString("Historial del Uso de CPU", ancho / 90, 3 * alto / 50);
            g.drawString("¢"+c1, 70 * ancho / 90, 5 * alto / 50-2);
            g.drawString("Historial del Uso de memoria", ancho / 90, 23 * alto / 50);
            g.drawString("¢"+c2, 70 * ancho / 90, 25 * alto / 50-2);
            g.drawString("Total:", 65 * ancho / 90, 45 * alto / 50);
            g.drawString("¢"+ct, 70 * ancho / 90, 45 * alto / 50);

            // Área 1.1
            g.drawRect(1 * ancho / 90, 5 * alto / 50, 19 * ancho / 90, 15 * alto / 50);
            paintGraph1(1 * ancho / 90, (5 * alto / 50) + (15 * alto / 50),  5 * alto / 50, (19 * ancho / 90)/10, ((20 * alto / 50)), graphs1[0],g);
            // Área 1.2
            g.drawRect(21 * ancho / 90, 5 * alto / 50, 19 * ancho / 90, 15 * alto / 50);
            paintGraph1(21 * ancho / 90, (5 * alto / 50) + (15 * alto / 50),  5 * alto / 50, (19 * ancho / 90)/10, ((20 * alto / 50)), graphs1[1],g);
            // Área 1.3
            g.drawRect(41 * ancho / 90, 5 * alto / 50, 19 * ancho / 90, 15 * alto / 50);
            paintGraph1(41 * ancho / 90, (5 * alto / 50) + (15 * alto / 50),  5 * alto / 50, (19 * ancho / 90)/10, ((20 * alto / 50)), graphs1[2],g);
            // Área 1.4
            g.drawRect(61 * ancho / 90, 5 * alto / 50, 19 * ancho / 90, 15 * alto / 50);
            paintGraph1(61 * ancho / 90, (5 * alto / 50) + (15 * alto / 50),  5 * alto / 50, (19 * ancho / 90)/10, ((20 * alto / 50)), graphs1[3],g);
            // Área 2.1
            g.drawRect(1 * ancho / 90, 25 * alto / 50, 19 * ancho / 90, 15 * alto / 50);
            paintGraph2(Double.parseDouble(maxMem.get(0)), 1 * ancho / 90, (25 * alto / 50) + (15 * alto / 50),  25 * alto / 50, (19 * ancho / 90)/10, ((20 * alto / 50)), graphs2[0],g);
             // Área 2.2
            g.drawRect(21 * ancho / 90, 25 * alto / 50, 19 * ancho / 90, 15 * alto / 50);
            paintGraph2(Double.parseDouble(maxMem.get(1)), 21 * ancho / 90, (25 * alto / 50) + (15 * alto / 50),  25 * alto / 50, (19 * ancho / 90)/10, ((20 * alto / 50)), graphs2[1],g);
            // Área 2.3
            g.drawRect(41 * ancho / 90, 25 * alto / 50, 19 * ancho / 90, 15 * alto / 50);
            paintGraph2(Double.parseDouble(maxMem.get(2)), 41 * ancho / 90, (25 * alto / 50) + (15 * alto / 50),  25 * alto / 50, (19 * ancho / 90)/10, ((20 * alto / 50)), graphs2[2],g);
            // Área 2.4
            g.drawRect(61 * ancho / 90, 25 * alto / 50, 19 * ancho / 90, 15 * alto / 50);
            paintGraph2(Double.parseDouble(maxMem.get(3)), 61 * ancho / 90, (25 * alto / 50) + (15 * alto / 50),  25 * alto / 50, (19 * ancho / 90)/10, ((20 * alto / 50)), graphs2[3],g);
        }
        //Gráficas del área 1
        private void paintGraph1(double lastX, double lastY, double maxPosY, double incrementX, double incrementY, ArrayList<String> arrayList, Graphics g){ 
            for(String str: arrayList){
                double maxV = 100.0;
                double currentV = Double.parseDouble(str)*10;
                double currentPosY = currentV*maxPosY/maxV; // regla de tres
                g.drawLine((int)lastX, (int)lastY, (int)lastX+(int)(incrementX), (int)(incrementY) - (int)currentPosY);
                lastX += incrementX;
                lastY = incrementY - currentPosY;
            }
        }
        //Gráficas del área 2
        private void paintGraph2(double maxV, double lastX, double lastY, double maxPosY, double incrementX, double incrementY, ArrayList<String> arrayList, Graphics g){ 
            for(String str: arrayList){
                double currentV = Double.parseDouble(str);
                double currentPosY = currentV*maxPosY/maxV; //regla de tres
                g.drawLine((int)lastX, (int)lastY, (int)lastX+(int)(incrementX), (int)(2*incrementY) - (int)currentPosY);
                lastX += incrementX;
                lastY = 2*incrementY - currentPosY;
            }
        }
    }
}
