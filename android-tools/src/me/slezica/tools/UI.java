package me.slezica.tools;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.view.View;

public class UI {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface ViewId { int value(); }
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ViewListener { int value(); }
    
    @SuppressWarnings("unchecked")
    public static <T extends View> T findView(View v, int id) {
        return (T) v.findViewById(id);
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends View> T findView(Activity a, int id) {
        return (T) a.findViewById(id);
    }
    
    public static void findViews(Activity a) {
        findViews(a, getRoot(a));
    }
    
    public static void findViews(Object parent, View root) {
        try   { _findViews(parent, root); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
    
    static void _findViews(Object parent, View root) throws IllegalArgumentException, IllegalAccessException {
        for (Field f : parent.getClass().getDeclaredFields()) {
            ViewId viewId = f.getAnnotation(ViewId.class);
            
            if (viewId != null) {
                boolean wasAccessible = f.isAccessible();
                
                if (!wasAccessible) f.setAccessible(true);
                
                View view = root.findViewById(viewId.value());
                
                if   (view != null) f.set(parent, view);
                else throw new IllegalArgumentException(noViewFound(parent, f, viewId));
                
                if (!wasAccessible) f.setAccessible(false);
            }
        }
    }
    
    static String noViewFound(Object a, Field f, ViewId v) {
        return String.format("No view in found in %s for field %s and ID %d",
                             a.getClass().getName(), f.getName(), v.value());
    }
    
    public static void findListeners(Activity a) {
        findListeners(a, getRoot(a));
    }
    
    public static void findListeners(Object parent, View root) {
        try   { _findListeners(parent, root); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
    
    public static void _findListeners(Object parent, View root) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        for (Field field : parent.getClass().getDeclaredFields()) {
            ViewListener viewListener = field.getAnnotation(ViewListener.class);
            
            if (viewListener != null) {
                Field target = findViewIdFor(parent, viewListener.value());
                
                if   (target != null) setListener(parent, field, target);
                else throw new IllegalArgumentException();
            }
        }
    }
    
    static void setListener(Object parent, Field source, Field target) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        boolean fieldWasAccesible   = source.isAccessible(),
                targetWasAccessible = target.isAccessible();
        
        if (!fieldWasAccesible)   source.setAccessible(true);
        if (!targetWasAccessible) target.setAccessible(true);
        
        Method setter = findSetter(source, target);
        
        if (setter != null) setter.invoke(target.get(parent), source.get(parent));
        else throw new IllegalArgumentException(noSetterFound(source, target));
        
        if (!fieldWasAccesible)   source .setAccessible(false);
        if (!targetWasAccessible) target.setAccessible(false);
    }
    
    static Field findViewIdFor(Object parent, int targetId) throws NullPointerException {
        for (Field f : parent.getClass().getDeclaredFields())
        if  (viewIdOrZero(f) == targetId)
            return f;
        
        return null;
    }
    
    static Method findSetter(Field source, Field target) {
        for (Method method: target.getType().getMethods()) {
            
            if (method.getParameterTypes().length == 1
            &&  method.getParameterTypes()[0].equals(source.getType())
            &&  method.getName().startsWith("set"))
                return method;
            
        } return null;
    }
    
    
    static int viewIdOrZero(Field f) {
        ViewId  viewId = f.getAnnotation(ViewId.class);
        return (viewId != null) ? viewId.value() : 0;
    }
    
    static String noSetterFound(Field source, Field target) {
        return String.format("%s: No setter found in %s of type %s for %s of type %s",
            source.getDeclaringClass().getSimpleName(),
            source.getName(), source.getType().getSimpleName(),
            target.getName(), target.getType().getSimpleName()
        );
    }
    
    public static Bitmap snapshot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(
            view.getWidth(), view.getHeight(), Config.ARGB_8888
        );
        
        view.draw(new Canvas(bitmap));
        return bitmap;
    }
    
    public static Bitmap snapshot(Activity activity) {
        return snapshot(getRoot(activity));
    }
    
    
    public static View getRoot(Activity activity) {
        return activity.findViewById(android.R.id.content);
    }
    
}