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

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.view.View;

public class UI {
    
    @SuppressWarnings("unchecked")
    public static <T extends View> T findView(View v, int id) {
        return (T) v.findViewById(id);
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends View> T findView(Activity a, int id) {
        return (T) a.findViewById(id);
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
        return activity.findViewById(android.R.id.content).getRootView();
    }
    
}