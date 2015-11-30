package com.sonification.accessibleweather.parallax;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Implements a Parallax Background view,
 * Author - Rodo
 * Original code and question - http://stackoverflow.com/questions/17207612/how-to-have-a-wider-image-scrolling-in-the-background
 */

public class ParallaxBackground extends View
{

    private final static String TAG = "ParallaxBackground";
    private final static int MODE_PRESCALE = 0, MODE_POSTSCALE = 1;

    /** How much a image will be scaled  */
    /** Warning: A full screen image on a Samsung 10.1 scaled to 1.5 consumes 6Mb !! So be careful */
    private final static float FACTOR = 2.0f;

    /** The current background */
    private Bitmap mCurrentBackground = null;

    /** Current progress 0...100 */
    private float mOffsetPercent = 0;

    /** Flag to activate */
    private boolean isParallax = true;

    /** The parallax mode (MODE_XXX) */
    private int mParallaxMode = MODE_PRESCALE;

    /** pre-calc stuff to tighten onDraw calls */
    private int  mCurrentFactorWidth;
    private float mCurrentFactorMultiplier;
    private Rect mRectDestination, mRectSource;

    private Paint mPaint;

    public ParallaxBackground(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        construct(context);
    }

    public ParallaxBackground(Context context)
    {
        super(context);
        construct(context);
    }

    /**
     * Enables or disables parallax mode
     * @param status
     */
    public void setParallax(boolean status)
    {
        isParallax = status;
    }

    /**
     * Sets the parallax memory mode. MODE_PRESCALE uses more memory but scrolls slightly smoother. MODE_POSTSCALE uses less memory but is more CPU-intensive.
     * @param mode
     */
    public void setParallaxMemoryMode(int mode)
    {
        mParallaxMode = mode;
        /*if (mCurrentBackground!=null && !mCurrentBackground.isRecycled()) {
            // mCurrentBackground.recycle();
            mCurrentBackground=null;
        }*/
    }

    /**
     * Seth the percentage of the parallax scroll. 0 Means totally left, 100 means totally right.
     * @param percentage The perc,
     */
    public void setPercent(float percentage)
    {
        if(percentage == mOffsetPercent)
        {
            return;
        }
        if(percentage > 100)
        {
            percentage = 100;
        }
        if(percentage<0)
        {
            percentage = 0;
        }
        mOffsetPercent = percentage;
        invalidate();
    }

    /**
     * Wether PArallax is active or not.
     * @return ditto.
     */
    public boolean isParallax()
    {
        return isParallax && (mCurrentBackground != null);
    }

    /**
     * We override setBackgroundDrawable so we can set the background image as usual, like in a normal view.
     * If parallax is active, it will create the scaled bitmap that we use on onDraw(). If parallax is not
     * active, it will divert to super.setBackgroundDrawable() to draw the background normally.
     * If it is called with anything than a BitMapDrawable, it will clear the stored background and call super()
     */

    @Override
    public void setBackground(Drawable d)
    {
        if((!isParallax) || (!(d instanceof BitmapDrawable)))
        {
            super.setBackground(d);
            return;
        }

        switch(mParallaxMode)
        {
            case MODE_POSTSCALE:
                setBackgroundDrawable_postscale(d);
                break;

            case MODE_PRESCALE:
                setBackgroundDrawable_prescale(d);
                break;
        }
    }

    private void setBackgroundDrawable_prescale(Drawable incomingImage)
    {
        Bitmap original = ((BitmapDrawable)incomingImage).getBitmap();
        mCurrentBackground = Bitmap.createBitmap((int)(this.getWidth() * FACTOR), this.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(mCurrentBackground);

        // we crop the original image up and down, as it has been expanded to FACTOR
        // you can play with the adjustment value to crop top, center or bottom.
        // I only use center so its hardcoded.
        float scaledBitmapFinalHeight = original.getHeight() * mCurrentBackground.getWidth() / original.getWidth();
        int adjustment = 0;

        if(scaledBitmapFinalHeight > mCurrentBackground.getHeight())
        {
            // as expected, we have to crop up&down to maintain aspect ratio
            adjustment = (int)(scaledBitmapFinalHeight - mCurrentBackground.getHeight()) / 4;
        }

        Rect srect = new Rect(0, adjustment,original.getWidth(), original.getHeight() - adjustment);
        Rect drect = new Rect(0, 0, mCurrentBackground.getWidth(), mCurrentBackground.getHeight());

        canvas.drawBitmap(original, srect, drect, mPaint);

        // pre-calc factor multiplier
        mCurrentFactorMultiplier = (FACTOR - 1) * getWidth() / 100;
        if(original != null)
        {
            //original.recycle();
            original = null;
        }
        System.gc();

        invalidate();
    }

    private void setBackgroundDrawable_postscale(Drawable d)
    {
        mCurrentBackground=((BitmapDrawable) d).getBitmap();

        int currentBackgroundWidth = mCurrentBackground.getWidth(),
                currentBackgroundHeight = mCurrentBackground.getHeight(),
                currentFactorHeight = (int)(currentBackgroundHeight / FACTOR);

        mCurrentFactorWidth = (int)(currentBackgroundWidth / FACTOR);
        mCurrentFactorMultiplier = (FACTOR - 1) * currentBackgroundWidth / 100;
        mRectDestination = new Rect(0, 0, getWidth(), getHeight());
        mRectSource = new Rect(0, 0, mCurrentFactorWidth, currentFactorHeight);
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        if((isParallax) && (mCurrentBackground != null))
        {
            if(mParallaxMode == MODE_POSTSCALE)
            {
                onDraw_postscale(canvas);
            }
            else
            {
                onDraw_prescale(canvas);
            }
        }
        else
        {
            super.onDraw(canvas);
        }
    }

    private void onDraw_prescale(Canvas canvas)
    {
        int oxb = (int)(mCurrentFactorMultiplier * mOffsetPercent);
        canvas.drawBitmap(mCurrentBackground, -oxb, 0, mPaint);
    }

    private void onDraw_postscale(Canvas canvas)
    {
        int oxb = (int)(mCurrentFactorMultiplier * mOffsetPercent);
        mRectSource.left = oxb;
        mRectSource.right = mCurrentFactorWidth + oxb;
        canvas.drawBitmap(mCurrentBackground, mRectSource, mRectDestination, mPaint);
    }

    private void construct(Context context)
    {
        mPaint = new Paint();
    }
}