package com.gmail.ilasdeveloper.fusionspreview.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.gmail.ilasdeveloper.fusionspreview.R;

public class ShapeView extends View {

    private final float[] corners = new float[8];
    private final Path outlinePath = new Path();
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ShapeView(Context context) {
        this(context, null);
    }

    public ShapeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        float cornerSize = 0f;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(
                    attrs, R.styleable.ShapeView, defStyleAttr, 0);
            cornerSize = a.getDimension(R.styleable.ShapeView_cornerSize, 0f);
            corners[0] = a.getDimension(R.styleable.ShapeView_cornerSizeTopLeft, cornerSize);
            corners[1] = corners[0];
            corners[2] = a.getDimension(R.styleable.ShapeView_cornerSizeTopRight, cornerSize);
            corners[3] = corners[2];
            corners[4] = a.getDimension(R.styleable.ShapeView_cornerSizeBottomRight, cornerSize);
            corners[5] = corners[4];
            corners[6] = a.getDimension(R.styleable.ShapeView_cornerSizeBottomLeft, cornerSize);
            corners[7] = corners[6];
            int strokeColor = a.getColor(R.styleable.ShapeView_strokeColor, Color.TRANSPARENT);
            float strokeWidth = a.getDimension(R.styleable.ShapeView_strokeWidth, 0f);
            a.recycle();

            strokePaint.setColor(strokeColor);
            strokePaint.setStrokeWidth(strokeWidth);
            strokePaint.setStyle(Paint.Style.STROKE);
        }
        setOutlineProvider(new OutlineProvider());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            rebuildPath();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.clipPath(outlinePath);
        super.onDraw(canvas);
        canvas.restore();
        if (strokePaint.getStrokeWidth() > 0f) {
            canvas.drawPath(outlinePath, strokePaint);
        }
    }

    private void rebuildPath() {
        outlinePath.reset();
        if (getWidth() > 0 && getHeight() > 0) {
            outlinePath.addRoundRect(0f, 0f, getWidth(), getHeight(), corners, Path.Direction.CW);
        }
    }

    private class OutlineProvider extends ViewOutlineProvider {

        @Override
        public void getOutline(View view, Outline outline) {
            float corner = corners[0];
            boolean isRoundRect = true;
            for (float item : corners) {
                if (item != corner) {
                    isRoundRect = false;
                    break;
                }
            }
            if (isRoundRect) {
                outline.setRoundRect(0, 0, getWidth(), getHeight(), corner);
            } else {
                outline.setConvexPath(outlinePath);
            }
        }
    }
}
