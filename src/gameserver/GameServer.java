
package gameserver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GameServer extends Application{
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXML.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.setTitle("Game Server");
        // Closing the main window does is not sufficient to completely kill
        // the application, since we are going to be running a server thread.
        // Calling System.exit() should do the trick.
        stage.setOnCloseRequest(event->System.exit(0));
        stage.show();
    }
    
    public static void main(String[] args) {
       launch(args);
    }
    
}
