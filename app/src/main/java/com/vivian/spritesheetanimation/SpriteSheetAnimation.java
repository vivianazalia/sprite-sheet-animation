package com.vivian.spritesheetanimation;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.RotateAnimation;

public class SpriteSheetAnimation extends Activity {

    GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        setContentView(gameView);
    }

    class GameView extends SurfaceView implements Runnable {
        Thread gameThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playing;

        Canvas canvas;
        Paint paint;
        Display display;

        long fps;
        private long timeThisFrame;

        Bitmap bitmap;
        Bitmap bitmapFlip;

        boolean isMoving = false;
        float walkSpeedPerSecond = 150;
        float charXPosition = 10;

        int xMaxPosition;
        int xMinPosition = 0;

        boolean bound = false;

        private int frameWidth = 200;
        private int frameHeight = 250;
        private int frameCount = 8;
        private int currentFrame = 0;
        private long lastFrameChangeTime = 0;
        private int frameLengthInMilliseconds = 100;

        private Rect frameToDraw = new Rect(0, 0, frameWidth, frameHeight);

        RectF whereToDraw = new RectF(charXPosition, 0, charXPosition + frameWidth, frameHeight);

        public GameView(Context context) {
            super(context);

            display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            ourHolder = getHolder();
            paint = new Paint();

            bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.walk);

            bitmap = Bitmap.createScaledBitmap(bitmap, frameWidth * frameCount, frameHeight, false);
            bitmapFlip = Bitmap.createScaledBitmap(bitmap, -frameWidth * frameCount, frameHeight, false);

            xMaxPosition = getResources().getDisplayMetrics().widthPixels - frameWidth;

            playing = true;
        }

        @Override
        public void run() {
            while (playing) {
                long startFrameTime = System.currentTimeMillis();

                update();

                draw();

                timeThisFrame = System.currentTimeMillis() - startFrameTime;

                if (timeThisFrame > 0) {
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        public void draw() {
            if (ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();

                canvas.drawColor(Color.argb(255, 26, 128, 182));

                paint.setColor(Color.argb(255, 249, 129, 0));
                paint.setTextSize(45);

                canvas.drawText("FPS : " + fps, 20, 40, paint);

                whereToDraw.set((int) charXPosition, 70, (int) charXPosition + frameWidth, frameHeight + 70);

                getCurrentFrame();

                //untuk flip karakter
                if (!bound)
                {
                    canvas.drawBitmap(bitmap, frameToDraw, whereToDraw, paint);
                } else
                {
                    canvas.drawBitmap(bitmapFlip, frameToDraw, whereToDraw, paint);
                }

                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        public void update() {
            if (isMoving) {
                if ((charXPosition < xMaxPosition) & !bound) {
                    charXPosition= charXPosition + (walkSpeedPerSecond / fps);
                    if (charXPosition >= xMaxPosition) {
                        bound = true;
                    }
                } else if ((charXPosition > xMinPosition) & bound) {
                    charXPosition = charXPosition - (walkSpeedPerSecond / fps);
                    if (charXPosition <= xMinPosition) {
                        bound = false;
                    }
                }
            }
        }

        public void getCurrentFrame() {
            long time = System.currentTimeMillis();
            if (isMoving) {
                if (time > lastFrameChangeTime + frameLengthInMilliseconds) {
                    lastFrameChangeTime = time;
                    currentFrame++;
                    if (currentFrame >= frameCount) {
                        currentFrame = 0;
                    }
                }

                frameToDraw.left = currentFrame * frameWidth;
                frameToDraw.right = frameToDraw.left + frameWidth;
            }
        }

        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }
        }

        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        //Override class View
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    isMoving = true;
                    break;
                case MotionEvent.ACTION_UP:
                    isMoving = false;
                    break;
            }

            return true;
        }
    }

        @Override
        protected void onResume() {
            super.onResume();

            gameView.resume();
        }

        @Override
        protected void onPause() {
            super.onPause();

            gameView.pause();
        }
    }