import java.io.*;
import java.net.*;
import java.io.BufferedReader;
import java.io.FileReader;

//Range: TUX050 TO TUX065
class UDPServer1 {
    public static void main(String args[]) throws Exception {
        InetAddress IPAddressServer = InetAddress.getByName("tux061");    //setting ip for the server

        // Assigns the same port number as the server
        int serverPort = 10009;
        DatagramSocket serverSocket = new DatagramSocket(serverPort, IPAddressServer);

        // This is the size of the data received and sent
        byte[] receiveData = new byte[256];
        byte[] sendData = new byte[256];
        // If the file is found
        boolean ifFileFound = false;

        // This is the 1st response message
        DatagramPacket initialReceivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(initialReceivePacket);
        InetAddress IPAddressClient = initialReceivePacket.getAddress();  //setting ip for the client
        String sentence = new String(initialReceivePacket.getData());
        System.out.println("FROM CLIENT: " + sentence);
        // Finds the file, if it exists
        String filename = sentence.substring(sentence.indexOf("GET ") + 4, sentence.indexOf(".html"));
        FileReader file = new FileReader(filename + ".txt");
        if (file != null) { // Returns true if the .txt file is found
            ifFileFound = true;
        }

        // Setting up for segmentation
        BufferedReader reader = new BufferedReader(file);
        String stringReader = new String();
        String stringReader2;
        String outputString = new String();
        String checkSum = "00000";
        int count = 0;



        // Header skeleton string
        outputString = count + ", " + checkSum + "\r\n ";
        byte[] header = outputString.getBytes();

        byte[] paddedData = new byte[256]; // This will be used for a padded packet so it'll all be the same size
        for (int i = 0; i < paddedData.length; i++) {
            if (i < header.length) {
                paddedData[i] = header[i];
            }
            else {
                paddedData[i] = 32;
            }
        }


        // Puts all the data in the .txt file into one string variable
        String append;
        do {
            append = reader.readLine();
            if (append != null) {
                stringReader = stringReader + append + " ";
            }
        } while (append != null);

        // Gets the entire byte value of the file as a string
        stringReader2 = new String(stringReader.getBytes());

        // Header for the 1st response
        String firstResponseHeader = "HTTP/1.0 200 Document Follows\r\n Content-Type: text/plain\r\n Content-Length: " + stringReader2.length() +"\r\n \r\n " + filename + ".http";
        sendData = firstResponseHeader.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(sendData, sendData.length, IPAddressClient, 10008);
        serverSocket.send(responsePacket);
        System.out.println("TO CLIENT: " + firstResponseHeader);
        // Segments the data with byte size 256 (max)

        int z = 7;
        int spaceLeft = 256;
        while (true & ifFileFound) {
            for (int i = 0; i < stringReader2.length(); i++) {
                // Appends to the outputString
                while (paddedData[z] != 32 && z < 256) {
                    z++;
                }
                spaceLeft = spaceLeft - z;
                paddedData[z] = (byte)stringReader2.charAt(i);
                z++;

                // Goes here if the outputString byte size has reached 256 or it has read the last bit of the .txt file
                if (z == 256 || i == stringReader2.length() - 1) {
                    // Replace the -1 placeholder in the output string with the real checksum

                    String newString = new String(paddedData);
                    int checkSumInt = checksum(newString);
                    String checkSumString = Integer.toString(checkSumInt);
                    outputString = changePaddedData(paddedData, checkSumString);
                    outputString = outputString.replaceFirst("0", count+"");
                    System.out.println("TO CLIENT PACKET: " + count);
                    System.out.println(outputString + "\n");

                    // Assingning outputString to sendData as bytes
                    sendData = outputString.getBytes();

                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddressClient, 10008);
                    serverSocket.send(sendPacket);

                    count++;

                    z = 7;
                    spaceLeft = 256;

                    // This re-pads the data
                    paddedData = new byte[256]; // This will be used for a padded packet so it'll all be the same size
                    for (int j = 0; j < paddedData.length; j++) {
                        if (j < header.length) {
                            paddedData[j] = header[j];
                        }
                        else {
                            paddedData[j] = 32;
                        }
                    }

                    // If the server is done sending packets
                    if (i == stringReader2.length() - 1) {
                        // Sends the null packet to identify that the Server is done sending packets
                        sendData = "<><>".getBytes();
                        sendPacket = new DatagramPacket(sendData, sendData.length, IPAddressClient, 10008);
                        serverSocket.send(sendPacket);

                        serverSocket.close();
                        reader.close();
                        System.exit(0);
                    }
                }
            }
        }
    }


    //This ensured the segmented data being transfered from the client to server isn't corrupted
    public static int checksum(String dataString) {
      byte[] dataArray = dataString.getBytes();
      int sum = 0;
      for (int i = dataString.indexOf("\r\n") + 1; i < dataArray.length; i++) {
        sum += (int)dataArray[i];
      }
      return sum;
    }

    public static String changePaddedData(byte[] paddedData, String checkSum2) {
        String output = new String(paddedData);
        int i = output.indexOf(" ") + 1;
        byte[] looker = checkSum2.getBytes();
        if (checkSum2.length() == 2) {
            paddedData[i + 3] = looker[0];
            paddedData[i + 4] = looker[1];
        }
        else if (checkSum2.length() == 3) {
            paddedData[i + 2] = looker[0];
            paddedData[i + 3] = looker[1];
            paddedData[i + 4] = looker[2];
        }
        else if (checkSum2.length() == 4) {
            paddedData[i + 1] = looker[0];
            paddedData[i + 2] = looker[1];
            paddedData[i + 3] = looker[2];
            paddedData[i + 4] = looker[3];
        }
        else if (checkSum2.length() == 5) {
            paddedData[i] = looker[0];
            paddedData[i + 1] = looker[1];
            paddedData[i + 2] = looker[2];
            paddedData[i + 3] = looker[3];
            paddedData[i + 4] = looker[4];
        }
        output = new String(paddedData);
        return output;
        }
    }

