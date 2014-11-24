package project.src;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;


public class Client extends Thread {
    protected static int CONTROLLPORT = 10007;
    protected static int CLIENTCONNECTPORT;
    protected static int THISSERVERPORT = 10008;
    protected static int PEERSERVERPORT = 0;
    protected static LogicalClock clock;
    protected static String SERVERHOSTNAME = "127.0.0.1";
    protected static int ID;
    protected static Socket socketToControl = null;
    protected static PrintWriter printWriter = null;
    protected static BufferedReader bufferedReader = null;
    protected static BufferedReader bufferedReader2;
    protected static String outGoingMSG = "";
    protected static String inComingMSG = "";
    protected static Socket socket;//enables listening to client systems
    protected static ArrayList<Integer> clientids = new ArrayList<>();
//    protected static ArrayList<Integer> clientports = new ArrayList<>();
    protected static int clientcount = 0; //clients systems initialized to zero
//    protected static Socket clientSocket;//enables listening to client systems
//    protected static ArrayList<Integer> clientIDs = new ArrayList<>();
//    protected static ArrayList<Integer> clientPORTs = new ArrayList<>();
     static Boolean firstSystem = true;

    public static void main(String[] args) throws IOException {

        //set up some datas...
        Random rand = new Random();
        ID = Math.abs(rand.nextInt(10000));
        clock = new LogicalClock(ID);
        System.out.println(clock.getTimeStamp());
        
        connectToController(CONTROLLPORT);

        makePeerServerSocket(THISSERVERPORT);
        if (!firstSystem) {
            connectToController(CLIENTCONNECTPORT);
        }
    }

    private static void makePeerServerSocket(int port) throws IOException {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
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
        //Server Socket for peer-to-peer conections
        PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(),true);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(System.in));
        String server_says; boolean done = false;

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

    /**create and initialize a socket that can exchange with server */
    private static boolean connectToController(int port) throws IOException {
        try {
            socketToControl = new Socket(SERVERHOSTNAME, port);
            printWriter = new PrintWriter(socketToControl.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(socketToControl.getInputStream()));
            System.out.println("Connected to " + SERVERHOSTNAME + " :: " + port);
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + SERVERHOSTNAME);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for " + "the connection to: " + SERVERHOSTNAME);
            System.exit(1);
        }

        bufferedReader2 = new BufferedReader(new InputStreamReader(System.in));
        String sendthis = "";
         sendthis += ID;
         sendthis += "@";
        int temp_port = getValidPort();
        sendthis += temp_port;
        System.out.println("sending data: "+sendthis);
        printWriter.println(sendthis);
        inComingMSG = bufferedReader.readLine();
        if (inComingMSG.contains("@@")) {
            getClientConnectPort(inComingMSG);
        }
        while (outGoingMSG != null) {
            clock.tickTock();
            printWriter.println(outGoingMSG);
            try {
                inComingMSG = bufferedReader.readLine();//read the server says.....
                if (!inComingMSG.isEmpty()) {
                    System.out.println("incoming MSG: " + inComingMSG);
                }
                if (inComingMSG.trim().equalsIgnoreCase("End")) {
                    System.err.println("connection terminated by server");
                    break;
                }
                if (inComingMSG.contains("@@")) {
                    CLIENTCONNECTPORT = getClientConnectPort(inComingMSG);
                    if (CLIENTCONNECTPORT!=THISSERVERPORT)firstSystem=false;
                    makePeerServerSocket(THISSERVERPORT);
                    return true;
                }
            } catch (Exception e) {
                System.out.println("Socket Closed!");
                return false;

            }
            System.out.print("say anything: ");
            outGoingMSG = bufferedReader2.readLine();

        }
        printWriter.close();
        bufferedReader.close();
        bufferedReader2.close();
        socketToControl.close();
        return false;

    }

    protected Client(Socket clientSoc) {
        socket = clientSoc;
        start();
    }

    /***************************************************************
     **
     **  Controller/Server Socket Run()
     **
     *****************************************************************/
    @Override
    public void run() {
        clientcount++;
        try {  PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),true);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String client_says = bufferedReader.readLine();

            while ((client_says = bufferedReader.readLine()) != null) {
                if(!client_says.isEmpty()){
                    System.out.println("received MSG: " + client_says);}
                printWriter.println(client_says);
                if (client_says.trim().equalsIgnoreCase("Bye")) break;
            }
            printWriter.close();
            bufferedReader.close();
            socket.close();
            //serverSocket.close();

        } catch (IOException e) {
            System.err.println("Could not listen on port: 10008.");
            System.exit(1);
        }
    }
    public static int getValidPort() throws IOException {
        Random ran = new Random();
        int possible = Math.abs(ran.nextInt(1000));
        boolean thisworks = false;
        while (!thisworks) {
            try (ServerSocket ignored = new ServerSocket(possible)) {
                thisworks = false;
                possible = Math.abs(ran.nextInt(10007));
            } catch (IOException ignored) {
                thisworks = true;
            }
        }
        THISSERVERPORT = possible;
        return possible;
    }
    private static int getClientConnectPort(String inComingMSG) {
        Integer temp = Integer.parseInt(inComingMSG.substring(2));//remove @@
        if (temp!=THISSERVERPORT)
        {
            PEERSERVERPORT=temp;
            System.out.println("just added need to connect port-->"+temp);
        }else{
            System.out.println("this was the first system to join; no peerSocket port assigned yet!");
        }
        return temp;
    }
}

// import java.io.*;
// import java.net.*;
// import java.util.Random;

// public class EchoClient {
//     public static void main(String[] args) throws IOException {
        
//         Random rand = new Random();
//         String serverHostname = new String ("127.0.0.1");
        
//         int ID = rand.nextInt();
//         ID = Math.abs(ID);
//         if (args.length > 0)
//             serverHostname = args[0];
//         System.out.println ("Attemping to connect to host " +
//                             serverHostname + " on port 10007.");
        
//         Socket echoSocket = null;
//         PrintWriter out = null;
//         BufferedReader in = null;
        
//         try {
//             // echoSocket = new Socket("taranis", 7);
//             echoSocket = new Socket(serverHostname, 10007);
//             out = new PrintWriter(echoSocket.getOutputStream(), true);
//             in = new BufferedReader(new InputStreamReader(
//                                                           echoSocket.getInputStream()));
//         } catch (UnknownHostException e) {
//             System.err.println("Don't know about host: " + serverHostname);
//             System.exit(1);
//         } catch (IOException e) {
//             System.err.println("Couldn't get I/O for "
//                               + "the connection to: " + serverHostname);
//             System.exit(1);
//         }
        
//         BufferedReader stdIn = new BufferedReader(
//                                                   new InputStreamReader(System.in));
//         String userInput;
        
//         System.out.print ("input: ");
//         out.println(ID);
//         while ((userInput = stdIn.readLine()) !=  null) {
//             out.println(userInput);
//             try{
//                 System.out.println("echo: " + in.readLine());
//                 //if(in.readLine().equalsIgnoreCase("End")) {System.out.println("Socket will be closed!");break;}
//             }
//             catch (Exception e)
//             {
//                 System.out.println("Socket Closed!");
//                 break;
//             }
//             System.out.print ("input: ");
            
//         }
        
//         out.close();
//         in.close();
//         stdIn.close();
//         echoSocket.close();
//     }
// }
