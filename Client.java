package project.src;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class Client extends Thread {

    protected static int NETWORK_SIZE;
    protected static int SERVER_PORT = 10007;
    protected static int NEIGHBOR_PORT;
    protected static int THIS_CLIENT_PORT;
    protected static int position;
    protected static int[] clock;// = new int[11];
    protected static int[] IDS;// = new int[10];
    protected static ArrayList<Integer> ports = new ArrayList<>();
    protected static String SERVER_HOST_NAME = "127.0.0.1";
    //you realize this is redundant right???
    protected static String THIS_HOST_NAME = "127.0.0.1";
    protected static Integer ID;
    protected static Socket socketToServer = null;
    protected static PrintWriter printWriter1 = null;           //connect to server
    protected static PrintWriter printWriter2 = null;           //connect to peer
    protected static BufferedReader bufferedReader1 = null;     //connect to server
    protected static BufferedReader bufferedReader2 = null;     //connect to peer
    protected static String outGoingMSG = "";
    protected static String inComingMSG = "";
    protected Socket clientSocket;//enables listening to client systems
    protected static boolean isUnique = false;
    protected static boolean isHead = false;
    protected static boolean isTail = false;
    protected static boolean isReady = false;
    private static Logger log;
    private static FileHandler fh;
    static SimpleFormatter formatter;
    static Thread listen;

    public static void main(String[] args) throws Exception {

        //logging();  //only use this when everything is ready to roll, otherwise a zillion log file you will have
        ID = Integer.parseInt((String.valueOf(Calendar.getInstance().getTimeInMillis())).substring(6));
        //clock = new LogicalClock(ID);
        //System.out.println(clock.getTimeStamp());

        connectToController(SERVER_PORT);

        listen = new Thread() {
            @Override
            public void run() {
                try {
                    beAServer(THIS_CLIENT_PORT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        try {
            listen.start();//this thread opens socket to listen for messages from peers
            if (isHead) {
                Thread.sleep(5000); //listen before talking
            }

            //log.info("Thread: "+Thread.currentThread().getName()+" is running now");
            System.out.println("Thread: " + Thread.currentThread().getName() + " is running now");
            connectToPeer(NEIGHBOR_PORT);//not the head
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void connectToController(int server_port) throws IOException {
        try {
            socketToServer = new Socket(SERVER_HOST_NAME, server_port);
            printWriter1 = new PrintWriter(socketToServer.getOutputStream(), true);
            bufferedReader1 = new BufferedReader(new InputStreamReader(socketToServer.getInputStream()));
            System.out.println("Connected to " + SERVER_HOST_NAME + " :: " + server_port);
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + SERVER_HOST_NAME);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for " + "the connection to: " + SERVER_HOST_NAME);
            System.exit(1);
        }

        bufferedReader2 = new BufferedReader(new InputStreamReader(System.in));
        printWriter1.println("@@" + ID + "@@" + THIS_CLIENT_PORT);

        try {
            Thread.sleep(50);
            inComingMSG = bufferedReader1.readLine();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (!inComingMSG.contains("End") && !inComingMSG.isEmpty()) {
            //clock.tickTock();
            printWriter1.println(outGoingMSG);
            inComingMSG = bufferedReader1.readLine();//read the server says.....

            try {
                System.out.println("Incoming MSG: " + inComingMSG);
                if (inComingMSG.contains("@@")) {
                    setData(inComingMSG);
                } else if (inComingMSG.contains("##")) {
                    setNeighborPort(inComingMSG);
                    try {
                        Thread.sleep(50);
                        printWriter1.println("Bye");
                        //break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }
            } catch (Exception e) {
                System.out.println("Socket to Controller Closed.");
                break;
            }
        }//end-while

//        printWriter1.close();
//        printWriter1.flush();
//        bufferedReader1.close();
//        bufferedReader2.close();
        socketToServer.close(); //close socket
    }

    private static void beAServer(int localServerPort) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(localServerPort);
        } catch (IOException e) {
            System.err.println("Problem Setting Up ServerSocket on Port:"+localServerPort+"\n -->"+e.getMessage());
            System.exit(1);
        }

        Socket clientSocket = null;
        System.out.println("Listening on port: " + localServerPort);

        try {
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
        }

        System.out.println("Connection successful");
        System.out.println("Waiting for input.....");

        PrintWriter out2 = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String inputLine;
        boolean done = false;
        while ((inputLine = in2.readLine()) != null) {
            System.out.println("received MSG: " + inputLine);
            //out2.println(inputLine);
            clock[position] += 1;
            if(inputLine.contains("<"));
            String[] array = inputLine.split(" ");
            setNewClock(array[0]);
            setNewIDS(array[1]);
            for(int n : clock){
                System.out.print(n +"-");
            }
            System.out.print("\n");
            if (inputLine.equalsIgnoreCase("Bye"))
                break;
        }

        System.out.println("outside of input-while loop...");

        out2.close();
        in2.close();
        clientSocket.close();
        serverSocket.close();
    }

    private static void connectToPeer(int server_port) throws IOException {

        String serverHostname = new String("127.0.0.1");

        System.out.println("Attempting to connect to host " + serverHostname + " on port " + server_port);

        Socket talkToPeerSocket = null;
        PrintWriter printWriter2 = null;
        BufferedReader bufferedReader2 = null;

        try {
            talkToPeerSocket = new Socket(serverHostname, server_port);
            printWriter2 = new PrintWriter(talkToPeerSocket.getOutputStream(), true);
            bufferedReader2 = new BufferedReader(new InputStreamReader(talkToPeerSocket.getInputStream()));
            System.out.println("talk to peer socket[" + server_port + "] is open. sending message...");
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + serverHostname);
            System.exit(1);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.err.println("Couldn't get I/O for the connection to: " + serverHostname);
            System.exit(1);
        }


        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        clock[position] += 1;
        for(int n : clock){
            System.out.print(n+"-");
        }
        //System.out.print(", from:");
        String userInput = sendClock() + " " + sendIDS();
        printWriter2.println(userInput);
        try {
            Thread.sleep(40);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        printWriter2.println("Thats all i got");

        stdIn.close();
//      talkToPeerSocket.close();
    }

    private static void setNeighborPort(String inComingMSG) {
        StringTokenizer stringTokenizer = new StringTokenizer(inComingMSG, "##");
        NEIGHBOR_PORT = Integer.parseInt(stringTokenizer.nextToken());
        System.out.println("just set NeighborPort: " + NEIGHBOR_PORT);
    }

    private static void setData(String inComingMSG) {
        //incomingMSG: validID @@ validPORT @@ neighPORT @@ isHEAD @@ isTail
        StringTokenizer stringTokenizer = new StringTokenizer(inComingMSG, "@@");
        position = Integer.parseInt(stringTokenizer.nextToken());
        ID = Integer.parseInt(stringTokenizer.nextToken());
        THIS_CLIENT_PORT = Integer.parseInt(stringTokenizer.nextToken());
        NEIGHBOR_PORT = Integer.parseInt(stringTokenizer.nextToken());
        isHead = Boolean.parseBoolean(stringTokenizer.nextToken());
        isTail = Boolean.parseBoolean(stringTokenizer.nextToken());
        NETWORK_SIZE = Integer.parseInt(stringTokenizer.nextToken());

        clock = new int[NETWORK_SIZE+1];
        IDS = new int[NETWORK_SIZE];

        for(int i = 0; i < clock.length; i++){
            if(i==0) clock[i] = ID;
            else if(i == position) clock[i] = 1;
            else clock[i] = 0;
        }

        for(int i = 0; i < IDS.length ; i++){
            if(i == position -1) IDS[i] = ID;
            else IDS[i] = -1;
        }

        System.out.println("      This Client ID: " + ID);
        System.out.println("    This Client Port: " + THIS_CLIENT_PORT);
        System.out.println("       Neighbor Port: " + NEIGHBOR_PORT);
        System.out.println("isHead/isTail Status: " + isHead + "/" + isTail);
    }

    //add method to both client and server
    private static void logging() throws IOException {
        log = Logger.getLogger("LogFile");
        fh = new FileHandler("Computer" + ID + ".txt");
        log.addHandler(fh);
        formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
    }

    public static String sendClock(){
        String myClock = "<";
        for(int i = 0;i<clock.length;i++){
            if(i == clock.length -1) myClock = myClock + clock[i] + ">";
            else myClock = myClock + clock[i] + ",";
        }
        return myClock;
    }

    public static String sendIDS(){
        String myIDS = "[";
        for(int i = 0;i<IDS.length;i++){
            if(i==IDS.length -1) myIDS = myIDS + IDS[i] + "]";
            else myIDS = myIDS + IDS[i] + ",";
        }
        return myIDS;
    }

    public static void setNewClock(String newClock){
        String eatIt;
        int [] holder = new int[2];
        if(newClock.contains("<") && newClock.contains(">")){
            StringTokenizer stringTokenizer = new StringTokenizer(newClock, "<>,");
            for(int i = 0;i<clock.length;i++){
                if(i==0) eatIt = stringTokenizer.nextToken();
                else if (i == position) holder[1] = Integer.parseInt(stringTokenizer.nextToken());
                else clock[i] = Integer.parseInt(stringTokenizer.nextToken());
            }
        }
    }

    public static void setNewIDS(String newIDS){
        String eatIt;
        if(newIDS.contains("[") && newIDS.contains("]")) {
            StringTokenizer stringTokenizer = new StringTokenizer(newIDS, "[],");
            for (int i = 0; i < IDS.length; i++) {
                if (i == position - 1) eatIt = stringTokenizer.nextToken();
                else IDS[i] = Integer.parseInt(stringTokenizer.nextToken());
            }
        }
    }
} //ends Client Class...
