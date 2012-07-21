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

package me.slezica.android.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

public abstract class DataObject<T extends DataObject<T>> implements Serializable {

    public ContentValues toValues() {
        final ContentValues cv = new ContentValues();
        
        try {
            for (Field f: this.getClass().getFields()) {
                String   n = f.getName();
                Object   v = f.get(this);
                Class<?> t = f.getType();
                
                if (t == Long.class)    cv.put(n, (Long)    v); else
                if (t == Float.class)   cv.put(n, (Float)   v); else
                if (t == String.class)  cv.put(n, (String)  v); else
                if (t == Integer.class) cv.put(n, (Integer) v);
            }
        } catch (Exception e) { throw new RuntimeException(e); }
            
        return cv;
    }
    
    public Intent asIntent(Context ctx, Class<?> target) {
        Intent i = new Intent(ctx, target);
            i.getExtras().putAll(toBundle());
        return i;
    }
    
    public Bundle toBundle() {
        Bundle b = new Bundle();
            b.putByteArray(this.getClass().getSimpleName(), this.toBytes());
        return b;
    }
    
    public T fromBundle(Bundle b) {
        return fromBytes(b.getByteArray(this.getClass().getSimpleName()));
    }

    @SuppressWarnings("unchecked")
    public T fromCursor(Cursor row) {
        try {
            for (Field f: this.getClass().getFields()) {
                int    col = row.getColumnIndex(f.getName());
                Class<?> t = f.getType();
                
                if (t == Long.class)    f.set(this, row.getLong  (col)); else
                if (t == Float.class)   f.set(this, row.getFloat (col)); else
                if (t == String.class)  f.set(this, row.getString(col)); else
                if (t == Integer.class) f.set(this, row.getInt   (col));
            }
            
            return (T) this;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public byte[] toBytes() {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        
        try   { new ObjectOutputStream(ostream).writeObject(this); }
        catch (IOException e) { throw new RuntimeException(e); } 
        
        return ostream.toByteArray();
    }
    
    @SuppressWarnings("unchecked")
    public T fromBytes(byte[] bytes) {
        ByteArrayInputStream istream = new ByteArrayInputStream(bytes);
        
        try   { return (T) new ObjectInputStream(istream).readObject(); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
    
    @Override
    public String toString() {
        String str = "[" + this.getClass().getSimpleName() + " ";
        
        try {
            for (Field f: this.getClass().getFields())
                str += f.getName() + "=" + f.get(this) + ", ";
            
        } catch (Exception e) { throw new RuntimeException(e); }
        
        return str.substring(0, str.length() - 2) + "]";
    }
    
    private static final long serialVersionUID = 2;
}
