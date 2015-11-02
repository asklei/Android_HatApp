package edu.msu.xulei1.leixumadhatter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.Serializable;

/**
 * The view we will draw out hatter in
 */
public class HatterView extends View {

    /*
    * The ID values for each of the hat types. The values must
    * match the index into the array hats_spinner in arrays.xml.
    */
    public static final int HAT_BLACK = 0 ;
    public static final int HAT_GRAY = 1 ;
    public static final int HAT_CUSTOM = 2 ;

    /**
     * The current parameters
     */
    private Parameters params = new Parameters();
    /**
     * The image bitmap. None initially.
     */
    private Bitmap imageBitmap = null ;
    /**
     * Image drawing scale
     */
    private float imageScale = 1 ;

    /**
     * Image left margin in pixels
     */
    private float marginLeft = 0 ;

    /**
     * Image top margin in pixels
     */
    private float marginTop = 0 ;

    /**
     * The bitmap to draw the hat
     */
    private Bitmap hatBitmap = null ;

    /**
     * The bitmap to draw the feather
     */
    private Bitmap featherBitmap = null ;

    /**
     * The bitmap to draw the hat band. We draw this
     * only when drawing the custom color hat, so we
     * don't color the hat band
     */
    private Bitmap hatbandBitmap = null ;

    /**
     * Paint to use when drawing the custom color hat
     */
    private Paint customPaint;

    /**
     * First touch status
     */
    private Touch touch1 = new Touch();

    /**
     * Second touch status
     */
    private Touch touch2 = new Touch();

    public HatterView(Context context) {
        super(context);
        init( null, 0);
    }

    public HatterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0 );
    }

    public HatterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        setHat( HAT_BLACK);
        customPaint = new Paint();
        customPaint.setColorFilter( new LightingColorFilter( params .color , 0 ));
        setFeather( false);
    }
    /**
     * Save the view state to a bundle
     * @param key key name to use in the bundle
     * @param bundle bundle to save to
     */
    public void putToBundle(String key, Bundle bundle) {
        bundle.putSerializable(key, params);
    }

    /**
     * Get the view state from a bundle
     * @param key key name to use in the bundle
     * @param bundle bundle to load from
     */
    public void getFromBundle(String key, Bundle bundle) {
        params = (Parameters)bundle.getSerializable(key);
        // Ensure the options are all set
        setColor( params .color );
        setImagePath( params. imagePath);
        setHat( params. hat);
        setFeather( params. drawTheFeather);
    }
    /**
     * Handle a draw event
     * @param canvas canvas to draw on.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // If there is no image to draw, we do nothing
        if( imageBitmap == null ) {
            return;
        }

        /*
         * Determine the margins and scale to draw the image
         * centered and scaled to maximum size on any display
         */
        // Get the canvas size
        float wid = canvas.getWidth();
        float hit = canvas.getHeight();

        // What would be the scale to draw the where it fits both
        // horizontally and vertically?
        float scaleH = wid / imageBitmap.getWidth();
        float scaleV = hit / imageBitmap.getHeight();

        // Use the lesser of the two
        imageScale = scaleH < scaleV ? scaleH : scaleV;

        // What is the scaled image size?
        float iWid = imageScale * imageBitmap .getWidth();
        float iHit = imageScale * imageBitmap .getHeight();

        // Determine the top and left margins to center
        marginLeft = (wid - iWid) / 2;
        marginTop = (hit - iHit) / 2;

        /*
         * Draw the image bitmap
         */
        canvas.save();
        canvas.translate( marginLeft, marginTop);
        canvas.scale( imageScale, imageScale);
        canvas.drawBitmap( imageBitmap, 0, 0, null);
        /*
         * Draw the hat
         */
        canvas.translate( params .hatX , params. hatY);
        canvas.scale( params. hatScale, params .hatScale );
        canvas.rotate( params. hatAngle);

        if( params. hat == HAT_CUSTOM ) {
            canvas.drawBitmap( hatBitmap, 0, 0, customPaint );
        } else {
            canvas.drawBitmap( hatBitmap, 0, 0, null );
        }

        if( params. drawTheFeather ) {
            // Android scaled images that it loads. The placement of the
            // feather is at 322, 22 on the original image when it was
            // 500 pixels wide. It will have to move based on how big
            // the hat image actually is.
            float factor = hatBitmap .getWidth() / 500.0f ;
            canvas.drawBitmap( featherBitmap, 322 * factor, 22 * factor, null);
        }

        if( hatbandBitmap != null ) {
            canvas.drawBitmap( hatbandBitmap, 0, 0, null );
        }
        canvas.restore();
    }

    /**
     * Get the installed image path
     * @return path or null if none 
     */
    public String getImagePath() {
        return params . imagePath;
    }

    /**
     * Set an image path
     * @param imagePath path to image file
     */
    public void setImagePath(String imagePath) {
        params. imagePath = imagePath;

        imageBitmap = BitmapFactory. decodeFile(imagePath);
        invalidate();
    }

    /**
     * Get the current hat type
     * @return one of the hat type values HAT_BLACK, etc. 
     */
    public int getHat() {
        return params . hat;
    }

    /**
     * Set the hat type
     * @param hat hat type value, HAT_BLACK, etc.
     */
    public void setHat( int hat) {
        params. hat = hat;
        hatBitmap = null;
        hatbandBitmap = null;

        switch(hat) {
            case HAT_BLACK:
                hatBitmap = BitmapFactory. decodeResource(getResources(), R.drawable.hat_black );
                break;

            case HAT_GRAY:
                hatBitmap = BitmapFactory. decodeResource(getResources(), R.drawable.hat_gray );
                break;

            case HAT_CUSTOM:
                hatBitmap = BitmapFactory. decodeResource(getResources(), R.drawable.hat_white );
                hatbandBitmap = BitmapFactory. decodeResource(getResources(), R.drawable.hat_white_band );
                break;
        }

        invalidate();
    }

    /**
     * set the feather
     */
    public void setFeather(Boolean withFeather) {
        params. drawTheFeather = withFeather;
        featherBitmap = null;
        if ( params. drawTheFeather ) {
            featherBitmap =  BitmapFactory. decodeResource(getResources(), R.drawable.feather );
        }
        invalidate();
    }

    /**
     * Handle a touch event
     * @param event The touch event
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int id = event.getPointerId(event.getActionIndex());

        switch(event.getActionMasked()) {
            case MotionEvent. ACTION_DOWN :
                touch1. id = id;
                touch2. id = -1 ;
                getPositions(event);
                touch1.copyToLast();
                return true;

            case MotionEvent. ACTION_POINTER_DOWN :
                if( touch1. id >= 0 && touch2. id < 0 ) {
                    touch2. id = id;
                    getPositions(event);
                    touch2.copyToLast();
                    return true;
                }
                break;

            case MotionEvent. ACTION_UP :
            case MotionEvent. ACTION_CANCEL :
                touch1. id = -1 ;
                touch2. id = -1 ;
                invalidate();
                return true;

            case MotionEvent. ACTION_POINTER_UP :
                if(id == touch2 .id ) {
                    touch2. id = -1 ;
                } else if(id == touch1 .id ) {
                    // Make what was touch2 now be touch1 by
                    // swapping the objects. 
                    Touch t = touch1;
                    touch1 = touch2 ;
                    touch2 = t;
                    touch2. id = -1 ;
                }
                invalidate();
                return true;

            case MotionEvent. ACTION_MOVE :
                getPositions(event);
                move();
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * Get the positions for the two touches and put them
     * into the appropriate touch objects.
     * @param event the motion event
     */
    private void getPositions(MotionEvent event) {
        for( int i= 0;  i<event.getPointerCount();  i++) {

            // Get the pointer id
            int id = event.getPointerId(i);

            // Convert to image coordinates
            float x = (event.getX(i) - marginLeft ) / imageScale ;
            float y = (event.getY(i) - marginTop ) / imageScale ;

            if(id == touch1 .id ) {
                touch1.copyToLast();
                touch1. x = x;
                touch1. y = y;
            } else if(id == touch2 .id ) {
                touch2.copyToLast();
                touch2. x = x;
                touch2. y = y;
            }
        }

        invalidate();
    }

    /**
     * Handle movement of the touches
     */
    private void move() {
        // If no touch1, we have nothing to do
        // This should not happen, but it never hurts
        // to check.
        if( touch1. id < 0 ) {
            return;
        }

        if( touch1. id >= 0 ) {
            // At least one touch
            // We are moving
            touch1.computeDeltas();

            params. hatX += touch1 . dX;
            params. hatY += touch1 . dY;
        }

        if( touch2. id >= 0 ) {
            // Two touches
            /*
            * Scaling
             */
            float dis1 = distance( touch1 .lastX , touch1. lastY, touch2 .lastX , touch2 .lastY );
            float dis2 = distance( touch1 .x , touch1. y, touch2. x , touch2 . y);
            float dd = dis2/dis1;
            scale(dd, touch1. x , touch1 . y);
            /*
             * Rotation
             */
            float angle1 = angle( touch1 .lastX , touch1. lastY, touch2 .lastX , touch2 .lastY );
            float angle2 = angle( touch1 .x , touch1. y, touch2. x , touch2 . y);
            float da = angle2 - angle1;
            rotate(da, touch1. x , touch1 . y);
        }
    }

    /**
     * Rotate the image around the point x1, y1
     * @param dAngle Angle to rotate in degrees
     * @param x1 rotation point x
     * @param y1 rotation point y
     */
    public void rotate( float dAngle, float x1, float y1) {
        params. hatAngle += dAngle;

        // Compute the radians angle
        double rAngle = Math. toRadians(dAngle);
        float ca = ( float) Math. cos(rAngle);
        float sa = ( float) Math. sin(rAngle);
        float xp = ( params. hatX - x1) * ca - ( params. hatY - y1) * sa + x1;
        float yp = ( params. hatX - x1) * sa + ( params. hatY - y1) * ca + y1;

        params. hatX = xp;
        params. hatY = yp;
    }

    /**
     * Scale the image
     * @param dd distance ratio
     * @param x1 scaling point x
     * @param y1 scaling point y
     */
    public void scale( float dd, float x1, float y1){
        params. hatScale *= dd;
        float xp = dd*( params. hatX - x1) + x1;
        float yp = dd*( params. hatY - y1) + y1;
        params. hatX = xp;
        params. hatY = yp;
    }

    /**
     * Determine the angle for two touches
     * @param x1 Touch 1 x
     * @param y1 Touch 1 y
     * @param x2 Touch 2 x
     * @param y2 Touch 2 y
     * @return computed angle in degrees 
     */
    private float angle( float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return ( float) Math. toDegrees(Math.atan2 (dy, dx));
    }

    /**
     * Determine the distance for two touches
     * @param x1 Touch 1 x
     * @param y1 Touch 1 y
     * @param x2 Touch 2 x
     * @param y2 Touch 2 y
     */
    private float distance( float x1, float y1, float x2, float y2) {
        return ( float)Math. pow(Math.pow (x2 - x1, 2 ) + Math.pow(y2 - y1, 2), 0.5);
    }

    /**
     * Get the current custom hat color
     * @return hat color integer value 
     */
    public int getColor() {
        return params . color;
    }

    /**
     * Set the current custom hat color
     * @param color hat color integer value
     */
    public void setColor( int color) {
        params. color = color;

        // Create a new filter to tint the bitmap
        customPaint.setColorFilter( new LightingColorFilter(color, 0));
        invalidate();
    }

    private static class Parameters implements Serializable {
        /**
         * Path to the image file if one exists
         */
        public String imagePath = null ;
        /**
         * The current hat type
         */
        public int hat = HAT_BLACK;
        /**
         * X location of hat relative to the image
         */
        public float hatX = 0 ;

        /**
         * Y location of hat relative to the image
         */
        public float hatY = 0 ;

        /**
         * Hat scale, also relative to the image
         */
        public float hatScale = 0.5f ;

        /**
         * Hat rotation angle
         */
        public float hatAngle = 0 ;

        /**
         * Custom hat color
         */
        public int color = Color. CYAN;
        public boolean drawTheFeather = false;
    }

    /**
     * Local class to handle the touch status for one touch.
     * We will have one object of this type for each of the
     * two possible touches.
     */
    private class Touch {
        /**
         * Touch id
         */
        public int id = -1 ;

        /**
         * Current x location
         */
        public float x = 0 ;

        /**
         * Current y location
         */
        public float y = 0 ;

        /**
         * Previous x location
         */
        public float lastX = 0 ;

        /**
         * Previous y location
         */
        public float lastY = 0 ;

        /**
         * Change in x value from previous
         */
        public float dX = 0 ;

        /**
         * Change in y value from previous
         */
        public float dY = 0 ;

        /**
         * Copy the current values to the previous values
         */
        public void copyToLast() {
            lastX = x ;
            lastY = y ;
        }

        /**
         * Compute the values of dX and dY
         */
        public void computeDeltas() {
            dX = x - lastX ;
            dY = y - lastY ;
        }
    }

}