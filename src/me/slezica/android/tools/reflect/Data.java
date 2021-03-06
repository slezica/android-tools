package me.slezica.android.tools.reflect;
//package me.slezica.mithril;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.lang.reflect.Field;
//
//import android.content.ContentValues;
//import android.database.Cursor;
//import android.os.Bundle;
//
//public class Data {
//    
//    public static ContentValues toValues(Object obj) {
//        final ContentValues cv = new ContentValues();
//        
//        try {
//            for (Field f: obj.getClass().getFields()) {
//                String   n = f.getName();
//                Object   v = f.get(obj);
//                Class<?> t = f.getType();
//                
//                if (t == Long.class)    cv.put(n, (Long)    v); else
//                if (t == Float.class)   cv.put(n, (Float)   v); else
//                if (t == String.class)  cv.put(n, (String)  v); else
//                if (t == Integer.class) cv.put(n, (Integer) v);
//            }
//        } catch (Exception e) { throw new RuntimeException(e); }
//            
//        return cv;
//    }
//    
//    public Bundle toBundle() {
//        Bundle b = new Bundle();
//            b.putByteArray(this.getClass().getSimpleName(), this.toBytes());
//        return b;
//    }
//    
//    public T fromBundle(Class<T> cls, Bundle b) {
//        return fromBytes(b.getByteArray(this.getClass().getSimpleName()));
//    }
//
//    @SuppressWarnings("unchecked")
//    public T fromCursor(Cursor row) {
//        try {
//            for (Field f: this.getClass().getFields()) {
//                int    col = row.getColumnIndex(f.getName());
//                Class<?> t = f.getType();
//                
//                if (t == Long.class)    f.set(this, row.getLong  (col)); else
//                if (t == Float.class)   f.set(this, row.getFloat (col)); else
//                if (t == String.class)  f.set(this, row.getString(col)); else
//                if (t == Integer.class) f.set(this, row.getInt   (col));
//            }
//            
//            return (T) this;
//        } catch (Exception e) { throw new RuntimeException(e); }
//    }
//
//    public byte[] toBytes() {
//        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
//        
//        try   { new ObjectOutputStream(ostream).writeObject(this); }
//        catch (IOException e) { throw new RuntimeException(e); } 
//        
//        return ostream.toByteArray();
//    }
//    
//    @SuppressWarnings("unchecked")
//    public T fromBytes(byte[] bytes) {
//        ByteArrayInputStream istream = new ByteArrayInputStream(bytes);
//        
//        try   { return (T) new ObjectInputStream(istream).readObject(); }
//        catch (Exception e) { throw new RuntimeException(e); }
//    }
//    
//    @Override
//    public String toString() {
//        String str = "[" + this.getClass().getSimpleName() + " ";
//        
//        try {
//            for (Field f: this.getClass().getFields())
//                str += f.getName() + "=" + f.get(this) + ", ";
//            
//        } catch (Exception e) { throw new RuntimeException(e); }
//        
//        return str.substring(0, str.length() - 2) + "]";
//    }
//}
