/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package traffic3.simulator;

/**
 *
 * @author fabio
 */
public class CivInfo {
    private int id;
    private int x;
    private int y;
    private double vx;
    private double vy;
    
    public CivInfo(int id, int x, int y, double vx, double vy) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }
    
    public int getID () {
        return id;
}
    public int getX() {
        return x;
    }
    
    public int getY() {
     return y;   
    }
    
    public double getVX() {
        return vx;
    }
    
    public double getVY() {
        return vy;
    }
    
    public void stampa() {
        System.out.println("------------------");
        System.out.println("ID "+id);
        System.out.println("X "+x);
        System.out.println("Y "+y);
        System.out.println("VX "+vx);
        System.out.println("VY "+vy);
        System.out.println("------------------");
    }
    
}
