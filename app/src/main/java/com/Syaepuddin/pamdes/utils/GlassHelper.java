package com.Syaepuddin.pamdes.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * Helper untuk memberikan efek Liquid Glass (Frosted Glass Blur) dan animasi sentuh.
 */
public class GlassHelper {

    /**
     * Menerapkan efek blur latar belakang sesuai versi Android.
     * Android 12+ (API 31+) menggunakan RenderEffect bawaan Android.
     * Android 10-11 (API 29-30) menggunakan fallback panel semi-transparan yang pekat dan elegan.
     */
    public static void applyGlassBlur(View targetView) {
        if (targetView == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                RenderEffect blurEffect = RenderEffect.createBlurEffect(25f, 25f, Shader.TileMode.MIRROR);
                targetView.setRenderEffect(blurEffect);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Menambahkan animasi efek tekan (scale & bounce) pada tombol atau kartu glass.
     */
    @SuppressLint("ClickableViewAccessibility")
    public static void setupGlassTouchAnimation(View view, Runnable onClickAction) {
        if (view == null) return;

        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(v, "scaleX", 0.95f);
                    ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(v, "scaleY", 0.95f);
                    AnimatorSet setDown = new AnimatorSet();
                    setDown.playTogether(scaleXDown, scaleYDown);
                    setDown.setDuration(100);
                    setDown.start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(v, "scaleX", 1.0f);
                    ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(v, "scaleY", 1.0f);
                    scaleXUp.setInterpolator(new OvershootInterpolator(3.0f));
                    scaleYUp.setInterpolator(new OvershootInterpolator(3.0f));
                    AnimatorSet setUp = new AnimatorSet();
                    setUp.playTogether(scaleXUp, scaleYUp);
                    setUp.setDuration(250);
                    setUp.start();

                    if (event.getAction() == MotionEvent.ACTION_UP && onClickAction != null) {
                        v.postDelayed(onClickAction, 50);
                    }
                    break;
            }
            return true;
        });
    }
}
