
package gameserver;

import simulation.Box;

public class Player {
    private Box paddle;
    
    public Player (int startingX, int startingY){
        paddle = new Box(startingX, startingY, 20, 20, true);
    }
    
    public int getX(){ return paddle.getX(); }
    public int getY(){ return paddle.getY(); }
    public void move(int dX, int dY){
        paddle.move(dX, dY);
    }
}
