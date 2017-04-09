package com.mvtablayout;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by Mehul on 06-Apr-17.
 */

public class MVMaterialTab implements View.OnTouchListener{

    private final static int REVEAL_DURATION = 400;
    private final static int HIDE_DURATION = 500;

    private int mTextColor;
    private int mIconColor;
    private int mAccentColor;
    private int mPrimaryColor;
    private int mPosition;
    private float mDensity;
    private boolean mHasIcon;
    private boolean mHasCircleTabIndicator;
    private boolean mActive;

    private Context mContext;
    private Point mLastTouchedPoint;
    private View mView;
    private MVColorView mBackground;
    private Drawable mIconDrawable;
    private TextView mTextView;
    private ImageView mIconImageView;
    private ImageView mImageViewSelector;

    private MVMaterialTabListener mvMaterialTabListener;

    public MVMaterialTab(Context context, boolean hasIcon, boolean mHasCircleTabIndicator) {
        this.mContext = context;
        this.mHasIcon = hasIcon;
        this.mHasCircleTabIndicator = mHasCircleTabIndicator;
        init();

    }

    private void init() {
        mDensity = mContext.getResources().getDisplayMetrics().density;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (!mHasIcon) {
                mView = LayoutInflater.from(mContext).inflate(R.layout.tab, null);

                mTextView = (TextView) mView.findViewById(R.id.text);
            } else {
                mView = LayoutInflater.from(mContext).inflate(R.layout.tab_icon, null);

                mIconImageView = (ImageView) mView.findViewById(R.id.icon);
            }

            mImageViewSelector = (ImageView) mView.findViewById(R.id.selector);
        } else {
            if (!mHasIcon) {
                // if there is no icon
                mView = LayoutInflater.from(mContext).inflate(R.layout.material_tab, null);

                mTextView = (TextView) mView.findViewById(R.id.text);
            } else {
                // with icon
                mView = LayoutInflater.from(mContext).inflate(R.layout.material_tab_icon, null);

                mIconImageView = (ImageView) mView.findViewById(R.id.icon);
            }

            mBackground = (MVColorView) mView.findViewById(R.id.reveal);
            mImageViewSelector = (ImageView) mView.findViewById(R.id.selector);

        }

        mView.setOnTouchListener(this);
        mTextColor = Color.WHITE;            // Default Text Color
        mIconColor = Color.WHITE;           // Default Icon Color
    }

    /*Set Accent Color*/
    public void setAccentColor(int color) {
        this.mAccentColor = color;
        this.mTextColor = color;
        this.mIconColor = color;
    }

    /*  Set Primary Color*/
    public void setPrimaryColor(int color) {
        this.mPrimaryColor = color;

        if (deviceHaveRippleSupport())
            mBackground.setBackgroundColor(color);
        else
            mView.setBackgroundColor(color);
    }

    /*  Set Text Color*/
    public void setTextColor(int color) {
        mTextColor = color;
        if (mTextView != null)
            mTextView.setTextColor(color);
    }

    /*  Set Icon Color*/
    public void setIconColor(int color) {
        this.mIconColor = color;
        if (mIconImageView != null) {
            mIconImageView.setColorFilter(color);
        }
    }

    /*  Set Tab Icon*/
    public MVMaterialTab setIcon(Drawable drawable) {
        if (!mHasIcon)
            throw new RuntimeException("You had set text without icons, use text instead icon");
        this.mIconDrawable = drawable;
        this.mIconImageView.setImageDrawable(drawable);
        this.setIconColor(this.mIconColor);
        return this;

    }

    /*  Set Text*/
    public MVMaterialTab setText(CharSequence text) {
        if (mHasIcon)
            throw new RuntimeException("You had set tabs with icons, uses icon instead text");
        this.mTextView.setText(text.toString().trim().toUpperCase(Locale.US));
        return this;
    }

    public void disableTab() {
        // set 60% alpha to text color
        if (mTextView != null)
            this.mTextView.setTextColor(Color.argb(0x99, Color.red(mTextColor), Color.green(mTextColor), Color.blue(mTextColor)));
        // set 60% alpha to icon
        if (mIconImageView != null)
            setIconAlpha(0x99);

        // set transparent the selector view
        this.mImageViewSelector.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));

        mActive = false;

        if (mvMaterialTabListener != null)
            mvMaterialTabListener.onTabUnselected(this);
    }

    public void activateTab() {
        // set full color text
        if (mTextView != null)
            this.mTextView.setTextColor(mTextColor);
        // set 100% alpha to icon
        if (mIconImageView != null)
            setIconAlpha(0xFF);

        // set accent color to selector view
        this.mImageViewSelector.setBackgroundResource(R.drawable.circle);

        mActive = true;
    }

    public boolean isSelected() {
        return mActive;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        mLastTouchedPoint = new Point();
        mLastTouchedPoint.x = (int) event.getX();
        mLastTouchedPoint.y = (int) event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!deviceHaveRippleSupport()) {
                mView.setBackgroundColor(Color.argb(0x80, Color.red(mAccentColor), Color.green(mAccentColor), Color.blue(mAccentColor)));
            }

            // do nothing
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (!deviceHaveRippleSupport()) {
                mView.setBackgroundColor(mPrimaryColor);
            }
            return true;
        }

        // new effects
        if (event.getAction() == MotionEvent.ACTION_UP) {

            if (!deviceHaveRippleSupport()) {
                mView.setBackgroundColor(mPrimaryColor);
            } else {
                // set the backgroundcolor
                this.mBackground.reveal(mLastTouchedPoint.x, mLastTouchedPoint.y, Color.argb(0x80, Color.red(mAccentColor), Color.green(mAccentColor), Color.blue(mAccentColor)), 0, REVEAL_DURATION, new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mBackground.reveal(mLastTouchedPoint.x, mLastTouchedPoint.y, mPrimaryColor, 0, HIDE_DURATION, null);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
            }

            // set the click
            if (mvMaterialTabListener != null) {

                if (mActive) {
                    // if the tab is active when the user click on it it will be reselect
                    mvMaterialTabListener.onTabReselected(this);
                } else {
                    mvMaterialTabListener.onTabSelected(this);
                }
            }
            // if the tab is not activated, it will be active
            if (!mActive)
                this.activateTab();

            return true;

        }
        return false;
    }

    public View getView() {
        return mView;
    }

    public MVMaterialTab setTabListener(MVMaterialTabListener listener) {
        this.mvMaterialTabListener = listener;
        return this;
    }

    public MVMaterialTabListener getTabListener() {
        return mvMaterialTabListener;
    }


    public int getPosition() {
        return mPosition;
    }


    public void setPosition(int position) {
        this.mPosition = position;
    }

    @SuppressLint({"NewApi"})
    private void setIconAlpha(int paramInt)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            this.mIconImageView.setImageAlpha(paramInt);
            return;
        }
        this.mIconImageView.setColorFilter(Color.argb(paramInt, Color.red(this.mIconColor), Color.green(this.mIconColor), Color.blue(this.mIconColor)));
    }

    private int getTextLenght() {
        String textString = mTextView.getText().toString();
        Rect bounds = new Rect();
        Paint textPaint = mTextView.getPaint();
        textPaint.getTextBounds(textString,0,textString.length(),bounds);
        return bounds.width();
    }

    private boolean deviceHaveRippleSupport() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return false;
        }
        else {
            return true;
        }

    }

    private int getIconWidth() {
        return (int) (mDensity * 24);
    }

    public int getTabMinWidth() {
        if(mHasIcon) {
            return getIconWidth();
        }
        else {
            return getTextLenght();
        }
    }


}
