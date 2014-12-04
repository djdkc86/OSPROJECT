
package project.src;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server extends Thread {

    protected static int NETWORK_SIZE = 3;  //change this to adjust size of ring
    protected Socket clientSocket;//enables listening to client systems
    protected static ArrayList<Integer> clientIDs = new ArrayList<>();
    protected static ArrayList<Integer> clientPORTs = new ArrayList<>();
    protected static ArrayList<Socket> clientTHREADS = new ArrayList<>();
    private static Logger log;
    private static FileHandler fh;
    static SimpleFormatter formatter;

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
            if (server_says.equalsIgnoreCase("Bye")) {
                System.exit(0);
            }
        }
        printWriter.close();
        bufferedReader.close();
        clientSocket.close();
        serverSocket.close();
    }

    protected Server(Socket clientSoc) {
        clientSocket = clientSoc;
        start(); //only use this if the class extends thread, not if it implements runnable
        //run();
    }


    @Override
    public void run() {

        count++;
        try {
            PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //add this client socket to the array
            if (clientSocket.isBound()) clientTHREADS.add(clientSocket);
            System.out.println("Connection Socket Created [added thread: " + Thread.currentThread().getName() + "]");

            String client_says = bufferedReader.readLine();
            printWriter.println("processing request...");
            int position = getThreadIndex();

            if (!getData(client_says)) {
                printWriter.println("invalid ");
            } else {
                //initial data transfer: validID @@ validPORT @@ neighPORT @@ isHEAD @@ isTail
                System.out.println("\nclient IDs: " + clientIDs);
                System.out.println("client Ports: " + clientPORTs);
                boolean isHead = (clientIDs.size() < 2 && position == 0);
                boolean isTail = (clientIDs.size() == NETWORK_SIZE);

                if(isTail){
                    printWriter.println("@@" + position + "@@" + clientIDs.get(position) +
                            "@@" + clientPORTs.get(position) +
                            "@@" + (clientPORTs.get(position) + (NETWORK_SIZE-1)) +
                            "@@" + isHead + "@@" + isTail + "@@" +  NETWORK_SIZE);
                    broadcast(-1, "End");//+ clientPORTs.get(NETWORK_SIZE-1).toString());//send port address of last to the first

                }

                if (!isHead) {//to non-head processes
                    printWriter.println("@@" + position + "@@" + clientIDs.get(position) +
                            "@@" + clientPORTs.get(position) +
                            "@@" + clientPORTs.get(position - 1) +
                            "@@" + isHead + "@@" + isTail + "@@" + NETWORK_SIZE);

                } else if (isHead) {//to head
                    printWriter.println("@@" + position + "@@" + clientIDs.get(position) +
                            "@@" + clientPORTs.get(position) +
                            "@@" + (clientPORTs.get(position) + (NETWORK_SIZE-1)) +
                            "@@" + isHead + "@@" + isTail + "@@" +  NETWORK_SIZE);

                }

                System.out.println("   -->just sent data to client " + position);
                if (count == NETWORK_SIZE)
                    broadcast(0, "##" + clientPORTs.get(NETWORK_SIZE-1).toString());//send port address of last to the first
//              printWriter1.println("End");

            }


            while ((client_says = bufferedReader.readLine()) != null) {
//              if(!client_says.isEmpty()){
//                    System.out.println("received MSG: " + client_says);}
//                    printWriter.println(client_says);
                if (client_says.trim().equalsIgnoreCase("Bye")) {
                      broadcast(-1, "End");//send port address of
//                    printWriter.close();
//                    printWriter.flush();
//                    bufferedReader.close();
                      System.out.println("RightNow: "+Calendar.getInstance().getTimeInMillis());                      System.out.println(Calendar.getInstance().getTimeInMillis());
//                    clientSocket.close();
                }
            }

//            printWriter1.close();
//            printWriter1.flush();
//            bufferedReader.close();
//            clientSocket.close();

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
        Integer temp2 = getValidPort();
        clientPORTs.add(temp2);
        okayPORT = true;

        if (validID(temp)) {
            clientIDs.add(temp);
            okayID = true;
        } else {
            clientIDs.add(getValidID(temp));
            okayID = true;
        }
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

    private int getValidID(int inValidID) {
        boolean legitID = false;
        int validID = inValidID;

        while (!legitID) {
            validID += 1;
            legitID = validID(validID);
        }

        return validID;
    }

    private int getValidPort() {
        return 20010 + getThreadIndex();
    }

    private int getThreadIndex() {
        CharSequence name = Thread.currentThread().getName();
        String index_str = "";

        for (int i = 0; i < name.length(); i++) {
            Character temp = name.charAt(i);
            if (Character.isDigit(temp)) {
                index_str = index_str + temp;
            }
        }

        return Integer.valueOf(index_str);
    }

    public void broadcast(int position, String MSG) {
        System.out.println("  ...within broadcast trying to send MSG: "+ MSG);

        if (position == -1) {
            for (Socket socket : clientTHREADS) {
                try {
                    PrintWriter printWriter3 = new PrintWriter(socket.getOutputStream(), true);
                    printWriter3.println(MSG);
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                } catch (IOException e) {
                    System.err.println("broadcast ["+MSG+"] failed");
                    e.printStackTrace();
                }
            }

        } else {
            try {
                PrintWriter printWriter3 = new PrintWriter(clientTHREADS.get(position).getOutputStream(), true);
                printWriter3.println(MSG);
            } catch (IOException e) {
                System.err.println("broadcast ["+MSG+"] failed");
                e.printStackTrace();
            }
        }
    }

    //add method to both client and server
    private static void logging() throws IOException {
        log = Logger.getLogger("LogFile");
        fh = new FileHandler("Controller.txt");
        log.addHandler(fh);
        formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
    }


}
