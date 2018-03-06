package simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javafx.scene.shape.Shape;
import physics.*;

public class Simulation {
    private Box outer;
    private Box scoreBox;
    private Ball ball;
    private Diamond player1;
    private Diamond player2;
    private Lock lock;
    private int p1score;
    private int p2score;
    
    public Simulation(int width,int height,int dX,int dY)
    {
        outer = new Box(0,0,width,height,false);
        ball = new Ball(width/2,height/2,dX,dY);
        player1 = new Diamond(width - 60,height - 40, 40);
        player2 = new Diamond(60, 40, 20);
        scoreBox = new Box(width-5,height-5,10,10,true);
        lock = new ReentrantLock();
    }
    
    public void score(int player){
        lock.lock();
        if (player == 1)
            p1score ++;
        else if (player == 2)
            p2score ++;
        lock.unlock();
    }
    
    public int getScore (int player){
        lock.lock();
        if (player == 1)
            return p1score;
        else if (player == 2)
            return p2score;
        else{ 
            lock.unlock();
            return 0;
        }
    }
    
    public void evolve(double time, int player)
    {
        lock.lock();
        
            Ray newLoc = scoreBox.bounceRay(ball.getRay(), time);
            if(newLoc != null){
                ball.setRay(newLoc);
                score(ball.getLastPlayer()); //SCORES HERE
            } else {
                newLoc = outer.bounceRay(ball.getRay(), time);
                if(newLoc != null)
                    ball.setRay(newLoc); //Bounces off the outer wall
                else {
                    newLoc = player1.bounceRay(ball.getRay(), time);
                    if (newLoc != null){
                        ball.setRay(newLoc); //bounces off player 1, sets last player to player 1
                        ball.setLastPlayer(1);
                    } else {
                        newLoc = player2.bounceRay(ball.getRay(), time);
                        if(newLoc != null){
                            ball.setRay(newLoc); //bounces off player 2, sets last player to player 2
                            ball.setLastPlayer(2);
                        } else{
                            ball.move(time);
                        }
                    }
                }
            } 
        lock.unlock();
    }
    
    public void moveInner(int deltaX,int deltaY, int player)
    {
        lock.lock();
        Diamond inner = null;
        if (player == 1){
            inner = player1;
        } else if (player == 2){
            inner = player2;
        } 
        if (inner != null){
            int dX = deltaX;
            int dY = deltaY;
            if(inner.x + deltaX < 0)
              dX = -inner.x;
            if(inner.x + (inner.r*2) + deltaX > outer.width)
              dX = outer.width - (inner.r*2) - inner.x;

            if(inner.y + deltaY < 0)
               dY = -inner.y;
            if(inner.y + (inner.r*2) + deltaY > outer.height)
               dY = (outer.height - (inner.r*2)) - inner.y;

            inner.move(dX,dY);
            if(inner.contains(ball.getRay().origin)) {
                // If we have discovered that the box has just jumped on top of
                // the ball, we nudge them apart until the box no longer
                // contains the ball.
                int bumpX = -1;
                if(dX < 0) bumpX = 1;
                int bumpY = -1;
                if(dY < 0) bumpY = 1;
                do {
                inner.move(bumpX, bumpY);
                ball.getRay().origin.x += -bumpX;
                ball.getRay().origin.y += -bumpY;
                } while(inner.contains(ball.getRay().origin));
            }
        }
        lock.unlock();
    }
    
    public List<Shape> setUpShapes()
    {
        ArrayList<Shape> newShapes = new ArrayList<Shape>();
        newShapes.add(outer.getShape());
        newShapes.add(player1.getShape());
        newShapes.add(ball.getShape());
        newShapes.add(player2.getShape());
        newShapes.add(scoreBox.getShape());
        return newShapes;
    }
    
    public void updateShapes()
    {
        player2.updateShape();
        player1.updateShape();
        ball.updateShape();
    }
}
