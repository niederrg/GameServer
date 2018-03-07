
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
    private static Diamond player1;
    private static Diamond player2;
    private Simulation sim;
    private int clientNum;
    private static int p1ready;
    private static int p2ready;
    private final static int speed = 10;

    public HandleAClient(Socket socket,TextArea textArea,int clientNo, Simulation sim) {
        this.socket = socket;
        this.textArea = textArea;
        this.sim = sim;
        this.clientNum = clientNo;
        p1ready = 0; // 0 = false
        p2ready = 0; // 1 = true
        if (clientNo==1){
            player1 = new Diamond(120, 120,10);
            player2 = new Diamond(60,60,10);
        }
        else{
            player1 = new Diamond(60,60,10);
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
                    case SEND_MOVEMENT: {
                        String direction = inputFromClient.readLine();
                        switch(direction){
                            case "down": 
                                sim.moveInner(0, speed, clientNum);
                                break;
                            case "up": 
                                sim.moveInner(0,-1*speed,clientNum);
                                break;
                            case "right": 
                                sim.moveInner(speed, 0, clientNum);
                                break;
                            case "left":
                                sim.moveInner(-1*speed, 0, clientNum);
                                break;
                        }
                    }
                    case GET_POINTS: {
                        int playerNum = Integer.parseInt(inputFromClient.readLine());
                        if (playerNum==1){
                            for(int i=0;i<4;i++){
                                outputToClient.println(player1.getWallEndX(i));
                                outputToClient.println(player1.getWallEndY(i));
                            }
                        }else{
                            for(int i=0;i<4;i++){
                                outputToClient.println(player2.getWallEndX(i));
                                outputToClient.println(player2.getWallEndY(i));
                            }
                        }
                        outputToClient.flush();
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
                        } else {
                            inputFromClient.readLine();
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
                        if (p1ready == 1 && p2ready == 1){
                            outputToClient.println(1);
                        } else {
                            outputToClient.println(0);
                        }
                        outputToClient.flush();
                        break;
                    }
                    case GET_SCORE: {
                        int playerNum = Integer.parseInt(inputFromClient.readLine());
                        outputToClient.println(getScore(playerNum));
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