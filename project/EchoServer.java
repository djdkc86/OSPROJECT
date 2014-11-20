import java.io.*;
import java.net.*;

public class EchoServer extends Thread { 
    
    protected Socket clientSocket;
 public static void main(String[] args) throws IOException 
   { 
    ServerSocket serverSocket = null; 

    try { 
         serverSocket = new ServerSocket(10007); 
         System.out.println("Connection Socket Created");
         try{
             while (true){
                 System.out.println("waiting for connection");
                 new EchoServer(serverSocket.accept());
             }
         }catch (IOException e) 
        { 
         System.err.println("Could not listen on port: 10007."); 
         System.exit(1); 
        } 
        } 
    catch (IOException e) 
        { 
         System.err.println("Could not listen on port: 10008."); 
         System.exit(1); 
        } 

    Socket clientSocket = null; 
    System.out.println ("Waiting for connection.....");

    try { 
         clientSocket = serverSocket.accept(); 
        } 
    catch (IOException e) 
        { 
         System.err.println("Accept failed."); 
         System.exit(1); 
        } 

    System.out.println ("Connection successful");
    System.out.println ("Waiting for input.....");

    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), 
                                      true); 
    BufferedReader in = new BufferedReader( 
            new InputStreamReader( clientSocket.getInputStream())); 

    String inputLine; 
    boolean done = false;
    while ((inputLine = in.readLine()) != null) 
        { 
         System.out.println ("Server: " + inputLine); 
         out.println(inputLine); 

         if (inputLine.equalsIgnoreCase("Bye")) 
             break; 
        } 

    out.close(); 
    in.close(); 
    clientSocket.close(); 
    serverSocket.close(); 
   } 
 private EchoServer(Socket clientSoc){
     clientSocket = clientSoc;
     start();
     
 }
 private static int count = 0;
 @Override
 public void run()
 {
     count++;
     System.out.println("New Comm Thread Started");
     System.out.println("This is Customer: "+count);
     
     try{
         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), 
                                      true); 
        BufferedReader in = new BufferedReader( 
            new InputStreamReader( clientSocket.getInputStream())); 

    String inputLine; 
    boolean done = false;
    while ((inputLine = in.readLine()) != null) 
        { 
         System.out.println ("Server: " + inputLine); 
         out.println(inputLine); 

         if (inputLine.equalsIgnoreCase("Bye")) 
             break; 
        } 

    out.close(); 
    in.close(); 
    clientSocket.close(); 
    //serverSocket.close(); 
     }catch (IOException e) 
        { 
         System.err.println("Could not listen on port: 10008."); 
         System.exit(1); 
        } 
 }
}