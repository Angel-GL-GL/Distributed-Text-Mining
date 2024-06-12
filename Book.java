import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

//La vamos a utilizar para representar cada libro y sus resultados
public class Book implements java.io.Serializable{
    private String name;
    private int number;
    private HashMap<String, Integer> hashMap; //Palabras y resultado de cada una
    private double total;

    public Book(String name, int number, String[] words, Integer[] matches){
        hashMap = new HashMap<>();
        this.name = name;
        this.number = number;
        total = 0;
        //Ingresamos valores al hashMap
        for(int i = 0; i<words.length; i++) hashMap.put(words[i], matches[i]);
    }
    //TF
    public double getTF(String word){
        return hashMap.containsKey(word)?(double)hashMap.get(word)/(double)number:0;
    }
    //Es el TF*ITF del libro
    public void setTotal(int n, int[] nt, String[] words){
        total = 0;
        for(int i = 0; i<nt.length; i++){
            //ITF
            double itf = nt[i]>0? Math.log10((double)n/(double)nt[i]): 0;
            //TF
            double tf = getTF(words[i]);
            total += tf*itf;
        }
    }

    public String getName(){return name;}

    public double getTotal(){return total;}

    @Override
    public String toString() {
        return name + " : " + total; 
    }
}