package me.slezica.android.tools.dev;

import android.util.Log;
import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static android.util.Log.WARN;

public class Notes {

    public static Notes about(Object what) {
        return new Notes(what.getClass().getSimpleName());
    }
    
    public static Notes about(Class<?> what) {
        return new Notes(what.getSimpleName());
    }
    
    final String tag;
    Notes(String tag) { this.tag = tag; }
    
    int     level   = DEBUG;
    boolean enabled = true;
    
    
    public Notes enable () { enabled = true ; return this; }
    public Notes disable() { enabled = false; return this; }
    
    public Notes on(int level) {
        this.level = level;
        return this;
    }
    
    public Notes e(String msg) {
        if (logging(ERROR)) Log.e(tag, alter(msg));
        return this;
    }
    
    public Notes w(String msg) {
        if (logging(WARN)) Log.w(tag, alter(msg));
        return this;
    }
    
    public Notes i(String msg) {
        if (logging(INFO)) Log.i(tag, alter(msg));
        return this;
    }
    
    public Notes d(String msg) {
        if (logging(DEBUG)) Log.d(tag, alter(msg));
        return this;
    }
    
    public Notes v(String msg) {
        if (logging(VERBOSE)) Log.v(tag, alter(msg));
        return this;
    }
    
    protected boolean logging(int level) { 
        return enabled && level >= this.level;
    }
    
    /* This can be overridden by subclasses */
    protected String alter(String message) { return message; }
}
