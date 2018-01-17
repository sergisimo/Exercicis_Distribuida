import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class ProcessB {

    private static final int PORT = 5000;
    private static final int PORT_LW = 5002;
    private static final int PORT_SCREEN = 5003;
    private static final String IP = "127.0.0.1";
    private static final String TOKEN_REQUEST = "TOKEN";

    private ServerSocket lwServerSocket;
    private DataOutputStream dataOut;
    private DataInputStream dataIn;

    private DataOutputStream printStream;

    private Socket[] clients;

    private ProcessB () {

        clients = new Socket[2];
    }

    private void connect() {

        try {

            lwServerSocket = new ServerSocket(PORT_LW);

            Socket socket = new Socket(IP, PORT);
            dataOut = new DataOutputStream(socket.getOutputStream());
            dataIn = new DataInputStream(socket.getInputStream());

            socket = new Socket(IP, PORT_SCREEN);
            printStream = new DataOutputStream(socket.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitForClients() {

        Socket socket;
        DataInputStream dataIn;
        ClientBThread clientThread;
        Semaphore outSemaphore = new Semaphore(1, true);
        int id, count = 0;

        try {

            while (count < 2) {
                System.out.println("Waiting clients!");
                socket = lwServerSocket.accept();
                dataIn = new DataInputStream(socket.getInputStream());
                id = dataIn.readInt();
                clients[id] = socket;
                clientThread = new ClientBThread(id, clients, printStream, this, outSemaphore);
                clientThread.start();
                count++;
                System.out.println("Connectat!");
            }

            for (int i = 0; i < 2; i++) {

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

        ProcessB process = new ProcessB();

        process.connect();
        process.waitForClients();
    }
}

class ClientBThread extends Thread {

    private int id;
    private Socket[] clients;
    private DataOutputStream printStream;
    private ProcessB process;
    private Semaphore outSemaphore;

    ClientBThread (int id, Socket[] clients, DataOutputStream printStream, ProcessB process, Semaphore outSemaphore) {

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
                        for (int i = 0; i < 2; i++) {
                            if (i != id) {
                                outStream = new DataOutputStream(clients[i].getOutputStream());
                                outStream.writeInt(id);
                                outStream.writeInt(timeStamp);
                                outStream.writeUTF(tag);
                            }
                        }
                        break;

                    case "request":
                        System.out.println("TOKEN DEMANAT");
                        process.waitForToken();
                        break;

                    case "release":
                        process.releaseToken();
                        break;
                }

                outSemaphore.release();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
