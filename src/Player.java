import com.raylib.java.raymath.Vector2;
public class Player {
    int pLife = 5;
    int pBullets = 0;
    Vector2 ballPosition = new Vector2();
    float velocity = 7.0f;
    public boolean shotMode() {return ( pBullets>=5);}

}
