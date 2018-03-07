
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
   
    private static int clientNo = 0;
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
          
          Simulation sim = new Simulation(300,250,2,2);
          // Create and start a new thread for the connection
          new Thread(new HandleAClient(socket,textArea,clientNo,sim)).start();
        }
      }
      catch(IOException ex) {
        System.err.println(ex);
      }
    }).start();
    }    
    
    public static void clientLeft() {
        clientNo--;
    }
}


class HandleAClient implements Runnable, game.GameConstants {
    private Socket socket;
    private TextArea textArea;
    private Diamond player;
    private Diamond player2;
    private Simulation sim;
    private int clientNum;
    private static int p1ready;
    private static int p2ready;

    public HandleAClient(Socket socket,TextArea textArea,int clientNo, Simulation sim) {
      this.socket = socket;
      this.textArea = textArea;
      this.sim = sim;
      this.clientNum = clientNo;
      p1ready = 0; // 0 = false
      p2ready = 0; // 1 = true
      if (clientNo==1){
          player = new Diamond(120, 120,10);
          player2 = new Diamond(60,60,10);
      }
      else{
          player = new Diamond(60,60,10);
          player2 = new Diamond(120,120,10);
      }    
    }
    
    public int getScore(int playerNum){
        return sim.getScore(playerNum);
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
              case EVOLVE: {
                  sim.evolve(Integer.parseInt(inputFromClient.readLine()),clientNum);
                  break;
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
                  break;
              }
              case GET_CLIENT_NUM: {
                  outputToClient.println(clientNum);
                  outputToClient.flush();
                  break;
              }
              case SEND_READY: {
                  if (clientNum ==1){
                      if (Integer.parseInt(inputFromClient.readLine())==1){
                          p1ready = 1; //true
                      } else {
                          p1ready = 0; //false
                      }
                  } else if (clientNum == 2) {
                      if (Integer.parseInt(inputFromClient.readLine())==1){
                          p2ready = 1;
                      } else {
                          p2ready = 0;
                      }
                  }
                  break;
              }
              case GET_READY: {
                  if (clientNum ==1 ){
                      outputToClient.println(p2ready);
                  } else if (clientNum == 2){
                      outputToClient.println(p1ready);
                  }
                  outputToClient.flush();
                  break;
              }
              case START_GAME_SIGNAL: {
                  if (p1ready == 1 && p1ready == 1){
                      outputToClient.println(1);
                  } else {
                      outputToClient.println(0);
                  }
                  outputToClient.flush();
                  break;
              }
              case GET_SCORE: {
                  outputToClient.println(getScore(1));
                  outputToClient.println(getScore(2));
                  outputToClient.flush();
                  break;
              }
              case END_GAME: {
                  // WHAT DOES THE SERVER DO WHEN WE END THE GAME?????????
                  sim.endGame(Integer.parseInt(inputFromClient.readLine()));
                  break;
              }
              case GET_BALL_X: {
                  outputToClient.println(sim.getBall().getX());
                  outputToClient.flush();
                  break;
              }
              case GET_BALL_Y: {
                  outputToClient.println(sim.getBall().getY());
                  outputToClient.flush();
                  break;
              }
          }
        }
      }
      catch(IOException ex) {
          Platform.runLater(()->textArea.appendText("Exception in client thread: "+ex.toString()+"\n"));
          FXMLController.clientLeft();
      }
    }
  }