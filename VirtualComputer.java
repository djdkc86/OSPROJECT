import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.logging.*;

public class VirtualComputer extends Thread{
    public ServerSocket serverSocket;
    protected Socket neighborSocket;
    public Socket controllerSocket;
    private int size;
    private int []time;
    private int ID;
    private int position;
    private int controllerSocketPortNumber;
    private static Logger log;
    private static FileHandler fh;
    private final String NAME = "127.0.0.1";
    PrintWriter out;
    BufferedReader in;
    SimpleFormatter formatter;
    
    public VirtualComputer() throws IOException{
        serverSocket = null;
        ID = 0;
        position = -1;
        logging();
    }
    
    public VirtualComputer (int control, int position, int size) throws IOException {
        this.ID = createID();
        this.controllerSocketPortNumber = control;
        logging();
        log.info("Computer ID: "+this.ID);
        this.position = position;
        controllerSocket = null;
        out = null;
        in = null;
        this.size = size;
        this.time = new int[this.size];
        for(int i = 0;i<this.size;i++){
            if(this.position == i+1) time[i] = 1;
            else time[i] = 0;
        }
        createPort();
        test();
        System.exit(1);
    }
    
    public void logging() throws IOException{
        String fileName = "VirtualComputer"+this.ID+".txt";
        log = Logger.getLogger("LogFile");
        fh = new FileHandler(fileName);
        log.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
    }
    private void createPort() throws IOException{
        this.serverSocket = new ServerSocket(0);
        log.info("Socket for this computer: "+Integer.toString(serverSocket.getLocalPort()));
    }
    private int createID(){
        Random rand = new Random();
        return Math.abs(rand.nextInt());
    }
    
    public void test() throws IOException{
        try{
            controllerSocket = new Socket(NAME, this.controllerSocketPortNumber);
            out = new PrintWriter(controllerSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(controllerSocket.getInputStream()));
        }catch(UnknownHostException e){
            log.warning("don't know about host");
            System.exit(1);
        }catch(IOException e){
            log.warning("couldn't do IO");
            System.exit(1);
        }
        
        String timestamp = "<";
        for(int i = 0;i<this.size;i++){
            if(i==this.size-1) timestamp = timestamp+time[i];
            else timestamp = timestamp + time[i] +",";
        }
        timestamp = timestamp + ">";
        String fromControl;
        log.info(timestamp);
        out.println("This is computer "+this.ID);
        fromControl = in.readLine();
        log.info("from control: "+fromControl);
        
        out.close();
        in.close();
        controllerSocket.close();
    }
    
    public static void main(String[] args) throws IOException{
        if(args.length != 3){
            System.out.println("Need some args");
            System.exit(1);
        }
        new VirtualComputer(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        
    }
}