package coreLayer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class CoreLayerServer {

    private static final String HOST = "localhost";
    private static final int LAYER_PORT = 2000;
    private static final int LAYER_1_PORT = 3000;
    private static final int CLIENT_PORT = 5000;

    int myId;
    private ServerSocket clientSocket;
    private ServerSocket layerSocket;
    private ServerSocket nextLayerSocket;
    private int[] data;
    private Semaphore dataSemaphore = new Semaphore(1, true);
    private Socket nextLayerServer;
    PrintWriter fileWriter;
    int updateCounter;
    int totalCounter;

    private CoreLayerServer(int myId) {

        this.myId = myId;
        ClientThread clientThread;
        data = new int[50];
        updateCounter = totalCounter = 0;

        try {
            fileWriter = new PrintWriter("src/coreLayer/log_corelayer_" + myId + ".txt", "UTF-8");
            clientSocket = new ServerSocket((CLIENT_PORT + myId));
            layerSocket = new ServerSocket((LAYER_PORT + myId));
            if (myId != 0) nextLayerSocket = new ServerSocket(LAYER_1_PORT + myId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        clientThread = new ClientThread(clientSocket, this, dataSemaphore);
        clientThread.start();
    }

    private void waitForSyncs() {

        Socket server;
        String numbersAux, updatesAux;
        String[] numbers, updates;
        DataInputStream dataIn;
        DataOutputStream dataOut;

        while (true) {
            try {
                server = layerSocket.accept();

                dataIn = new DataInputStream(server.getInputStream());
                dataOut = new DataOutputStream(server.getOutputStream());

                numbersAux = dataIn.readUTF();
                updatesAux = dataIn.readUTF();

                dataOut.writeUTF("OK");
                dataSemaphore.acquire();

                numbers = numbersAux.split("-");
                updates = updatesAux.split("-");

                for (int i = 0; i < numbers.length; i++) {
                    data[Integer.parseInt(numbers[i])] = Integer.parseInt(updates[i]);
                    updateCounter++;
                    if (updateCounter == 10) {
                        System.out.println("UPDATE LAYER 2");
                        if (myId != 0) sendUpdateToNextLayer();
                        updateCounter = 0;
                    }
                }
                fileWriter.println("UPDATE Nº" + totalCounter + " --> " + dataToString());
                fileWriter.flush();
                totalCounter++;
                dataSemaphore.release();

                server.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitForNextLayer() {

        System.out.println("WAITING NEXT LAYER...");

        try {
            nextLayerServer = nextLayerSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("NEXT LAYER CONNECTED...");
    }

    void broadcastUpdate(String updatePositions, String updateNumbers) {

        DataOutputStream dataOut;
        DataInputStream dataIn;
        Socket socket;

        for (int i = 0; i < 3; i++) {

            if (i != myId) {
                try {

                    socket = new Socket(HOST, (LAYER_PORT + i));
                    dataOut = new DataOutputStream(socket.getOutputStream());
                    dataIn = new DataInputStream(socket.getInputStream());

                    dataOut.writeUTF(updatePositions);
                    dataOut.writeUTF(updateNumbers);

                    dataIn.readUTF();

                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void sendUpdateToNextLayer() {

        DataOutputStream dataOut;

        try {

            dataOut = new DataOutputStream(nextLayerServer.getOutputStream());
            dataOut.writeUTF(dataToString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String dataToString() {

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

    private int getMyId() {

        return myId;
    }

    public static void main (String[] args) {

        CoreLayerServer server = new CoreLayerServer(Integer.parseInt(args[0]));

        if (server.getMyId() != 0) server.waitForNextLayer();

        server.waitForSyncs();
    }
}

class ClientThread extends Thread {

    private ServerSocket socket;
    private CoreLayerServer server;
    private Semaphore dataSemaphore;

    ClientThread (ServerSocket socket, CoreLayerServer server, Semaphore dataSemaphore) {

        this.socket = socket;
        this.server = server;
        this.dataSemaphore = dataSemaphore;
    }

    @Override
    public void run() {

        Socket client;
        DataInputStream inStream;
        DataOutputStream outputStream;
        String numbersAux, ordersAux, response, updatePositions, updateNumbers;
        String[] numbers, orders, updateNums;
        boolean firstRead, firstWrite;
        int[] data;

        while (true) {
            try {

                client = socket.accept();

                inStream = new DataInputStream(client.getInputStream());
                outputStream = new DataOutputStream(client.getOutputStream());

                numbersAux = inStream.readUTF();
                ordersAux = inStream.readUTF();
                dataSemaphore.acquire();

                numbers = numbersAux.split("-");
                orders = ordersAux.split("-");
                response = "";
                updatePositions = "";
                updateNumbers = "";
                firstRead = firstWrite = true;

                for (int i = 0; i < orders.length; i++) {

                    if (orders[i].equals("r")) {
                        if (!firstRead) response += "-";
                        else firstRead = false;

                        response += numbers[i] + ";" + server.getData()[Integer.parseInt(numbers[i])];
                    } else {
                        updateNums = numbers[i].split(";");
                        data = server.getData();
                        data[Integer.parseInt(updateNums[0])] = Integer.parseInt(updateNums[1]);
                        server.updateCounter++;
                        if (server.updateCounter == 10) {
                            System.out.println("UPDATE LAYER 2");
                            if (server.myId != 0) server.sendUpdateToNextLayer();
                            server.updateCounter = 0;
                        }
                        if (!firstWrite) {
                            updatePositions += "-";
                            updateNumbers += "-";
                        }
                        else firstWrite = false;

                        updatePositions += updateNums[0];
                        updateNumbers += updateNums[1];
                    }
                }

                outputStream.writeUTF(response);
                client.close();

                if (updatePositions != "") {
                    server.broadcastUpdate(updatePositions, updateNumbers);
                    server.fileWriter.println("UPDATE Nº" + server.totalCounter + " --> " + server.dataToString());
                    server.fileWriter.flush();
                    server.totalCounter++;
                }
                dataSemaphore.release();
            }catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
