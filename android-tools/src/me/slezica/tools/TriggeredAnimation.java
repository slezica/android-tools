package me.slezica.tools;

import android.animation.AnimatorSet;
import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public abstract class TriggeredAnimation {

    /* This class becomes useful when an animation has to be configured
     * according to parameters not available at construction. This is typical
     * when attempting to animate things from onCreate(): layout data is not
     * yet available.
     * 
     * By using a dummy animation as trigger, we can ensure the true animation 
     * will run when all the required information is there.
     */
    
    public abstract void onTriggered(AnimatorSet a);
    
    public AnimatorSet onTriggered() {
        AnimatorSet a = new AnimatorSet();
        onTriggered(a);
        return a;
    }
    
    public Animation forContext(Context ctx) {
        Animation dummy = AnimationUtils.loadAnimation(ctx, R.anim.dummy);
        
        dummy.setAnimationListener(new AnimationListener() {
            public void onAnimationEnd   (Animation dummy) { onTriggered().start(); }
            public void onAnimationStart (Animation dummy) {}
            public void onAnimationRepeat(Animation dummy) {}
        });
        
        return dummy;
    }
}
