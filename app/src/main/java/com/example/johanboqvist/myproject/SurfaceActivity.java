package com.example.johanboqvist.myproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import com.example.johanboqvist.myproject.Mob.Mob;
import com.example.johanboqvist.myproject.Mob.Player;
import java.util.Iterator;

public class SurfaceActivity extends AppCompatActivity {

    public final static int TILE_SIZE = 96;
    public final static int MAP_WIDTH = 24;
    public final static int MAP_HEIGHT = 8;
    public static int CANVAS_WIDTH;
    public static int CANVAS_HEIGHT;

    static Bitmap sprites;

    private Accelerometer accelerometer;
    private LevelData levelData;

    private GameState gameState;
    private boolean touched = false;

    public float scrollX = 0.f;
    public float scrollY = 0.f;
    double clock = 300;
    int points = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        final GameView gameView = new GameView(SurfaceActivity.this);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sprites);

        sprites = Bitmap.createScaledBitmap(bitmap,
                114, 164, false);

        levelData = new LevelData(this);
        levelData.loadLevel();

        gameState = new Playing();

        accelerometer = new Accelerometer(this);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.frame);
        relativeLayout.addView(gameView);



    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public synchronized void move(double delta) {
        float speed = 120f;
        float moveX = accelerometer.getY() * speed * (float)delta;
        float moveY = accelerometer.getX() * speed * (float)delta;
        Player player = levelData.player;

        float mapX = (player.getX() + scrollX);
        float mapY = (player.getY() + scrollY);

        player.update(delta);

        if(moveX > 0){
            player.setDir(1);
        } else {
            player.setDir(-1);
        }

        if(!isCollision(mapX, mapY, moveX, 0)) {
            scrollX += moveX;
        }
        if(!isCollision(mapX, mapY, 0, moveY)) {
            scrollY += moveY;
        }
    }

    public boolean isCollision(float posX, float posY, float moveX, float moveY){
        for(int i = 0; i < 4; i++) {

            int newX = (int)((posX + (i%2)*TILE_SIZE + moveX) / TILE_SIZE);
            int newY = (int)((posY + (i/2)*TILE_SIZE + moveY) / TILE_SIZE);

            if (newX > MAP_WIDTH || newX / TILE_SIZE < 0) {
                return true;
            }
            if (newY > MAP_HEIGHT || newY < 0) {
                return true;
            }

            if (levelData.map.get(newX + newY * MAP_WIDTH) == '1') {
                return true;
            }
        }
        return false;
    }




    private class GameView extends SurfaceView implements SurfaceHolder.Callback {

        private Thread gameThread = null;
        private SurfaceHolder surfaceHolder;

        public GameView(Context context) {
            super(context);

            surfaceHolder = getHolder();
            surfaceHolder.addCallback(this);

            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    touched = true;
                }
            });
        }

        public void surfaceCreated(SurfaceHolder holder) {

            this.gameThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    Canvas canvas;

                    CANVAS_WIDTH = getWidth();
                    CANVAS_HEIGHT = getHeight();

                    long now, lastTime = System.nanoTime();
                    double delta = 0;

                    while (true) {
                        now = System.nanoTime();
                        delta = (now - lastTime) / 1000000000.0;
                        lastTime = now;

                        gameState.update(delta);

                        if(levelData.collected == levelData.coins){
                            gameState = new LevelTransition();
                        }

                        if(!surfaceHolder.getSurface().isValid())
                            continue;

                        canvas = surfaceHolder.lockCanvas();

                        /* draw here ! */
                        if (null != canvas) {
                            gameState.render(canvas);
                            surfaceHolder.unlockCanvasAndPost(canvas);

                        }

                    }
                }
            });

            this.gameThread.start();

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

    }

    private abstract class GameState {
        public abstract void render(Canvas canvas);
        public abstract void update(double delta);
    }

    private class Playing extends GameState{

        @Override
        public void render(Canvas canvas) {
            canvas.drawColor(Color.BLACK);


            int left = (int)(scrollX / TILE_SIZE) ;
            int top = (int)(scrollY / TILE_SIZE);

            for(int y = top; y < top + 12; y++) {
                if(y < 0 || y > MAP_HEIGHT-1) continue;
                for(int x = left; x < left + 32; x++) {

                    if(x < 0 || x > MAP_WIDTH-1) continue;

                    if((x + y * MAP_WIDTH) >= levelData.map.size() || (x+y*MAP_WIDTH < 0)) continue;
                    int c = levelData.map.get(x + y * MAP_WIDTH);

                    int offsetX = (int)scrollX;
                    int offsetY = (int)scrollY;

                    if(c == '1') {
                        RectF re = new RectF(x * TILE_SIZE - offsetX, y*TILE_SIZE - offsetY,
                                x * TILE_SIZE - offsetX + TILE_SIZE, y * TILE_SIZE - offsetY + TILE_SIZE);
                        Rect d = new Rect(16, 16*5, 16 + 16, 16*5 + 16);
                        canvas.drawBitmap(sprites, d, re, null);
                    } else {
                        RectF re = new RectF(x * TILE_SIZE - offsetX, y*TILE_SIZE - offsetY,
                                x * TILE_SIZE - offsetX + TILE_SIZE, y * TILE_SIZE - offsetY + TILE_SIZE);
                        Rect d = new Rect(0, 16*5, 16, 16*5 + 16);
                        canvas.drawBitmap(sprites, d, re, null);
                    }


                }
            }


            for(Mob mob : levelData.npcs){
                RectF r = new RectF(mob.getX() - scrollX, mob.getY() - scrollY,
                        mob.getX() + TILE_SIZE - scrollX, mob.getY() + TILE_SIZE - scrollY);
                canvas.drawBitmap(sprites, mob.getFrame(), r, null );
            }

            Player player = levelData.player;
            canvas.drawBitmap(sprites, player.getFrame(), new RectF(player.getX(), player.getY(),
                    player.getX() + TILE_SIZE, player.getY() + TILE_SIZE), null);


            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(48f);
            String bottomBar = "Coins: "+levelData.collected+" / "+levelData.coins
                    + "    Time: " + (int) clock + "    Total points: "+ points;
            canvas.drawText(bottomBar, 50, CANVAS_HEIGHT - 48, paint);
        }

        public void update(double delta){

            clock = clock - delta;

            Iterator<Mob> i = levelData.npcs.iterator();
            while(i.hasNext()) {
                Mob mob = i.next();

                //insert check for out of bounds here!
                mob.update(delta);

                if(isCollision(mob.getX(), mob.getY(), 0, 0)){
                    mob.handleCollision();
                }

                if(mob.getRect(scrollX, scrollY).intersect(levelData.player.getRect())){
                    if(mob.isCollectible()){
                        levelData.collected++;
                        i.remove();
                    } else {
                        scrollX = 0;
                        scrollY = 0;
                    }
                }
            }

            move(delta);
        }
    }

    private class LevelTransition extends GameState {


        public void render(Canvas canvas) {
            canvas.drawColor(Color.argb(40, 100, 220, 100));
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(128f);

            String title = "STAGE COMPLETE!";
            Rect bounds = new Rect();
            paint.getTextBounds(title, 0, title.length(), bounds);
            int x = (canvas.getWidth() / 2) - (bounds.width() / 2);
            int y = (canvas.getHeight() / 2) - (bounds.height() / 2);
            canvas.drawText(title,  x, y , paint);


            paint.setTextSize(64f);
            String points = "Time bonus: " + (int) clock;
            bounds = new Rect();
            paint.getTextBounds(points, 0, points.length(), bounds);
            x = (canvas.getWidth() / 2) - (bounds.width() / 2);
            y = (canvas.getHeight() / 2) - (bounds.height() / 2);
            canvas.drawText(points, x, y + 64, paint);



        }

        @Override
        public void update(double delta) {

            if(touched){
                touched = false;
                points += (int) clock;
                scrollX = 0;
                scrollY = 0;
                clock = 300;
                levelData.collected = 0;
                levelData.coins = 0;
                levelData.loadLevel();

                gameState = new Playing();
            }
        }


    }
}
