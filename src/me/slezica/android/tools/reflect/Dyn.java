package me.slezica.android.tools.reflect;

import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("unchecked")
public class Dyn {

    public static <T> T dispatch(Object target, String mname, Object... args) {
        try   {
            return (T) safeDispatch(target, mname, args);
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }
    
    public static Object safeDispatch(Object target, String mname, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        return target.getClass().getMethod(mname, getClasses(args)).invoke(target, args);
    }
    
    static Class<?>[] getClasses(Object[] objects) {
        Class<?>[] classes = new Class<?>[objects.length];
        
        for (int i = 0; i < objects.length; i++)
            classes[i] = objects[i].getClass();
        
        return classes;
    }
}
