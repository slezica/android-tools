package me.slezica.tools;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ActivitySlider {
    static final String SNAPSHOT = "_desktop_snapshot";
    
    static final Cache<Bitmap> bitmapCache = new Cache<Bitmap>();
    
    public static void leaveLeft(Class<?> entering, Activity leaving) {
        Intent i = new Intent(leaving, entering);
            i.putExtra(SNAPSHOT, bitmapCache.put(UI.snapshot(leaving)));
        leaving.startActivity(i);
        
        leaving.overridePendingTransition(0, 0);
    }
    
    public static void enterRight(Activity activity) {
        ViewGroup root = (ViewGroup) activity.findViewById(android.R.id.content);

        Bitmap bgBitmap = bitmapCache.remove(
            activity.getIntent().getExtras().getInt(SNAPSHOT)
        );
        
        ViewGroup newRoot = (ViewGroup) activity.getLayoutInflater()
                                                .inflate(R.layout.desktop, null);
        
        final ViewGroup entering = UI.findView(newRoot, R.id.desktop_entering);
        final ImageView leaving  = UI.findView(newRoot, R.id.desktop_leaving);
        
        View actualContent = root.getChildAt(0);

        root.removeAllViews();
        
        entering.addView(actualContent);
        leaving .setImageBitmap(bgBitmap);
        
        root.addView(newRoot);
        
        TriggeredAnimation a = new TriggeredAnimation() {
            public void onTriggered(AnimatorSet a) {
                a.play(ObjectAnimator.ofFloat(entering, "x", -entering.getWidth(), 0))
                 .with(ObjectAnimator.ofFloat(leaving , "x", 0, entering.getWidth()));
            }
        };
        
        entering.startAnimation(a.forContext(activity));
    }
    
    public static void goBack(final Activity activity) {
        AnimatorSet a = new AnimatorSet();
        
        ViewGroup entering = UI.findView(activity, R.id.desktop_entering);
        ImageView leaving  = UI.findView(activity, R.id.desktop_leaving);
        
        a.play(ObjectAnimator.ofFloat(entering, "x", 0, -entering.getWidth()))
         .with(ObjectAnimator.ofFloat(leaving , "x", entering.getWidth(), 0));
        
        a.addListener(new AnimatorListener() {
            public void onAnimationStart (Animator a) {}
            public void onAnimationRepeat(Animator a) {}
            public void onAnimationCancel(Animator a) {}
            
            public void onAnimationEnd   (Animator a) {
                activity.finish();
                activity.overridePendingTransition(0, 0);
            }
        });
        
        a.start();
    }
}
