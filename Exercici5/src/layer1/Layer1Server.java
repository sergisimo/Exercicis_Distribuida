package layer1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Layer1Server {

    private static final String HOST = "localhost";
    private static final int CORE_LAYER_PORT = 3000;
    private static final int LAYER_2_PORT = 4000;
    private static final int CLIENT_PORT = 5100;

    private int myId;
    private ServerSocket clientSocket;
    private ServerSocket nextLayerSocket;
    private int[] data;
    private Socket previousLayerServer;
    private Socket[] nextLayerServer;
    PrintWriter fileWriter;
    int totalCounter;

    private Layer1Server(int myId) {

        this.myId = myId;
        ClientThread clientThread;
        TimeThread timeThread;
        data = new int[50];
        totalCounter = 0;
        nextLayerServer = new Socket[2];

        try {
            fileWriter = new PrintWriter("src/layer1/log_layer1_" + myId + ".txt", "UTF-8");
            clientSocket = new ServerSocket((CLIENT_PORT + myId));
            previousLayerServer = new Socket(HOST, (CORE_LAYER_PORT + myId));
            if (myId == 2) nextLayerSocket = new ServerSocket(LAYER_2_PORT + myId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        clientThread = new ClientThread(clientSocket, this);
        clientThread.start();
    }

    private void waitForSyncs() {

        String updateAux;
        String[] update;
        DataInputStream dataIn;

        while (true) {
            try {

                dataIn = new DataInputStream(previousLayerServer.getInputStream());

                updateAux = dataIn.readUTF();

                update = updateAux.split("-");
                for (int i = 0; i < data.length; i++) data[i] = Integer.parseInt(update[i]);

                fileWriter.println("UPDATE Nº" + totalCounter + " --> " + dataToString());
                fileWriter.flush();
                totalCounter++;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitForNextLayer() {

        TimeThread timeThread;

        System.out.println("WAITING NEXT LAYER...");

        try {
            nextLayerServer[0] = nextLayerSocket.accept();
            nextLayerServer[1] = nextLayerSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (myId == 2) {
            timeThread = new TimeThread(this);
            timeThread.start();
        }

        System.out.println("NEXT LAYER CONNECTED...");
    }

    void sendUpdateToNextLayer() {

        DataOutputStream dataOut;

        try {

            for (int i = 0; i < nextLayerServer.length; i++) {
                dataOut = new DataOutputStream(nextLayerServer[i].getOutputStream());
                dataOut.writeUTF(dataToString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String dataToString() {

        String returnValue = "";

        for (int i = 0; i < data.length; i++) {
            if (i == 0) returnValue += data[i];
            else returnValue += "-" + data[i];
        }

        return returnValue;
    }

    int[] getData() {

        return data;
    }

    public static void main (String[] args) {

        Layer1Server server = new Layer1Server(Integer.parseInt(args[0]));

        if (server.myId == 2) server.waitForNextLayer();

        server.waitForSyncs();
    }
}

class ClientThread extends Thread {

    private ServerSocket socket;
    private Layer1Server server;

    ClientThread (ServerSocket socket, Layer1Server server) {

        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {

        Socket client;
        DataInputStream inStream;
        DataOutputStream outputStream;
        String numbersAux, response;
        String[] numbers;
        boolean firstRead;
        int[] data;

        while (true) {
            try {

                client = socket.accept();
                inStream = new DataInputStream(client.getInputStream());
                outputStream = new DataOutputStream(client.getOutputStream());

                numbersAux = inStream.readUTF();

                numbers = numbersAux.split("-");
                response = "";
                firstRead = true;

                for (int i = 0; i < numbers.length; i++) {

                        if (!firstRead) response += "-";
                        else firstRead = false;

                        response += numbers[i] + ";" + server.getData()[Integer.parseInt(numbers[i])];
                }

                outputStream.writeUTF(response);
                client.close();


            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class TimeThread extends Thread {

    private Layer1Server server;

    TimeThread (Layer1Server server) {

        this.server = server;
    }

    @Override
    public void run() {

        while (true) {

            try {
                Thread.sleep(10000);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("UPDATING NEXT LAYER...");
            server.sendUpdateToNextLayer();
        }
    }
}