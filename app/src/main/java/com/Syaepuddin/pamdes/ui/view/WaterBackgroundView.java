package com.Syaepuddin.pamdes.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

/**
 * Custom View untuk membuat latar belakang animasi Liquid Water Mesh & Floating Ambient Bubbles.
 */
public class WaterBackgroundView extends View {

    private Paint backgroundPaint;
    private Paint bubblePaint1;
    private Paint bubblePaint2;
    private Paint bubblePaint3;

    private float animOffset1 = 0f;
    private float animOffset2 = 0f;
    private float animOffset3 = 0f;

    private ValueAnimator animator;

    public WaterBackgroundView(Context context) {
        super(context);
        init();
    }

    public WaterBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaterBackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint3 = new Paint(Paint.ANTI_ALIAS_FLAG);

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(12000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            animOffset1 = value * 150f;
            animOffset2 = value * -200f;
            animOffset3 = value * 120f;
            invalidate();
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (animator != null && !animator.isRunning()) {
            animator.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;

        // Base Water Linear Gradient (Deep Ocean to Teal to Soft Indigo)
        LinearGradient baseGradient = new LinearGradient(
                0, 0, w, h,
                new int[]{Color.parseColor("#0F2027"), Color.parseColor("#203A43"), Color.parseColor("#2C5364")},
                null, Shader.TileMode.CLAMP
        );
        backgroundPaint.setShader(baseGradient);

        // Ambient Sphere 1 (Cyan/Toska)
        RadialGradient radial1 = new RadialGradient(
                w * 0.2f, h * 0.3f, w * 0.7f,
                new int[]{Color.parseColor("#4400F2FE"), Color.parseColor("#114FACFE"), Color.TRANSPARENT},
                new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP
        );
        bubblePaint1.setShader(radial1);

        // Ambient Sphere 2 (Purple/Indigo)
        RadialGradient radial2 = new RadialGradient(
                w * 0.8f, h * 0.6f, w * 0.8f,
                new int[]{Color.parseColor("#447F00FF"), Color.parseColor("#11E100FF"), Color.TRANSPARENT},
                new float[]{0f, 0.6f, 1f}, Shader.TileMode.CLAMP
        );
        bubblePaint2.setShader(radial2);

        // Ambient Sphere 3 (Bright Water Teal)
        RadialGradient radial3 = new RadialGradient(
                w * 0.5f, h * 0.85f, w * 0.6f,
                new int[]{Color.parseColor("#5500C9FF"), Color.parseColor("#1192FE96"), Color.TRANSPARENT},
                new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP
        );
        bubblePaint3.setShader(radial3);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();

        // 1. Draw base gradient
        canvas.drawRect(0, 0, w, h, backgroundPaint);

        // 2. Draw animated glowing spheres
        canvas.drawCircle(w * 0.2f + animOffset1, h * 0.3f + animOffset1 * 0.5f, w * 0.7f, bubblePaint1);
        canvas.drawCircle(w * 0.8f + animOffset2, h * 0.6f + animOffset2 * 0.8f, w * 0.8f, bubblePaint2);
        canvas.drawCircle(w * 0.5f + animOffset3, h * 0.85f - animOffset3 * 0.4f, w * 0.6f, bubblePaint3);
    }
}
