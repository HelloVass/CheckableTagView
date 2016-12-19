package xyz.geminiwen.view;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by geminiwen on 08/12/2016.
 */

public class CheckableTagView extends View {

    private ColorStateList mColorStateListText;
    private CharSequence mText;
    private float mTextSize;
    private TextPaint mTextPaint;
    private Rect mRectText;
    private CheckDrawable mCheckDrawable;

    private ValueAnimator mTextMoveAnimator;
    private boolean mIsChecked = false;


    public CheckableTagView(Context context) {
        this(context, null);
    }

    public CheckableTagView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckableTagView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float destiny = getResources().getDisplayMetrics().density;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckableTagView, defStyleAttr, 0);
        mColorStateListText = a.getColorStateList(R.styleable.CheckableTagView_android_textColor);
        mText = a.getString(R.styleable.CheckableTagView_android_text);
        mTextSize = a.getDimension(R.styleable.CheckableTagView_android_textSize, 16 * destiny);
        a.recycle();

        mCheckDrawable = new CheckDrawable();
        mRectText = new Rect();
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        if (mColorStateListText != null) {
            mTextPaint.setColor(mColorStateListText.getDefaultColor());
        }
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CheckableTagView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMeasureMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMeasureMode = MeasureSpec.getMode(heightMeasureSpec);

        int length = mText.length();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();

        mTextPaint.getTextBounds(mText.toString(), 0, length, mRectText);

        int width = mRectText.width() + paddingLeft + paddingRight;
        int height = mRectText.height() + paddingTop + paddingBottom;

        Drawable background = getBackground();
        if (background != null) {
            int backgroundWidth = background.getMinimumWidth();
            int backgroundHeight = background.getMinimumHeight();

            width = width < backgroundWidth ?  backgroundWidth: width;
            height = height < backgroundHeight ? backgroundHeight : height;
        }


        switch (widthMeasureMode) {
            case MeasureSpec.EXACTLY: {
                width = MeasureSpec.getSize(widthMeasureSpec);
                break;
            }
        }

        switch (heightMeasureMode) {
            case MeasureSpec.EXACTLY: {
                height = MeasureSpec.getSize(heightMeasureSpec);
                break;
            }
        }


        setMeasuredDimension(MeasureSpec.makeMeasureSpec(width, widthMeasureMode),
                             MeasureSpec.makeMeasureSpec(height, heightMeasureMode));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        float destiny = getResources().getDisplayMetrics().density;

        float center = (bottom - top) / 2;
        float halfDrawableHeight = mCheckDrawable.getIntrinsicHeight() / 2;

        mCheckDrawable.setBounds(
                (int)(destiny * 9),
                (int)(center - halfDrawableHeight),
                (int)(destiny * 9) + mCheckDrawable.getIntrinsicWidth(),
                (int)(center + halfDrawableHeight)
        );

    }

    public void setText(CharSequence text) {
        mText = text;
        invalidate();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mColorStateListText != null) {
            mTextPaint.setColor(mColorStateListText.getColorForState(getDrawableState(), Color.BLACK));
        }

        if (!ViewCompat.isLaidOut(this)) return;
        float destiny = getResources().getDisplayMetrics().density;
        float center = getWidth() / 2;
        float right = center + destiny * 8;

        boolean isChecked = isChecked();
        if (isChecked != mIsChecked) {
            mIsChecked = isChecked;

            if (mTextMoveAnimator != null) {
                mTextMoveAnimator.cancel();
            }

            if (isChecked) {
                mTextMoveAnimator = ValueAnimator.ofFloat(center, right);
            } else {
                mTextMoveAnimator = ValueAnimator.ofFloat(right, center);
            }
            mCheckDrawable.start();
            mTextMoveAnimator.setDuration(100);
            mTextMoveAnimator.setInterpolator(new DecelerateInterpolator());
            mTextMoveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    postInvalidate();
                }
            });
            mTextMoveAnimator.start();
        }

    }

    private boolean isChecked() {
        int[] states = getDrawableState();
        for (int state : states) {
            if (state == android.R.attr.state_checked) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerHorizontalTextPos;

        if (mIsChecked) {
            mCheckDrawable.draw(canvas);
        }


        if (mTextMoveAnimator == null) {
            centerHorizontalTextPos = getWidth() / 2;
        } else {
            centerHorizontalTextPos = (float) mTextMoveAnimator.getAnimatedValue();
        }
        float centerVerticalTextPos = ((canvas.getHeight() / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));

        int length = mText.length();
        canvas.drawText(mText, 0, length,
                        centerHorizontalTextPos,
                        centerVerticalTextPos,
                        mTextPaint);
    }

    class CheckDrawable extends Drawable implements ValueAnimator.AnimatorUpdateListener{

        private float mDestiny;
        private Path mPath;
        private Paint mPaint;
        private Path mTmpPath;
        private PathMeasure mPathMeasure;
        private ValueAnimator mValueAnimator;

        public CheckDrawable() {
            this.mDestiny = getResources().getDisplayMetrics().density;
            this.mPath = new Path();
            this.mPaint = new Paint();
            this.mPaint.setColor(Color.WHITE                                                                                  );
            this.mPaint.setStrokeWidth(2 * mDestiny);
            this.mPaint.setStyle(Paint.Style.STROKE);
            this.mTmpPath = new Path();
            this.mPathMeasure = new PathMeasure();
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            mPath.reset();

            mPath.moveTo(bounds.left, bounds.top + 4 * this.mDestiny);
            mPath.lineTo(bounds.left + 3 * this.mDestiny, bounds.top + 8 * this.mDestiny);
            mPath.lineTo(bounds.left + 10 * this.mDestiny, bounds.top);
            this.mPathMeasure.setPath(mPath, false);
        }

        @Override
        public int getIntrinsicHeight() {
            return (int)(this.mDestiny * 8);
        }

        @Override
        public int getIntrinsicWidth() {
            return (int)(this.mDestiny * 12);
        }

        public void start() {
            if (this.mValueAnimator != null) {
                this.mValueAnimator.cancel();
            }
            this.mValueAnimator = ValueAnimator.ofFloat(0, this.mPathMeasure.getLength());
            this.mValueAnimator.setDuration(100);
            this.mValueAnimator.setInterpolator(new LinearInterpolator());
            this.mValueAnimator.addUpdateListener(this);
            this.mValueAnimator.start();
        }

        @Override
        public void draw(Canvas canvas) {
            mTmpPath.reset();
            mTmpPath.lineTo(0, 0);
            float dst = (Float)mValueAnimator.getAnimatedValue();
            this.mPathMeasure.getSegment(0, dst, mTmpPath, true);
            canvas.drawPath(mTmpPath, mPaint);
        }

        @Override
        public void setAlpha(int i) {
            mPaint.setAlpha(i);
            invalidateSelf();
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            invalidateSelf();
        }
    }
}
