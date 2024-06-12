import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//Es lo que se envia de los servidores al cliente, una lista con 
//Los resultados para cada libro
public class Answers implements java.io.Serializable{
    private ArrayList<Book> ansList;

    public Answers(){//Inicializamos
        ansList = new ArrayList<>();
    }

    public void addBook(Book book){//Agregamos libro
        ansList.add(book);
    }

    public ArrayList<Book> getAnsList(){//Obtenemos el AL
        return ansList;
    }
    //Fusión de listas
    public void fusionList(ArrayList<Book> ansL2){
        for(Book ans: ansL2){
            ansList.add(ans);
        }
    }
    //A cada elemento le definimos su TF*ITF
    public void setEachTotal(int n, int[] nt, String[] words){
        for(Book book: ansList){
            book.setTotal(n, nt, words);
        }
    }
    //Ordenamos del más grande al menos
    public void order(){
        Collections.sort(ansList, new Comparator<Book>() {
            @Override
            public int compare(Book ans1, Book ans2) {
                return Double.compare(ans2.getTotal(), ans1.getTotal());
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Respuestas obtenidas:\n");
        for (Book ans: ansList) sb.append(ans.toString()).append("\n");
        return sb.toString();
    }
}
