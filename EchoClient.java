import java.io.*;
import java.net.*;

public class EchoClient {
    public static void main(String[] args) throws IOException {
        
        ServerSocket serverSocker = null;
        String serverHostname = new String ("127.0.0.1");
        
        if (args.length > 0)
            serverHostname = args[0];
        System.out.println ("Attemping to connect to host " +
                            serverHostname + " on port 10007.");
        
        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        
        try {
            serverSocket = new ServerSocket(10008);
        }
        catch (IOException e)
        {
            System.err.println("Could not listen on port: 10008.");
            System.exit(1);
        }

        
        try {
            echoSocket = new Socket(serverHostname, 10007);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(
                                                          echoSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + serverHostname);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                               + "the connection to: " + serverHostname);
            System.exit(1);
        }
        
        BufferedReader stdIn = new BufferedReader(
                                                  new InputStreamReader(System.in));
        String userInput;
        
        System.out.print ("input: ");
        
        while ((userInput = stdIn.readLine()) !=  null) {
            out.println(userInput);
            try{
                System.out.println("echo: " + in.readLine());
                if(in.readLine().equalsIgnoreCase("Bye")) {System.out.println("Socket will be closed!");break;}
            }
            catch (Exception e)
            {
                System.out.println("Socket Closed!");
                break;
            }
            System.out.print ("input: ");
            
        }
        
        out.close();
        in.close();
        stdIn.close();
        echoSocket.close();
    }
}