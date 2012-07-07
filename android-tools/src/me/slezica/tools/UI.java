package me.slezica.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.View;

public class UI {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface ViewId {
        int value();
    }
    
    public interface ViewFinder {
        // Missing Go's implicit interfaces right now
        public View     findViewById(int id);
        public Class<?> getOriginalClass();
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ViewListener {
        String field()  default "";
        String setter() default "";
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends View> T findView(View v, int id) {
        return (T) v.findViewById(id);
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends View> T findView(Activity a, int id) {
        return (T) a.findViewById(id);
    }
    
    public static void findViews(Activity activity) {
        try {
            for (Field field : activity.getClass().getDeclaredFields()) {
                ViewId viewId = field.getAnnotation(ViewId.class);
                
                if (viewId != null) {
                    boolean wasAccessible = field.isAccessible();
                    
                    if (!wasAccessible)
                        field.setAccessible(true);
                    
                    View view = activity.findViewById(viewId.value());
                    
                    if (view != null)
                        field.set(activity, view);
                    
                    else {
                        throw new IllegalArgumentException(String.format(
                            "No view in found in %s for field %s and ID %d",
                            activity.getClass().getName(),
                            field.getName(),
                            viewId.value()
                        ));
                    }
                    
                    if (!wasAccessible)
                        field.setAccessible(false);
                }
            }
        
        } catch (Exception e) {
            throw new RuntimeException(e); // Avoid forced check
        }
    }
    
    // TODO abstract this. Try and find out what went wrong
    // with the ViewFinder strategy (threw ClassCastEx)
    public static void findViews(Dialog dialog) {
        try {
            for (Field field : dialog.getClass().getDeclaredFields()) {
                ViewId viewId = field.getAnnotation(ViewId.class);
                
                if (viewId != null) {
                    boolean wasAccessible = field.isAccessible();
                    
                    if (!wasAccessible)
                        field.setAccessible(true);
                    
                    View view = dialog.findViewById(viewId.value());
                    
                    if (view != null)
                        field.set(dialog, view);
                    
                    else {
                        throw new IllegalArgumentException(String.format(
                            "No view in found in %s for field %s and ID %d",
                            dialog.getClass().getName(),
                            field.getName(),
                            viewId.value()
                        ));
                    }
                    
                    if (!wasAccessible)
                        field.setAccessible(false);
                }
            }
        
        } catch (Exception e) {
            throw new RuntimeException(e); // Avoid forced check
        }
    }
    
    public static void findListeners(Activity activity) {
        try {
            Class<? extends Activity> cls = activity.getClass();
            
            for (Field field : cls.getDeclaredFields()) {
                ViewListener viewListener = field.getAnnotation(ViewListener.class);
                
                if (viewListener != null) {
                    String targetName       = viewListener.field(),
                           targetSetterName = viewListener.setter();
                    
                    if (targetName.equals("")) // Assume from naming
                        targetName = field.getName().split("_")[0];

                    Field target = (Field) cls.getDeclaredField(targetName);
                    
                    boolean fieldWasAccesible   = field .isAccessible(),
                            targetWasAccessible = target.isAccessible();
                    
                    
                    if (!fieldWasAccesible)
                        field .setAccessible(true);
                    
                    if (!targetWasAccessible)
                        target.setAccessible(true);
                    
                    if (targetSetterName.equals("")) {
                        // Look setter up given method signatures
                        for (Method method: target.getType().getMethods()) {
                            
                            if (method.getParameterTypes().length == 1                &&
                                method.getParameterTypes()[0].equals(field.getType()) &&
                                method.getName().startsWith("set")
                               ) {
                                
                                method.invoke(target.get(activity), field.get(activity));
                            }
                        }
                    
                    }
                    
                    if (!fieldWasAccesible)
                        field .setAccessible(false);
                    
                    if (!targetWasAccessible)
                        target.setAccessible(false);
                }
            }
        
        } catch (Exception e) {
            throw new RuntimeException(e); // Avoid forced check
        }
    }
    
    public static Bitmap snapshot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(
            view.getWidth(), view.getHeight(), Config.ARGB_8888
        );
        
        view.draw(new Canvas(bitmap));
        return bitmap;
    }
    
    public static Bitmap snapshot(Activity activity) {
        return snapshot(activity.findViewById(android.R.id.content));
    }
    
    public static byte[] bmpToBytes(Bitmap bmp) {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, ostream);
        return ostream.toByteArray();
    }
    
    public static Bitmap bytesToBmp(byte[] bytes) {
        return BitmapFactory.decodeStream(new ByteArrayInputStream(bytes));
    }
}

