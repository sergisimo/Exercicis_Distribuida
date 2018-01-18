package layer2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Layer2Server {

    private static final String HOST = "localhost";
    private static final int LAYER_1_PORT = 4002;
    private static final int CLIENT_PORT = 5200;

    private int myId;
    private ServerSocket clientSocket;
    private int[] data;
    private Socket previousLayerServer;
    PrintWriter fileWriter;
    int totalCounter;

    private Layer2Server(int myId) {

        this.myId = myId;
        ClientThread clientThread;
        data = new int[50];
        totalCounter = 0;

        try {
            fileWriter = new PrintWriter("src/layer2/log_layer2_" + myId + ".txt", "UTF-8");
            clientSocket = new ServerSocket((CLIENT_PORT + myId));
            previousLayerServer = new Socket(HOST, LAYER_1_PORT);
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

        Layer2Server server = new Layer2Server(Integer.parseInt(args[0]));

        server.waitForSyncs();
    }
}

class ClientThread extends Thread {

    private ServerSocket socket;
    private Layer2Server server;

    ClientThread (ServerSocket socket, Layer2Server server) {

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

