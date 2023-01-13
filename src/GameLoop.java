import com.raylib.java.Raylib;
import com.raylib.java.core.Color;
import com.raylib.java.core.rCore;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.text.rText;
import com.raylib.java.textures.Texture2D;
import com.raylib.java.textures.rTextures;


import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import static com.raylib.java.core.input.Keyboard.*;


public class GameLoop {
    final static int SCREEN_WIDTH = 1280;
    final static int SCREEN_HEIGHT = 720;
    static int q; // quadrant iterator
    static int[] bulletIndex = new int[8];
    static int bulletCount = 0;
    static int gameState = 0;//0 - menu, 1 - game
    static int unshotTargets=3;
    static long scoreTimer =0;
    static long[] highScores = new long[5];
    static boolean scoreUpdated = false;
    static int noOfScores=0;
    static rCore rcoreobj = new rCore();

    public static void centerText(Raylib rlj,String text,int size,int y,Color textColor){
        Vector2 titleTextCenter = rText.MeasureTextEx(rText.GetFontDefault(),text,size,(float)(size/10));
        rlj.text.DrawTextEx(rText.GetFontDefault(),text,new Vector2(SCREEN_WIDTH/2-titleTextCenter.x/2,y),size,(float)(size/10),textColor);
    }
    public static void generateBullet(Vector2[] bulletLocation,int n) {
        int x = 0, y = 0;
        int[][] quadSelect = {
                {0, 0},
                {0, 1},
                {1, 0},
                {1, 1}
        };
        Random r = new Random();
        for (int i = 0; i < 8; i++) {
            if (bulletIndex[i] == 0) {
                x = r.nextInt(600) + 600 * quadSelect[q % 4][0] + 20;
                y = r.nextInt(300) + 300 * quadSelect[q % 4][1] + 30;
                bulletLocation[i] = new Vector2(x, y);
                bulletIndex[i] = 1;
                bulletCount++;
                q++;
            }
        }
    }
    public static float generateTrajectoryY(float x, Vector2 startPos, Vector2 endPos){
        float m = (float)(endPos.y-startPos.y);
        m = m/(float) (endPos.x - startPos.x); //slope y = Mx + c
        float c = endPos.y - m* endPos.x; //constant y = mx + C
        return m*x +c;
    }
    public static Vector2 generateTarget() {
        int x = 0, y = 0;
        Vector2 pos = new Vector2();
        int[][] quadSelect = {
                {0, 0},
                {0, 1},
                {1, 0},
                {1, 1}
        };
        Random r = new Random();
        x = r.nextInt(600) + 600 * quadSelect[q % 4][0] + 20;
        y = r.nextInt(300) + 300 * quadSelect[q % 4][1] + 30;
        pos= new Vector2(x, y);
        q++;
        return pos;
    }
    //Returns y for given x value for slope
    public static void main(String[] args) throws InterruptedException, IOException {
        Raylib rlj = new Raylib();
        rlj.core.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Shooter x Shooter");
        rlj.core.SetTargetFPS(60);

        boolean[] bulletCollision1 =  new boolean[8];
        Vector2[] bulletLocation = new Vector2[8];
        long init_time =0;
        double timer = 0.0d;
        boolean start = false;
        long start_t = 0;
        long finalPlayerScore = 0;
        Player p1 = new Player();
        Texture2D crosshairTxr = rTextures.LoadTextureFromImage(rTextures.LoadImage("textures/crosshair.png"));
        Vector2 relMousePosition;
        boolean shot = false;
        boolean shot_start=false;
        boolean cursorSet =false;

        Player target = new Player();

        p1.ballPosition.setX((float) SCREEN_WIDTH / 8);
        p1.ballPosition.setY((float)  SCREEN_HEIGHT/ 2);

        generateBullet(bulletLocation,8);
        target.ballPosition = generateTarget();
        while (!rlj.core.WindowShouldClose() && gameState == 0){
            int i = 3;
            if (rCore.IsKeyDown(KEY_SPACE)){
                start = true;
                start_t = System.currentTimeMillis();
            }
            rlj.core.BeginDrawing();
            rlj.core.ClearBackground(new Color(15, 17, 26,255));
            centerText(rlj,"Shooter x Shooter",45,SCREEN_HEIGHT/2-150,Color.GOLD);
            centerText(rlj,"A game by Aravinda Balaji, Venkata Hem, Sai Srinivas",19,SCREEN_HEIGHT/2-100,Color.GRAY);
            if(start){
                i = 3 - (int)Math.floor(timer);
                centerText(rlj, String.valueOf(i),50,SCREEN_HEIGHT/2,Color.GOLD);
                rlj.shapes.DrawLineEx(new Vector2(SCREEN_WIDTH/2-50, SCREEN_HEIGHT/2+60),new Vector2(SCREEN_WIDTH/2+50 - (int)(100*(timer - Math.floor(timer))),SCREEN_HEIGHT/2+60),10,Color.GOLD);
                timer = (System.currentTimeMillis()-start_t)/1000.0;
                if (i<1) gameState =1;
                scoreTimer = System.currentTimeMillis();
            }else {
                centerText(rlj,"Press space to begin",30,SCREEN_HEIGHT/2+60,Color.GOLD);
            }
            rlj.core.EndDrawing();
        }

        while (!rlj.core.WindowShouldClose() && gameState == 1){
            // Detect window close button or ESC keys
            // Update
            //----------------------------------------------------------------------------------
            if (rCore.IsKeyDown(KEY_D)) p1.ballPosition.x += p1.velocity;
            else if (rCore.IsKeyDown(KEY_A)) p1.ballPosition.x -= p1.velocity;
            if (rCore.IsKeyDown(KEY_S)) p1.ballPosition.y += p1.velocity;
            else if (rCore.IsKeyDown(KEY_W)) p1.ballPosition.y -= p1.velocity;
            if(rCore.IsMouseButtonDown(0)) {shot = true; }
            relMousePosition = rCore.GetMousePosition();
            relMousePosition.x -= 25;
            relMousePosition.y -= 25;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();
            rCore.DisableCursor();
            rlj.core.ClearBackground(new Color(15, 17, 26,255));
            //Score
            centerText(rlj, String.valueOf(System.currentTimeMillis()- scoreTimer),25,25,Color.PINK);
            centerText(rlj, String.valueOf(unshotTargets),25,50,Color.WHITE);
            //Player 1 - start------------------------------------------------------------------
            //Player 1 Render
            rlj.shapes.DrawCircleV(p1.ballPosition, 30, Color.SKYBLUE);
            //Bullet segments render
            int angle=0;
            for (int i = 0; i < p1.pBullets; i++) {
                angle += 10;
                rlj.shapes.DrawRing(p1.ballPosition, 35, 40, angle, 10 + angle, 10, Color.SKYBLUE);
                angle += 10;
            }
            //Aim Reticle
            if(p1.shotMode()){
                rlj.textures.DrawTextureEx(crosshairTxr,relMousePosition,0.3f,1.0f,Color.SKYBLUE);
            }
            if (p1.shotMode() && !cursorSet){
                rcoreobj.SetMousePosition(SCREEN_WIDTH/2,SCREEN_HEIGHT/2);
                cursorSet=true;
            }
            //Player 1 - end------------------------------------------------------------------

            //target - start------------------------------------------------------------------
            //target Render
            rlj.shapes.DrawCircleV(target.ballPosition, 30, new Color(211, 81, 61,255));
            //target - end------------------------------------------------------------------

            //Bullet Shot
            if (shot && p1.pBullets>=5){
                shot = false;
                shot_start = true;
                start_t = System.currentTimeMillis();
                System.out.println("Shot!!!!!!!!!!!");
                p1.pBullets -=5;
                //point of contact of line and circle, if any
                Vector2 lineHit = new Vector2(target.ballPosition.x,generateTrajectoryY(target.ballPosition.x,p1.ballPosition,rCore.GetMousePosition()));
                //bullet hit collision
                if(rlj.shapes.CheckCollisionPointCircle(lineHit,target.ballPosition,30)){
                    unshotTargets--;
                    target.ballPosition = generateTarget();
                    cursorSet =false;
                }
            }
            if (shot_start){
                timer = (System.currentTimeMillis()-start_t)/1000.0;
                int leftRightIndicator =  (rCore.GetMousePosition().x>p1.ballPosition.x)?SCREEN_WIDTH:0;
                rlj.shapes.DrawLineEx(p1.ballPosition,new Vector2(leftRightIndicator,generateTrajectoryY(leftRightIndicator,p1.ballPosition,rCore.GetMousePosition())), (float) (15*timer*10),Color.ORANGE);
                shot_start = !(timer > 0.05f);
            }
            shot = false;

            //regenerate bullet
            if (bulletCount<=2){
                generateBullet(bulletLocation,6);
            }
            //bullet render
            for (int i = 0; i < 8; i++) {
                if (bulletIndex[i]==1){
                    rlj.shapes.DrawRectangleV(bulletLocation[i],new Vector2(10,10),new Color(255, 204, 53,255));
                }
            }
            //bullet pickup collision
            for (int i = 0; i < 8; i++) {
                bulletCollision1[i] = rlj.shapes.CheckCollisionCircleRec(p1.ballPosition,30,new Rectangle(bulletLocation[i],10,10));
                if (bulletCollision1[i] && bulletIndex[i]==1){
                    p1.pBullets++;
                    bulletIndex[i] = 0;
                    bulletCount--;
                }
            }
            //end game
            if(unshotTargets<1){
                finalPlayerScore = System.currentTimeMillis()-scoreTimer;
                gameState = 2;
            }
            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }
        while (!rlj.core.WindowShouldClose() && gameState == 2){
            if(!scoreUpdated){
                noOfScores= ScoreMgmt.readScores(highScores);
                if (noOfScores>=5 && finalPlayerScore<highScores[4]) highScores[4] = finalPlayerScore;
                else {
                    highScores[noOfScores] = finalPlayerScore;
                    System.out.println(highScores[noOfScores]+"--"+finalPlayerScore);
                    noOfScores++;
                }
                Arrays.sort(highScores,0,noOfScores);
                ScoreMgmt.writeScores(highScores,noOfScores);
                scoreUpdated = true;
            }
            rCore.EnableCursor();
            rlj.core.BeginDrawing();
            rlj.core.ClearBackground(new Color(15, 17, 26,255));
            centerText(rlj,"Finish!!!",50,30,new Color(132, 255, 255,255));
            centerText(rlj,"Your score:"+finalPlayerScore,40,80,new Color(132, 255, 255,255));
            centerText(rlj,"Leaderboard",40,200,new Color(255, 141, 71,255));
            for (int k = 0; k < noOfScores;k++) {
                centerText(rlj, (k + 1)+"."+ highScores[k],20,250+40*k,new Color(255, 141, 71,255));
            }
            rlj.core.EndDrawing();
        }
        rlj.textures.UnloadTexture(crosshairTxr);
    }
}