package project.src;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

public class Server extends Thread {

    protected Socket clientSocket;//enables listening to client systems
    protected static ArrayList<Integer> clientIDs = new ArrayList<>();
    protected static ArrayList<Integer> clientPORTs = new ArrayList<>();
    private static int count = 0; //clients systems initialized to zero
    Random random;// = new Random();

    public static void main(String[] args) throws IOException {

        /***********************************************************
         **
         **   Initialize ServerSocket and tell it to wait for
         **   connections from client systems
         **
         ************************************************************/
        if (args.length > 0) {
            Integer.parseInt(args[0]);
        }

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(10007);
            System.out.println("Connection Socket Created");
            try {
                while (true) {//keep making connections as needed
                    System.out.println("waiting for clients...");
                    new Server(serverSocket.accept()); //run() method called
                }
            } catch (IOException e) {
                System.err.println("Could not listen on port: 10007.");
                System.exit(1);
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port: 10008.");
            System.exit(1);
        }

        Socket clientSocket = null;
        System.out.println("Waiting for connection.....");

        try {
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
        }

        /*****************************************************
         **
         **   Initialize ServerSocket and tell it to wait for
         **   connections from client systems
         **
         ******************************************************/
        PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(System.in));
        String server_says;
        boolean done = false;

        while ((server_says = bufferedReader2.readLine()) != null) {
            System.out.println("server_writer: " + server_says);
            printWriter.println(server_says);
            if (server_says.equalsIgnoreCase("Bye")) break;
        }

        printWriter.close();
        bufferedReader.close();
        clientSocket.close();
        serverSocket.close();
    }

    protected Server(Socket clientSoc) {
        clientSocket = clientSoc;
        start();
    }


    /**
     * ************************************************************
     * *
     * *   The run() method contains the server/client interaction;
     * *   the method is called automatically when a successful
     * *   connection occurs between server/client sockets
     * *
     * ***************************************************************
     */
    @Override
    public void run() {
        count++;
        try {
            PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String client_says = bufferedReader.readLine();
            if (!getData(client_says)) {
                printWriter.println("sorry, invalid user ID ");
                clientSocket.close();
            } else if (count == 1) {
                printWriter.println("@@" + clientPORTs.get(0));
            } else {
                int neighbor = count - 2;
                int c2cPORT = clientPORTs.get(neighbor);
                printWriter.println("@@" + c2cPORT);
            }

            printWriter.println("your connection looks good and is being processed...");
//            while ((client_says = bufferedReader.readLine()) != null) {
//                if(!client_says.isEmpty()){
//                    System.out.println("received MSG: " + client_says);}
//                printWriter.println(client_says);
//                if (client_says.trim().equalsIgnoreCase("Bye")) break;
//            }

            printWriter.close();
            bufferedReader.close();
            clientSocket.close();

        } catch (IOException e) {
            System.err.println("Could not listen on port: 10008.");
            System.exit(1);
        }
    }


    public boolean getData(String s) {
        boolean okayID = false;
        boolean okayPORT = false;

        StringTokenizer st = new StringTokenizer(s, "@@");
        String[] vals = new String[2];
        vals[0] = st.nextToken();
        vals[1] = st.nextToken();

        Integer temp = Integer.parseInt(vals[0]); //ID
        Integer temp2 = Integer.parseInt(vals[1]); //connection port


        if (validID(temp)) {
            clientIDs.add(temp);
            okayID = true;
        }

        if (validPORT(temp2)) {
            clientPORTs.add(temp2);
            okayPORT = true;
        }

        System.out.println("just added client " + (clientIDs.indexOf(temp) + 1) +
                " with ID " + temp + " and port: " + temp2);
        return okayID && okayPORT;
    }

    private boolean validPORT(int possible_port) {
        try (ServerSocket ignored = new ServerSocket(possible_port)) {
            return false;
        } catch (IOException ignored) {
            for (Integer taken : clientPORTs) {
                if (possible_port == taken) return false;
            }
            return true;
        }
    }

    private boolean validID(int ID) {
        for (Integer id : clientIDs) {
            if (ID == id) return false;
        }
        return true;
    }
}





// import java.io.*;
// import java.net.*;

// public class EchoServer extends Thread { 
    
//     protected Socket clientSocket;
//  public static void main(String[] args) throws IOException 
//   { 
//     ServerSocket serverSocket = null; 

//     try { 
//          serverSocket = new ServerSocket(10007); 
//          System.out.println("Connection Socket Created");
//          try{
//              while (true){
//                  System.out.println("waiting for connection");
//                  new EchoServer(serverSocket.accept());
//              }
//          }catch (IOException e) 
//         { 
//          System.err.println("Could not listen on port: 10007."); 
//          System.exit(1); 
//         } 
//         } 
//     catch (IOException e) 
//         { 
//          System.err.println("Could not listen on port: 10008."); 
//          System.exit(1); 
//         } 

//     Socket clientSocket = null; 
//     System.out.println ("Waiting for connection.....");

//     try { 
//          clientSocket = serverSocket.accept(); 
//         } 
//     catch (IOException e) 
//         { 
//          System.err.println("Accept failed."); 
//          System.exit(1); 
//         } 

//     System.out.println ("Connection successful");
//     System.out.println ("Waiting for input.....");

//     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), 
//                                       true); 
//     BufferedReader in = new BufferedReader( 
//             new InputStreamReader( clientSocket.getInputStream())); 

//     String inputLine; 
//     boolean done = false;
//     while ((inputLine = in.readLine()) != null) 
//         { 
//          System.out.println ("Server: " + inputLine); 
//          out.println(inputLine); 

//          if (inputLine.equalsIgnoreCase("Bye")) 
//              break; 
//         } 

//     out.close(); 
//     in.close(); 
//     clientSocket.close(); 
//     serverSocket.close(); 
//   } 
//  private EchoServer(Socket clientSoc){
//      clientSocket = clientSoc;
//      start();
     
//  }
//  private static int count = 0;
//  @Override
//  public void run()
//  {
//      count++;
//      System.out.println("New Comm Thread Started");
//      System.out.println("This is Customer: "+count);
     
//      try{
//          PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), 
//                                       true); 
//         BufferedReader in = new BufferedReader( 
//             new InputStreamReader( clientSocket.getInputStream())); 

//     String inputLine; 
//     boolean done = false;
//     while ((inputLine = in.readLine()) != null) 
//         { 
//          System.out.println ("Server: " + inputLine); 
//          out.println(inputLine); 

//          if (inputLine.equalsIgnoreCase("Bye")) 
//              break; 
//         } 

//     out.close(); 
//     in.close(); 
//     clientSocket.close(); 
//     //serverSocket.close(); 
//      }catch (IOException e) 
//         { 
//          System.err.println("Could not listen on port: 10008."); 
//          System.exit(1); 
//         } 
//  }
// }
