/*
 * The MIT License
 * Copyright (c) 2011 Santiago Lezica (slezica89@gmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package me.slezica.android.tools.ui;

import me.slezica.android.tools.Cache;
import me.slezica.android.tools.R;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class ActivitySlider {
    static final String SNAPSHOT = "_desktop_snapshot";
    
    static final Cache<Bitmap> bitmapCache = new Cache<Bitmap>();
    
    static abstract class AnimatorFactory {
        final Context context;
        
        public AnimatorFactory(Context ctx) { this.context = ctx; }
        
        final Animator fabricate(final View entering, final View leaving) {
            AnimatorSet a = new AnimatorSet();
            fabricate(entering, leaving, a);
            return a;
        }
        
        abstract void fabricate(View entering, View leaving, AnimatorSet a);
    }
    
    static class PushFromLeft extends AnimatorFactory {
        public PushFromLeft(Context ctx) { super(ctx); }

        void fabricate(View entering, View leaving, AnimatorSet a) {
            a.play(ObjectAnimator.ofFloat(entering, "x", -entering.getWidth(), 0))
             .with(ObjectAnimator.ofFloat(leaving , "x", 0, entering.getWidth()));
        };
    };

    static class PullFromLeft extends AnimatorFactory {
        public PullFromLeft(Context ctx) { super(ctx); }

        void fabricate(View entering, View leaving, AnimatorSet a) {
            a.play(ObjectAnimator.ofFloat(entering, "x", 0, -entering.getWidth()))
             .with(ObjectAnimator.ofFloat(leaving , "x", entering.getWidth(), 0));
        };
    };

    static class PushFromRight extends AnimatorFactory {
        public PushFromRight(Context ctx) { super(ctx); }

        void fabricate(View entering, View leaving, AnimatorSet a) {
            float screenw = leaving.getWidth();
            
            a.play(ObjectAnimator.ofFloat(entering, "x", screenw, screenw - entering.getWidth()))
             .with(ObjectAnimator.ofFloat(leaving , "x", 0, -entering.getWidth()));
        };
    };    

    static class PullFromRight extends AnimatorFactory {
        public PullFromRight(Context ctx) { super(ctx); }

        void fabricate(View entering, View leaving, AnimatorSet a) {
            float screenw = leaving.getWidth();
            
            a.play(ObjectAnimator.ofFloat(entering, "x", screenw - entering.getWidth(), screenw))
             .with(ObjectAnimator.ofFloat(leaving , "x", -entering.getWidth(), 0));
        };
    };
    
    public static void leave(Activity leaving, Class<?> entering) {
        Intent i = new Intent(leaving, entering);
            i.putExtra(SNAPSHOT, takeSnapshot(leaving));
            
        leaving.startActivity(i);
        leaving.overridePendingTransition(0, 0);
    }
    
    static int takeSnapshot(Activity a) {
        return bitmapCache.put(UI.snapshot(a));
    }
    
    static Bitmap recoverSnapshot(Activity a) {
        return bitmapCache.remove(a.getIntent().getExtras().getInt(SNAPSHOT));
    }
    
    static void enter(Activity activity, final AnimatorFactory af) {
        ViewGroup root = (ViewGroup) activity.findViewById(android.R.id.content);
        View   content = root.getChildAt(0); 

        ViewGroup newRoot = (ViewGroup) activity.getLayoutInflater()
                                                .inflate(R.layout.sliding, null);
        
        root.removeAllViews();
        
        final ViewGroup entering = UI.findView(newRoot, R.id.desktop_entering);
                        entering.addView(content);
            
        final ImageView leaving = UI.findView(newRoot, R.id.desktop_leaving);
                        leaving.setImageBitmap(recoverSnapshot(activity));
                  
        root.addView(newRoot);
        
        Animation trigger = AnimationUtils.loadAnimation(activity, R.anim.dummy);
        
        trigger.setAnimationListener(new AnimationListener() {
            public void onAnimationStart (Animation animation) {}
            public void onAnimationRepeat(Animation animation) {}
            
            public void onAnimationEnd(Animation animation) {
                af.fabricate(entering, leaving).start();
            }
        });
        
        root.startAnimation(trigger);
    }
    
    public static void enterRight(Activity activity) {
        enter(activity, new PushFromRight(activity));
    }
    
    public static void enterLeft(Activity activity) {
        enter(activity, new PushFromLeft(activity));
    }
    
    public static void goBack(final Activity activity) {
        ViewGroup entering = UI.findView(activity, R.id.desktop_entering);
        ImageView leaving  = UI.findView(activity, R.id.desktop_leaving);
        
        final AnimatorFactory af = (entering.getX() > 0) ?
            new PullFromRight(activity) :
            new PullFromLeft(activity);
                
        Animator a = af.fabricate(entering, leaving);
        
        a.addListener(new AnimatorListener() {
            public void onAnimationStart (Animator animation) {}
            public void onAnimationRepeat(Animator animation) {}
            public void onAnimationCancel(Animator animation) {}
            
            public void onAnimationEnd(Animator animation) {
                activity.finish();
                activity.overridePendingTransition(0, 0);
            }
            
        });
        
        a.start();
    }
}
