import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class ProcessA {

    private static final int PORT_HW = 5000;
    private static final int PORT_LW = 5001;
    private static final int PORT_SCREEN = 5003;
    private static final int TOKEN_PORT = 5004;
    private static final String IP = "127.0.0.1";
    private static final String TOKEN_REQUEST = "TOKEN";

    private ServerSocket serverSocket;
    private ServerSocket lwServerSocket;
    private ServerSocket tokenSocket;
    private DataOutputStream dataOut;
    private DataInputStream dataIn;
    private DataOutputStream printStream;
    private Socket[] clients;

    private ProcessA () {

        clients = new Socket[3];
        TokenThread thread;

        try {
            serverSocket = new ServerSocket(PORT_HW);
            lwServerSocket = new ServerSocket(PORT_LW);
            tokenSocket = new ServerSocket(TOKEN_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        thread = new TokenThread(tokenSocket, this);
        thread.start();
    }

    private void waitForConnection() {

        Socket socket;

        try {

            socket = serverSocket.accept();
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());

            socket = new Socket(IP, PORT_SCREEN);
            printStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitForClients() {

        Socket socket;
        DataInputStream dataIn;
        ClientThread clientThread;
        Semaphore outSemaphore = new Semaphore(1, true);
        int id, count = 0;

        try {

            while (count < 3) {
                System.out.println("Waiting clients!");
                socket = lwServerSocket.accept();
                dataIn = new DataInputStream(socket.getInputStream());
                id = dataIn.readInt();
                clients[id] = socket;
                clientThread = new ClientThread(id, clients, printStream, this, outSemaphore);
                clientThread.start();
                count++;
                System.out.println("Connectat!");
            }

            for (int i = 0; i < 3; i++) {

                DataOutputStream out = new DataOutputStream(clients[i].getOutputStream());
                out.writeUTF("start");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean waitForToken() {

        String response;

        try {

            do {
                response = dataIn.readUTF();
            } while (!response.equals(TOKEN_REQUEST));
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public boolean releaseToken() {

        try {

            dataOut.writeUTF(TOKEN_REQUEST);
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public static void main (String[] args) {

        ProcessA process = new ProcessA();

        process.waitForConnection();
        process.waitForClients();
    }
}

class ClientThread extends Thread {

    private int id;
    private Socket[] clients;
    private DataOutputStream printStream;
    private ProcessA process;
    private Semaphore outSemaphore;

    ClientThread (int id, Socket[] clients, DataOutputStream printStream, ProcessA process, Semaphore outSemaphore) {

        this.id = id;
        this.clients = clients;
        this.printStream = printStream;
        this.process = process;
        this.outSemaphore = outSemaphore;
    }

    @Override
    public void run() {

        DataInputStream dataIn;
        DataOutputStream outStream;
        String request;
        int destination, timeStamp;
        String tag;

        try {

            dataIn = new DataInputStream(clients[id].getInputStream());

            while (true) {

                request = dataIn.readUTF();
                System.out.println(request);

                outSemaphore.acquire();
                switch (request) {

                    case "print":
                        printStream.writeUTF(dataIn.readUTF());
                        break;

                    case "message":
                        destination = dataIn.readInt();
                        timeStamp = dataIn.readInt();
                        tag = dataIn.readUTF();
                        outStream = new DataOutputStream(clients[destination].getOutputStream());
                        outStream.writeInt(id);
                        outStream.writeInt(timeStamp);
                        outStream.writeUTF(tag);
                        break;

                    case "broadcast":
                        timeStamp = dataIn.readInt();
                        tag = dataIn.readUTF();
                        for (int i = 0; i < clients.length; i++) {
                            if (i != id) {
                                outStream = new DataOutputStream(clients[i].getOutputStream());
                                outStream.writeInt(id);
                                outStream.writeInt(timeStamp);
                                outStream.writeUTF(tag);
                            }
                        }
                        break;
                }

                outSemaphore.release();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class TokenThread extends Thread {

    private ServerSocket socket;
    private ProcessA process;

    TokenThread (ServerSocket socket, ProcessA process) {

        this.socket = socket;
        this.process = process;
    }

    @Override
    public void run() {

        Socket client;
        DataOutputStream outStream;
        DataInputStream inStream;
        String request;

        while (true) {
            try {

                client = socket.accept();

                inStream = new DataInputStream(client.getInputStream());
                outStream = new DataOutputStream(client.getOutputStream());

                request = inStream.readUTF();

                if (request.equals("release")) process.releaseToken();
                else if (request.equals("request")) {
                    process.waitForToken();
                    outStream.writeUTF("OK");
                }

                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}