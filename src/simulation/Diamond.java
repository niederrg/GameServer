package simulation;

import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import physics.*;

//Henry Killough
//Feb 27, 2018
//IHRTLUHC 

public class Diamond 
{
    private ArrayList<LineSegment> walls;
    private Polygon diamond;
    public int x;
    public int y;
    public int r;
    Point top, bottom, left, right;
    
    
    public Diamond(int x, int y, int r) {
        this.x = x;
        this.y = y;
        this.r = r;
        top = new Point(x+r, y);
        right = new Point(x+(2*r), y+r);
        left = new Point(x, y+r);
        bottom = new Point(x+r, y+(2*r));
        

        
        walls = new ArrayList<LineSegment>();

        walls.add(new LineSegment(top, left));
        walls.add(new LineSegment(left, bottom));
        walls.add(new LineSegment(bottom, right));
        walls.add(new LineSegment(right, top));
    }
    
    
    
    public void setX(int newX) {
        double deltaX = newX - this.x;
        this.top.x += deltaX;
        this.right.x += deltaX;
        this.bottom.x += deltaX;
        this.left.x += deltaX;
        this.x = newX;
    }
    
    public void setY(int newY) {
        double deltaY = newY - this.y;
        this.top.y += deltaY;
        this.right.y += deltaY;
        this.bottom.y += deltaY;
        this.left.y += deltaY;
        this.y = newY;
    }
    
    public Ray bounceRay(Ray in,double time)
    {
        // For each of the walls, check to see if the Ray intersects the wall
        Point intersection = null;
        for(int n = 0;n < walls.size();n++)
        {
            LineSegment seg = in.toSegment(time);
            intersection = walls.get(n).intersection(seg);
            if(intersection != null)
            {
                // If it intersects, find out when
                double t = in.getTime(intersection);
                // Reflect the Ray off the line segment
                Ray newRay = walls.get(n).reflect(seg,in.speed);
                // Figure out where we end up after the reflection.
                Point dest = newRay.endPoint(time-t);
                return new Ray(dest,newRay.v,in.speed);
            }
        }
        return null;
    }
    
    public void move(int deltaX,int deltaY)
    {
        for(int n = 0;n < walls.size();n++)
            walls.get(n).move(deltaX,deltaY);
        x += deltaX;
        y += deltaY;
    }
    
    public boolean contains(Point p)
    {
        if(((p.x + p.y) > (this.x + this.y + r)) && ((p.x + p.y) < (this.x + this.y + (3*r))) && (p.x>this.x) && (p.x<(this.x + (2*r)))) {
            return true;
        } else {return false; }
    }
    
    public Shape getShape()
    {
        diamond = new Polygon(top.x, top.y, right.x, right.y, bottom.x, bottom.y, left.x, left.y);
        diamond.setFill(Color.WHITE);
        diamond.setStroke(Color.BLACK);
        return diamond;
    }
    public double getWallEndX(int i){
        return walls.get(i).a.x;
    }
    public double getWallEndY(int i){
        return walls.get(i).a.y;
    }
    
    
    public void updateShape()
    {
        this.top = walls.get(0).a;
        this.left = walls.get(1).a;
        this.bottom = walls.get(2).a;
        this.right = walls.get(3).a;
        
        diamond.getPoints().clear();
        
        diamond.getPoints().addAll(
                top.x, top.y,
                right.x, right.y,
                bottom.x, bottom.y,
                left.x, left.y);
    }
}
