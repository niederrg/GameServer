
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
import simulation.Diamond;
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
    private Diamond player;
    private Diamond player2;
    private Simulation sim;

    public HandleAClient(Socket socket,TextArea textArea,int clientNo, Simulation sim) {
      this.socket = socket;
      this.textArea = textArea;
      this.sim = sim;
      if (clientNo==1){
          player = new Diamond(120, 120,10);
          player2 = new Diamond(60,60,10);
      }
      else{
          player = new Diamond(60,60,10);
          player2 = new Diamond(120,120,10);
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
                    outputToClient.println(player.x);
                  } else{
                    outputToClient.println(player2.x);
                  }
                  outputToClient.flush();
                  break;
              }
              case GET_Y: {
                  String opponent = inputFromClient.readLine();
                  if (opponent.equalsIgnoreCase("false")){
                      outputToClient.println(player.y);
                  }else{
                    outputToClient.println(player2.y);
                  }
                  outputToClient.flush();
                  break;
              }
              case EVOLVE: {
                  sim.evolve(Integer.parseInt(inputFromClient.readLine()));
              }
              case GET_POINTS: {
                  String opponent = inputFromClient.readLine();
                  if (opponent.equalsIgnoreCase("false")){
                      for(int i=0;i<4;i++){
                          outputToClient.println(player.getWallEndX(i));
                          outputToClient.println(player.getWallEndY(i));
                      }
                  }else{
                      for(int i=0;i<4;i++){
                          outputToClient.println(player2.getWallEndX(i));
                          outputToClient.println(player2.getWallEndY(i));
                      }
                  }
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