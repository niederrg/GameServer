package simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javafx.scene.shape.Shape;
import physics.*;

public class Simulation {
    private Box outer;
    private Ball ball;
    private Box inner;
    private Box opponent;
    private Lock lock;
    
    public Simulation(int width,int height,int dX,int dY)
    {
        outer = new Box(0,0,width,height,false);
        ball = new Ball(width/2,height/2,dX,dY);
        inner = new Box(width - 60,height - 40, 40, 20,true);
        opponent = new Box(60, 40, 20, 20, true);
        lock = new ReentrantLock();
    }
    
    public void evolve(double time)
    {
        lock.lock();
        Ray newLoc = inner.bounceRay(ball.getRay(), time);
        if(newLoc != null)
            ball.setRay(newLoc);
        else {
            newLoc = outer.bounceRay(ball.getRay(), time);
            if(newLoc != null)
                ball.setRay(newLoc);
            else
                ball.move(time);
        } 
        lock.unlock();
    }
    
    public void moveInner(int deltaX,int deltaY)
    {
        lock.lock();
        int dX = deltaX;
        int dY = deltaY;
        if(inner.x + deltaX < 0)
          dX = -inner.x;
        if(inner.x + inner.width + deltaX > outer.width) 
          dX = outer.width - inner.width - inner.x;
       
        if(inner.y + deltaY < 0)
           dY = -inner.y;
        if(inner.y + inner.height + deltaY > outer.height)
           dY = outer.height - inner.height - inner.y;
        
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
        lock.unlock();
    }
    
    public List<Shape> setUpShapes()
    {
        ArrayList<Shape> newShapes = new ArrayList<Shape>();
        newShapes.add(outer.getShape());
        newShapes.add(inner.getShape());
        newShapes.add(ball.getShape());
        newShapes.add(opponent.getShape());
        return newShapes;
    }
    
    public void updateShapes()
    {
        opponent.updateShape();
        inner.updateShape();
        ball.updateShape();
    }
}
