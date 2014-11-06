import java.net.*;
import java.io.*;
import java.util.logging.*;
import java.util.HashSet;
public class Controller extends Thread {
    private HashSet<Integer> uniqueIDS = new HashSet<Integer>(10);
    protected Socket vcSocket;
    private int count = 0;
    Logger log;
    FileHandler fh;
    SimpleFormatter formatter;
    public Controller() throws IOException{
        ServerSocket serverSocket = null;
        logging();
        try{
            serverSocket = new ServerSocket(10007);
            log.info("Connection Socket Created");
            try{
                while(true){
                    log.info("waiting for connection");
                    new Controller(serverSocket.accept());
                }
            }catch (IOException e)
            {
                log.info("Could not listen on port: 10007.");
                System.exit(1);
            }
        }catch (IOException e){
            log.info("Could not listen on port: 10007.");
        }catch (SecurityException e){
            e.printStackTrace();
        }
        
        Socket vcSocket = null;
        log.info("Waiting for connection.....");
        try{
            vcSocket = serverSocket.accept();
        }catch (IOException e){
            log.info("Accept failed.");
            System.exit(1);
        }
        log.info("Connection successful");
        log.info("Waiting for input......");
        
        PrintWriter out = new PrintWriter(vcSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(vcSocket.getInputStream()));
        String inputLine;
        boolean done = false;
        while((inputLine = in.readLine()) != null){
            log.info("Controller: "+inputLine);
            out.println(inputLine);
            if(inputLine.equalsIgnoreCase("finish")) break;
        }
        out.close();
        in.close();
        vcSocket.close();
        serverSocket.close();
    }
    private Controller(Socket compSocket){
        vcSocket = compSocket;
        start();
    }
    public void logging() throws IOException{
        log = Logger.getLogger("LogFile");
        fh = new FileHandler("controllerLogFile.txt");
        log.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
    }

    @Override
    public void run()
    {
        count++;
        log.info("New Comm Thread Started");
        log.info("This is Customer: "+count);
        
        try{
            PrintWriter out = new PrintWriter(vcSocket.getOutputStream(),
                                              true);
            BufferedReader in = new BufferedReader(
                                                   new InputStreamReader( vcSocket.getInputStream()));
            
            String inputLine;
            boolean done = false;
            while ((inputLine = in.readLine()) != null)
            {
                log.info ("Server: " + inputLine);
                out.println(inputLine);
                
                if (inputLine.equalsIgnoreCase("Bye"))
                    break;
            }
            
            out.close();
            in.close();
            vcSocket.close();
            //serverSocket.close(); 
        }catch (IOException e) 
        { 
            System.err.println("Could not listen on port: 10007.");
            System.exit(1); 
        } 
    }

    /* FIX THIS !!!!!!!!!
    @Override
    public void run(){
        while(count < 10){
            count++;
            log.info("Comm thread started");
            log.info("This is computer: "+count);
            try{
                PrintWriter out = new PrintWriter(vcSocket.getOutputStream(),true);
                BufferedReader in = new BufferedReader(new InputStreamReader(vcSocket.getInputStream()));
                String inputLine;
                boolean done = false;
                boolean uid = false;
                while((inputLine = in.readLine())!= null){
                    do{
                        log.info("Controller: "+inputLine);
                        uid = uniqueIDS.add(Integer.parseInt(inputLine));
                        if(inputLine.equalsIgnoreCase("finish")) break;
                        if(!uid){
                            out.println("Number already in use. Choose another number.");
                            log.info("Number already in use. Choose another number.");
                        }
                    }while(!uid);
                }
                out.close();
                in.close();
                vcSocket.close();
            }catch(IOException e){
                log.info("Could not listen on port: 10007");
                System.exit(1);
            }
        }
    }
     */
    public static void main(String [] args) throws IOException{
        new Controller();
    }
    
}