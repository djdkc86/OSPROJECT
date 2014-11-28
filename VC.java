/****************************
 Author: Thomas Wassum
 Date: Only with a girl
 Purpse: To create a controller
 for a distributed network of 
 computers
 ****************************/
import java.io.*;
import java.net.*;
import java.util.logging.*;
import java.util.Vector;

public class VC extends Thread{
    //fields
    public ServerSocket ss;
    protected Socket computerSocket;
    private int[] IDs;
    private int[] ports;
    private static Logger log;
    private static FileHandler fh;
    private int size;
    private int port;
    private boolean stop;
    SimpleFormatter formatter;
    PrintWriter out;
    BufferedReader in;
    //constuctors
    public VC(int size) throws IOException{
        this.size = size;
        this.IDs = new int[size];
        this.port = 10007;
        ss = null;
        computerSocket = null;
        stop = false;
        out = null;
        in = null;
        logging();
        timeToRun();
        System.exit(1);
    }
    
    private VC(Socket compSock){
        computerSocket = compSock;
        start();
    }
    //methods
    public void logging() throws IOException{
        log = Logger.getLogger("LogFile");
        fh = new FileHandler("Controller.txt");
        log.addHandler(fh);
        formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
    }
    
    private int count = 0;
    public void timeToRun() throws IOException{
        try{
            ss = new ServerSocket(0);
            this.port = ss.getLocalPort();
            log.info("This port: "+this.port);
            try{
                while(true){
                    log.info("waiting for connection");
                    new VC(ss.accept());
                }
            }catch(IOException e){
                log.warning("couldn't listen");
                System.exit(1);
            }
        }catch(IOException e){
            log.warning("couldn't listen either");
            System.exit(1);
        }
        try{
            computerSocket = ss.accept();
        }catch(IOException e){
            log.warning("Accept failed");
            System.exit(1);
        }
        log.info("Connection successful (1)");
        log.info("waiting for input.....(1)");
        out = new PrintWriter(computerSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(computerSocket.getInputStream()));
        String inputLine = in.readLine();
        while(!(inputLine.equalsIgnoreCase("bye"))){
            log.info("incoming message: "+inputLine);
            out.println("bye");
            inputLine = in.readLine();
            if(inputLine.equalsIgnoreCase("bye")){
                log.info("connection closed");
            }
        }
        out.close();
        in.close();
        computerSocket.close();
        ss.close();
    }
    String array;
    @Override
    public void run(){
        try{
            count++;
            out = new PrintWriter(computerSocket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(computerSocket.getInputStream()));
            String inputLine;
            while((inputLine = in.readLine()) != null){
                log.info("Incoming message: "+inputLine);
                String[] ary = inputLine.split(" ");
                if(count <= this.size) IDs[count-1] = Integer.parseInt(ary[3]);
                array = array + ary[3] + " ";
                log.info(array);
                out.println("Got it");
            }
            out.close();
            in.close();
            computerSocket.close();
        }catch(IOException e){
            log.info("couldn't listen");
            System.exit(1);
        }
    }
    
    public int getPort(){
        return this.port;
    }
    
    //check for open socket
    
    //add to vector of ints for ids
    
    //check to make sure ID is unique
    public static void main(String[] args) throws IOException{
        new VC(Integer.parseInt(args[0]));
    }
    
}