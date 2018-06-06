package pl.edu.pw.eiti.wpam.ablaszcz.phonedraw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

public class DrawableView extends View {

    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    private Rect clipBounds;

    private Model model;
    private float brushSize = 20;
    private boolean draw_via_acc=false;
    private boolean move_after_enabling_acc = true;
    private boolean outOfScreen = false;
    private float scaleFactor = 1.0f;

    public DrawableView(Context context, AttributeSet attr) {
        super(context, attr);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setupDrawing();

        model = new Model(false);
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(0xFF660000);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(drawPath, drawPaint);
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        model.setStartPoint(w / 2.0f, h / 2.0f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!draw_via_acc) {

            // get x and y
            float touchX = event.getX();
            float touchY = event.getY();

            if(touchX < 150) {
                outOfScreen = true;
                scale();
            }

            //respond to down, move and up events
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    drawPath.moveTo(touchX, touchY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    drawPath.lineTo(touchX, touchY);
                    drawCanvas.drawPath(drawPath, drawPaint);
                    break;
                case MotionEvent.ACTION_UP:
                    drawPath.lineTo(touchX, touchY);
                    model.setStartPoint(touchX, touchY);
                    reset();
                    break;
                default:
                    return false;
            }
            //redraw
            invalidate();
        }
        return true;
    }

    public void startNew(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvasBitmap.setConfig(Bitmap.Config.ARGB_8888);
        invalidate();
        drawPath.moveTo(this.getWidth() / 2.0f, this.getHeight() / 2.0f);
        model.setStartPoint(this.getWidth() / 2.0f, this.getHeight() / 2.0f);
    }

    private boolean isOutOfScreen(float[] new_pos){
        return new_pos[0] > this.getWidth() || new_pos[0] < 0 ||
                new_pos[1] > this.getHeight() || new_pos[1] < 0;
    }

    private void drawLineOrMove(float[] new_pos) {
        if (!move_after_enabling_acc){
            drawPath.lineTo(new_pos[0], new_pos[1]);
            drawCanvas.drawPath(drawPath, drawPaint);
        }
        else {
            System.out.println("MOVING ON START");
            drawPath.moveTo(new_pos[0], new_pos[1]);
            move_after_enabling_acc = false;
        }
        invalidate();
    }

    public void calcAndDrawMovement(float[] values, float ts) {
        if (draw_via_acc) {
            System.out.println("STARTING CALCULATING");
            // Reverse y
            values[1] = -values[1];

            // use model to calc movements
            float[] move = model.calc(values, ts);
            System.out.println("CALCULATED");

            // check if need to rescale
            if (isOutOfScreen(move)) {
                outOfScreen = true;
            }
            System.out.println(move[0]);

            // draw
            drawLineOrMove(move);
        }
        else {
            move_after_enabling_acc = true;
        }
    }

    public void setColor(String newColor){
        reset();
        drawPaint.setColor(Color.parseColor(newColor));
        //invalidate();
    }

    public void setDrawViaAcc(Boolean is_enabled) {
        this.draw_via_acc = is_enabled;
    }

    public void reset() {
        drawCanvas.drawPath(drawPath, drawPaint);
        drawPath.reset();
        model.reset();
    }

    public void scale() {
        if (outOfScreen) {
            System.out.println("SCALE to");
            scaleFactor = scaleFactor * 0.9f;
            //Bitmap bitmap = canvasBitmap.copy(Bitmap.Config.ARGB_8888, true);
            //drawCanvas = new Canvas(bitmap);
           // drawCanvas.scale(this.getWidth() /2 * (1- scaleFactor), this.getHeight() /2 * (1- scaleFactor));
           // drawCanvas.drawBitmap(canvasBitmap, 0,0, canvasPaint);
            //drawCanvas.drawPath(drawPath, drawPaint);
            outOfScreen = false;
            //invalidate();

        }

    }

    public void setBitmap(Bitmap bitmap) {
        canvasBitmap = bitmap;
        drawCanvas = new Canvas(canvasBitmap);
        invalidate();
    }

    public void enable3D(Boolean enable) {
        if (enable){
            model.setDraw3D();
        }
    }
}
