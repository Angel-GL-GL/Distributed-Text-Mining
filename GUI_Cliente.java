import javax.swing.*;
import java.awt.*;

public class GUI_Cliente extends JFrame {
    public JButton searchButton;
    private static int alto, ancho;
    private Container container;
    private JPanel headerPanel, mainPanel;
    private JTextField inputTextField;
    private JTextArea answerArea;
    private JScrollPane jScrollPane;
    
    public GUI_Cliente(){
        // Dimensiones
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        ancho = pantalla.width;
        alto = pantalla.height;
        //Container
        container = getContentPane();
        container.setLayout(new BorderLayout());
        //JPanels
        headerPanel = new JPanel(new BorderLayout());
        mainPanel = new JPanel(new BorderLayout());
        //JButtom
        searchButton = new JButton("Buscar");
        //JTextField
        inputTextField = new JTextField();
        //JTextArea
        answerArea = new JTextArea();
        answerArea.setEditable(false);
        //JScrollPane
        jScrollPane = new JScrollPane(new JPanel().add(answerArea));
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //Agregar los elementos de la GUI
        headerPanel.add(searchButton, BorderLayout.EAST);
        headerPanel.add(inputTextField, BorderLayout.CENTER);
        mainPanel.add(jScrollPane, BorderLayout.CENTER);
        container.add(headerPanel, BorderLayout.SOUTH);
        container.add(mainPanel, BorderLayout.CENTER);
    }

    //Iniciamos
    public void startView() {       
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(0,0,ancho,alto);
        setTitle("Mineria de Texto");
        setVisible(true);
    }
    //Boton de Buscar
    public String search(){
        return inputTextField.getText();
    }
    //Resetear el input
    public void resetFields(){
        inputTextField.setText("");
    }
    //Agregamos texto al JTextArea
    public void addAnswer(String txt){
        answerArea.setText(txt);
    }
}
