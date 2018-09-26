import java.io.*;
import java.net.*;
import java.lang.Math;
import java.util.Random;
import java.util.Scanner;

//Range: TUX050 TO TUX065
class UDPClient1 {
  public static void main(String args[]) throws Exception {
    Scanner scan = new Scanner(System.in);
    double prob = Double.valueOf(scan.nextLine());
    //    //setting ip
    InetAddress IPAddress = InetAddress.getByName("tux060");
    InetAddress IPAddressServer = InetAddress.getByName("tux061");

    //creating socket
    DatagramSocket clientSocket = new DatagramSocket(10008, IPAddress);

    //allocating memory for request
    byte[] request = new byte[256];

    //assigning request
    String requestString = "GET TestFile.html HTTP/1.0";
    request = requestString.getBytes();
    DatagramPacket HTTPRequestPacket = new DatagramPacket(request, request.length, IPAddressServer, 10009);
    clientSocket.send(HTTPRequestPacket);
    System.out.println("TO SERVER: " + requestString);
    FileWriter writer = new FileWriter("output.txt");
    BufferedWriter buffer = new BufferedWriter(writer);
    String outputString = "";
    byte[] HTTPResponse = new byte[256];
    DatagramPacket HTTPResponsePacket = new DatagramPacket(HTTPResponse, HTTPResponse.length);
    clientSocket.receive(HTTPResponsePacket);
    while(true) {
    byte[] receive = new byte[256];
    DatagramPacket receivePacket = new DatagramPacket(receive, receive.length);
    clientSocket.receive(receivePacket);
    String nullCheckString = new String(receivePacket.getData());
    if (nullCheckString.contains("><")) {
      System.out.println("End of File");
      clientSocket.close();
      buffer.close();
      System.exit(0);
    }
    gremlin(receivePacket, 0.5);
    String modifiedSentence = new String(receivePacket.getData());
    int space = modifiedSentence.indexOf(" ");
    int comma = modifiedSentence.indexOf(",");
    String dataString = modifiedSentence.substring(space + 6);
    int mySum = checksum(dataString);
    int expectedSum = -1;
    int sequence = -1;
    try {
      expectedSum = Integer.parseInt(modifiedSentence.substring(comma + 2, modifiedSentence.indexOf("\r")));
      sequence = Integer.parseInt(modifiedSentence.substring(0, comma));
    }
    catch (NumberFormatException n){
      System.out.println("Packet Corrupted Beyond Readablilty");
      expectedSum = -1;
      sequence = -1;
    }
    catch (StringIndexOutOfBoundsException s) {
      System.out.println("Packet Corrupted Beyond Readablilty");
      expectedSum = -1;
      sequence = -1;
    }
    if (mySum != expectedSum) {
      System.out.println("ERROR: Unexpected Checksum at Packet " + sequence);
      System.out.println("Expected: " + expectedSum);
      System.out.println("Got: " + mySum);
    }
    else {
      System.out.println("Packet " + sequence + " received OK");
    }
    System.out.println("FROM SERVER: " + modifiedSentence + "\n");
    buffer.write(modifiedSentence);
    }
  }
  public static void gremlin(DatagramPacket myPacket, double prob) {
    double grem = Math.random();
    if (grem >= prob) {
      return;
    }
    grem = Math.random();
    if (grem <= 0.2) {
      //3 bytes
      damage(myPacket, 3);
      return;
    }
    if (grem <= 0.5) {
      //2 bytes
      damage(myPacket, 2);
      return;
    }
    //1 byte
    damage(myPacket, 1);
    return;
  }
  public static void damage(DatagramPacket myPacket, int numBytes) {
    byte[] randBytes = new byte[numBytes];
    Random rand = new Random();
    rand.nextBytes(randBytes);
    byte[] packetData = myPacket.getData();
    for (byte b : randBytes) {
      packetData[rand.nextInt(packetData.length)] = b;
    }
    //put data back in
    myPacket.setData(packetData, 0, packetData.length);
  }
  public static int checksum(String dataString) {
    byte[] dataArray = dataString.getBytes();
    int sum = 0;
    for (int i = dataString.indexOf("\r\n") + 1; i < dataArray.length; i++) {
      sum += (int)dataArray[i];
    }
    return sum;
  }
}
