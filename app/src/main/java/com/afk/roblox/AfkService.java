package com.afk.roblox;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;

import java.util.Random;

public class AfkService extends AccessibilityService {
    private WindowManager wm;
    private Button btn;
    private WindowManager.LayoutParams lp;
    private boolean on = false;
    private int count = 0;
    private final Handler h = new Handler(Looper.getMainLooper());
    private final Random rnd = new Random();

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!on) return;
            doActions();
            count++;
            btn.setText("AFK ON #" + count);
            h.postDelayed(this, nextDelay());
        }
    };

    @Override
    protected void onServiceConnected() {
        if (btn != null) {
            try { wm.removeView(btn); } catch (Exception ignored) { }
        }
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        btn = new Button(this);
        btn.setText("AFK OFF");
        btn.setTextSize(12);
        btn.setAlpha(0.85f);

        lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = 20;
        lp.y = 20;

        btn.setOnTouchListener(new View.OnTouchListener() {
            int ox, oy;
            float downX, downY;
            boolean moved;

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ox = lp.x;
                        oy = lp.y;
                        downX = e.getRawX();
                        downY = e.getRawY();
                        moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float mvx = e.getRawX() - downX;
                        float mvy = e.getRawY() - downY;
                        if (Math.abs(mvx) > 15 || Math.abs(mvy) > 15) moved = true;
                        if (moved) {
                            lp.x = ox + (int) mvx;
                            lp.y = oy + (int) mvy;
                            wm.updateViewLayout(btn, lp);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved) toggle();
                        return true;
                }
                return false;
            }
        });
        wm.addView(btn, lp);
    }

    private void toggle() {
        on = !on;
        if (on) {
            count = 0;
            btn.setText("AFK ON");
            h.postDelayed(tick, 2000);
        } else {
            h.removeCallbacks(tick);
            btn.setText("AFK OFF");
        }
    }

    private long nextDelay() {
        SharedPreferences p = getSharedPreferences("afk", MODE_PRIVATE);
        int lo = p.getInt("min", 60);
        int hi = Math.max(lo, p.getInt("max", 120));
        return (lo + rnd.nextInt(hi - lo + 1)) * 1000L;
    }

    private void doActions() {
        SharedPreferences p = getSharedPreferences("afk", MODE_PRIVATE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        final int W = Math.max(dm.widthPixels, dm.heightPixels);
        final int H = Math.min(dm.widthPixels, dm.heightPixels);

        // small camera drag on the right side
        int x1 = (int) (W * 0.70);
        int y1 = (int) (H * 0.45);
        gesture(x1, y1, x1 + 40 + rnd.nextInt(80), y1 + rnd.nextInt(20) - 10, 300);

        // jump tap shortly after
        final int jx = (int) (W * p.getInt("jx", 88) / 100.0) + rnd.nextInt(21) - 10;
        final int jy = (int) (H * p.getInt("jy", 78) / 100.0) + rnd.nextInt(21) - 10;
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                gesture(jx, jy, jx, jy, 60);
            }
        }, 700);
    }

    private void gesture(int x1, int y1, int x2, int y2, long dur) {
        Path path = new Path();
        path.moveTo(x1, y1);
        if (x1 != x2 || y1 != y2) path.lineTo(x2, y2);
        GestureDescription g = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, dur))
                .build();
        dispatchGesture(g, null, null);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) { }

    @Override
    public void onInterrupt() { }

    @Override
    public void onDestroy() {
        h.removeCallbacksAndMessages(null);
        if (btn != null) {
            try { wm.removeView(btn); } catch (Exception ignored) { }
        }
        super.onDestroy();
    }
}
