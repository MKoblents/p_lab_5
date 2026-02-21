package manager;

public class Coordinates {
    private long x; //Значение поля должно быть больше -617
    private long y; //Значение поля должно быть больше -842
    public String toString(){
        return "("+this.x+"; "+this.y+")";
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    public void setX(long x) {
        this.x = x;
    }

    public void setY(long y) {
        this.y = y;
    }
}