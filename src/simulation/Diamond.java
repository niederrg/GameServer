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
    public double x;
    public double y;
    public double r;
    Point top, bottom, left, right;
    
    
    public Diamond(double x, double y, double r) {
        this.x = x;
        this.y = y;
        this.r = r;
        top = new Point(x+r, y);
        right = new Point(x+(2*r), y+r);
        left = new Point(x, y+r);
        bottom = new Point(x+r, y+(2*r));
        
        
        
        walls = new ArrayList<LineSegment>();

        walls.add(new LineSegment(top, right));
        walls.add(new LineSegment(right, bottom));
        walls.add(new LineSegment(bottom, left));
        walls.add(new LineSegment(left, top));
    }
    
    public void setX(Double newX) {
        double deltaX = newX - this.x;
        this.top.x += deltaX;
        this.right.x += deltaX;
        this.bottom.x += deltaX;
        this.left.x += deltaX;
        this.x = newX;
    }
    
    public void setY(Double newY) {
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
        diamond = new Polygon(top.x, top.y, right.x, right.y, left.x, left.y, bottom.x, bottom.y);
        diamond.setFill(Color.WHITE);
        diamond.setStroke(Color.BLACK);
        return diamond;
    }
    
    public void updateShape()
    {
        walls.clear();
        walls.add(new LineSegment(top, right));
        walls.add(new LineSegment(right, bottom));
        walls.add(new LineSegment(bottom, left));
        walls.add(new LineSegment(left, top));
    }
}
