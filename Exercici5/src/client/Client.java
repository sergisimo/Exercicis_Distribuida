package client;

import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class Client {

    private static final int LAYER_1_PORT = 5000;
    private static final int LAYER_2_PORT = 5100;
    private static final int LAYER_3_PORT = 5200;

    private static final String HOST = "localhost";

    private Scanner fileReader;

    private Client (String fileName) {

        try {
            fileReader = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendTransactions() {

        String[] operations;
        String aux, transaction, numbers, orders;
        int layer;

        while (fileReader.hasNextLine()) {

            transaction = fileReader.nextLine();

            operations = transaction.split(",");

            if (operations[0].contains("<")) {
                aux = operations[0].substring(operations[0].indexOf('<') + 1);
                aux = aux.substring(0, aux.indexOf('>'));
                layer = Integer.parseInt(aux);
            } else layer = 0;

            numbers = "";
            for (int i = 1; i < operations.length - 1; i++) {
                aux = operations[i].substring(operations[i].indexOf('(') + 1);
                aux = aux.substring(0, aux.indexOf(')'));
                if (i == 1) numbers += aux;
                else numbers += "-" + aux;
            }

            orders = "";
            if (layer == 0) {

                for (int i = 1; i < operations.length - 1; i++) {
                    aux = operations[i].substring(0, operations[i].indexOf('('));
                    if (i == 1) orders += aux;
                    else orders += "-" + aux;
                }
            }

            sendTransaction(layer, numbers, orders);
        }

        fileReader.close();
    }

    private void sendTransaction(int layer, String numbers, String orders) {

        Socket socket;
        DataOutputStream outStream;
        DataInputStream inputStream;
        Random rand = new Random();
        String response;
        int port = 0, random;

        try {
            switch (layer) {

                case 0:
                    random = (rand.nextInt() % 3);
                    if (random < 0) random = random * (-1);
                    port = LAYER_1_PORT + random;
                    break;
                case 1:
                    random = (rand.nextInt() % 2);
                    if (random < 0) random = random * (-1);
                    random++;
                    port = LAYER_2_PORT + random;
                    break;
                case 2:
                    random = (rand.nextInt() % 2);
                    if (random < 0) random = random * (-1);
                    port = LAYER_3_PORT + random;
                    break;
            }

            socket = new Socket(HOST, port);
            outStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());

            outStream.writeUTF(numbers);
            if (layer == 0) outStream.writeUTF(orders);

            response = inputStream.readUTF();

            socket.close();
            System.out.println("FROM PORT: " + port + " --> " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main (String[] args) {

        Client client = new Client(args[0]);

        client.sendTransactions();
    }
}
