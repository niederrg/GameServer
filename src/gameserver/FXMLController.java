
package gameserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import simulation.Simulation;

public class FXMLController implements Initializable {

    @FXML
    private TextArea textArea;
   
    private int clientNo = 0;
    Simulation sim;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    new Thread( () -> {
      try {
        // Create a server socket
        ServerSocket serverSocket = new ServerSocket(8000);
        
        while (true) {
          // Listen for a new connection request
          Socket socket = serverSocket.accept();
    
          // Increment clientNo
          clientNo++;
          
          Platform.runLater( () -> {
            // Display the client number
            textArea.appendText("Starting thread for client " + clientNo +
              " at " + new Date() + '\n');
            });
          
          // Create and start a new thread for the connection
          new Thread(new HandleAClient(socket,textArea,clientNo,sim)).start();
        }
      }
      catch(IOException ex) {
        System.err.println(ex);
      }
    }).start();
    }    
}


class HandleAClient implements Runnable, game.GameConstants {
    private Socket socket;
    private TextArea textArea;
    private Player player;
    private Player player2;
    private Simulation sim;

    public HandleAClient(Socket socket,TextArea textArea,int clientNo, Simulation sim) {
      this.socket = socket;
      this.textArea = textArea;
      this.sim = sim;
      if (clientNo==1){
          player = new Player(120, 120);
          player2 = new Player(60,60);
      }
      else{
          player = new Player(60,60);
          player2 = new Player(120,120);
      }    
    }
    
    
    public void run() {
      try {
        // Create reading and writing streams
        BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter outputToClient = new PrintWriter(socket.getOutputStream());

        // Continuously serve the client
        while (true) {
          // Receive request code from the client
          int request = Integer.parseInt(inputFromClient.readLine());
          // Process request
          switch(request) {
              case SEND_DXDY: {
                  int dX = Integer.parseInt(inputFromClient.readLine());
                  int dY = Integer.parseInt(inputFromClient.readLine());
                  player.move(dX, dY);
                  break;
              }
              case GET_X: {
                  String opponent = inputFromClient.readLine();
                  if (opponent.equalsIgnoreCase("false")){
                    outputToClient.println(player.getX());
                  } else{
                    outputToClient.println(player2.getX());
                  }
                  outputToClient.flush();
                  break;
              }
              case GET_Y: {
                  outputToClient.println(player.getY());
                  outputToClient.flush();
                  break;
              }
              case EVOLVE: {
                  sim.evolve(Integer.parseInt(inputFromClient.readLine()));
              }
//              case GET_COMMENT: {
//                  int n = Integer.parseInt(inputFromClient.readLine());
//                  outputToClient.println(room.getTranscriptComment(n));
//                  outputToClient.flush();
//                  System.out.print(room.getTranscriptComment(n));
//                  break;
//              }
//              case SEND_ROOM: {
//                  String roomString = inputFromClient.readLine();
//                  if(getRoomFromString(roomString)!=null){
//                    this.room = getRoomFromString(roomString);
//                    transcript = room.getTranscript();
//                    room.clientEntered();
//                  }
//                  break;
//              }
//              case GET_ROOM_LIST: {
//                  outputToClient.println(FXMLDocumentController.roomList.size());
//                  ListIterator<Room> itor = FXMLDocumentController.roomList.listIterator();
//                  while (itor.hasNext()){
//                    outputToClient.println(itor.next().getName());
//                  } 
//                  outputToClient.flush();
//                  break;
//              }
          }
        }
      }
      catch(IOException ex) {
          Platform.runLater(()->textArea.appendText("Exception in client thread: "+ex.toString()+"\n"));
      }
    }
  }