package me.slezica.android.tools;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class Inject {
    @Retention(RUNTIME) @Target(FIELD)
    public static @interface Content { int value() default -1; }
    
    @Retention(RUNTIME) @Target(METHOD)
    public static @interface Listen {
        String   to();
        Class<?> as();
        String with() default "";
    }
    
    @Retention(RUNTIME) @Target(FIELD)
    public static @interface Resource { int value(); }
    
    @Retention(RUNTIME) @Target(FIELD)
    public static @interface SystemService { String value(); }
    
    static class InjectionException extends RuntimeException {
        public InjectionException(String detail)       { super(detail); }
        public InjectionException(Throwable throwable) { super(throwable); }
        
        public InjectionException(String detail, Throwable throwable) {
            super(detail, throwable);
        }
    }
    
    /* High-level wrappers: */
    public static void into(Activity a) throws InjectionException {
        into(a, a, UI.getRoot(a));
    }
    
    public static void into(Fragment f) throws InjectionException {
        into(f, f.getActivity(), f.getView());
    }
    
    public static void into(Object parent, Context ctx, View root) throws InjectionException {
        try   { _into(parent, ctx, root); }
        catch (Exception e) { throw new InjectionException(e); }
    }

    
    /* Low-level workers: */
    static void _into(Object parent, Context ctx, View root) throws InjectionException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException {
        for (Field f : parent.getClass().getFields()) {
            if (has(f, Content.class))  injectContent (parent, f, root); else
            if (has(f, Resource.class)) injectResource(parent, f, ctx);
        }
        
        for (Method m : parent.getClass().getMethods())
            if (has(m, Listen.class)) injectListener(parent, m);
    }
    
    static void injectContent(Object parent, Field f,  View root) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
        int id     = f.getAnnotation(Content.class).value();
        String tag = f.getName();
        
        View view = (id != -1) ? root.findViewById(id) :
                                 root.findViewWithTag(tag);
        
        inject(parent, f, view);
    }
    
    static void injectResource(Object parent, Field f, Context ctx) throws IllegalArgumentException, IllegalAccessException {
        inject(parent, f, findResource(f, ctx));
    }
    
    static void injectListener(Object parent, Method proxyTarget) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException {
        Listen l = proxyTarget.getAnnotation(Listen.class);
        
        Object   target  = parent.getClass().getField(l.to()).get(parent);
        Class<?> targetc = target.getClass();
        
        Method     targetm  = null;
        Class<?>[] targetmp = new Class<?>[] { l.as() };
        
        if (!l.with().isEmpty()) { /* We were told which setter to use */
            targetm = targetc.getMethod(l.with(), targetmp);
            
        } else {
            /* We need to find the setter ourselves */
            for (Method m: targetc.getMethods())
            if  (m.getParameterTypes().length == 1)
            if  (m.getParameterTypes()[0].equals(l.as())) {
                if (targetm == null) targetm = m;
                else throw ambiguousListener(proxyTarget, l);
            }
            
            if (targetm == null) throw noMethodFound(targetc, l.as());
        }

        targetm.invoke(target, makeListener(l.as(), parent, proxyTarget));
    }
    
    static void inject(Object instance, Field f, Object value) throws IllegalArgumentException, IllegalAccessException {
        if (value != null) f.set(instance, value);
        else throw noValueFound(f);        
    }
    
    static Object findResource(Field f, Context ctx) {
        Class<?>  t = f.getType();
        Resources r = ctx.getResources();
        int      id = f.getAnnotation(Resource.class).value();
        
        return  is(t, String.class)   ? r.getString(id)   :
                is(t, boolean.class)  ? r.getBoolean(id)  :
                is(t, Boolean.class)  ? r.getBoolean(id)  :
                is(t, int.class)      ? r.getInteger(id)  :
                is(t, Integer.class)  ? r.getInteger(id)  :
                is(t, Drawable.class) ? r.getDrawable(id) :
                    
                is(t, String[].class) ? r.getStringArray(id) :
                is(t, int[].class)    ? r.getIntArray(id)    :
                is(t, Integer[].class)? r.getIntArray(id)    :
                    
                is(t, Movie.class)    ? r.getMovie(id) :
                is(t, Animation.class)? AnimationUtils.loadAnimation(ctx, id) :
                    
                is(t, ColorStateList.class) ? r.getColorStateList(id) :
                
        null; /* Every test failed */
    }

    static boolean has(Field f, Class<? extends Annotation> annot) {
        return f.isAnnotationPresent(annot);
    }
    
    static boolean has(Method m, Class<? extends Annotation> annot) {
        return m.isAnnotationPresent(annot);
    }

    static boolean is(Class<?> that, Class<?> what) {
        return what.isAssignableFrom(that);
    }
    
    static InjectionException noValueFound(Field f) {
        return new InjectionException(String.format(
            "No value found in %s for field %s",
            f.getDeclaringClass().getSimpleName(),
            f.getName()
        ));
    }
    
    static InjectionException ambiguousListener(Method m, Listen l) {
        return new InjectionException(String.format(
            "Ambiguous setter for listener %s for %s in %s",
            m.getName(),
            l.to(),
            m.getDeclaringClass().getSimpleName()
        ));
    }
    
    static InjectionException noMethodFound(Class<?> where, Class<?> arg) {
        return new InjectionException("No method with parameter "
            + arg.getSimpleName() + " found in " + where.getSimpleName()
        );
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T makeListener(Class<T> cls, Object host, Method m) {
        return (T)
        Proxy.newProxyInstance(cls.getClassLoader(),
                               new Class<?>[] { cls },
                               new Forwarder(host, m));
    }
    
    static class Forwarder implements InvocationHandler {
        final Object i;
        final Method m;
        public Forwarder(Object i, Method m) { this.m = m; this.i = i; }

        @Override
        public Object invoke(Object proxy, Method proxym, Object[] args) throws Throwable {
            return this.m.invoke(this.i, args);
        }
    }
    
//    public static void findListeners(Activity a) {
//        findListeners(a, getRoot(a));
//    }
//    
//    public static void findListeners(Object parent, View root) {
//        try   { _findListeners(parent, root); }
//        catch (Exception e) { throw new RuntimeException(e); }
//    }
//    
//    public static void _findListeners(Object parent, View root) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
//        for (Field field : parent.getClass().getDeclaredFields()) {
//            Listener viewListener = field.getAnnotation(Listener.class);
//            
//            if (viewListener != null) {
//                Field target = findViewIdFor(parent, viewListener.value());
//                
//                if   (target != null) setListener(parent, field, target);
//                else throw new IllegalArgumentException();
//            }
//        }
//    }
//    
//    static void setListener(Object parent, Field source, Field target) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
//        boolean fieldWasAccesible   = source.isAccessible(),
//                targetWasAccessible = target.isAccessible();
//        
//        if (!fieldWasAccesible)   source.setAccessible(true);
//        if (!targetWasAccessible) target.setAccessible(true);
//        
//        Method setter = findSetter(source, target);
//        
//        if (setter != null) setter.invoke(target.get(parent), source.get(parent));
//        else throw new IllegalArgumentException(noSetterFound(source, target));
//        
//        if (!fieldWasAccesible)   source .setAccessible(false);
//        if (!targetWasAccessible) target.setAccessible(false);
//    }
//    
//    static Field findViewIdFor(Object parent, int targetId) throws NullPointerException {
//        for (Field f : parent.getClass().getDeclaredFields())
//        if  (viewIdOrZero(f) == targetId)
//            return f;
//        
//        return null;
//    }
//    
//    static Method findSetter(Field source, Field target) {
//        for (Method method: target.getType().getMethods()) {
//            
//            if (method.getParameterTypes().length == 1
//            &&  method.getParameterTypes()[0].equals(source.getType())
//            &&  method.getName().startsWith("set"))
//                return method;
//            
//        } return null;
//    }
//    
//    
//    static int viewIdOrZero(Field f) {
//        Layout  viewId = f.getAnnotation(Layout.class);
//        return (viewId != null) ? viewId.value() : 0;
//    }
//    
//    static String noSetterFound(Field source, Field target) {
//        return String.format("%s: No setter found in %s of type %s for %s of type %s",
//            source.getDeclaringClass().getSimpleName(),
//            source.getName(), source.getType().getSimpleName(),
//            target.getName(), target.getType().getSimpleName()
//        );
//    }
}
