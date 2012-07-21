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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

public class ExecutorFragment extends Fragment implements Executor {

    protected Boolean mReady = false;
    protected List<Runnable> mPending = new LinkedList<Runnable>();

    public ExecutorFragment(Activity parent) {
        parent.getFragmentManager()
            .beginTransaction().add(getId(), this)
        .commit();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public synchronized void onDetach() {
        super.onDetach();
        mReady = false;
    }

    @Override
    public synchronized void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        setReady(true);
        while (mPending.size() > 0) execute(mPending.remove(0));
    }

    public synchronized boolean isReady() { return mReady;}
    protected synchronized void setReady(boolean ready) { mReady = ready; }

    public synchronized void execute(Runnable r) {
        if (isReady()) runNow(r); else mPending.add(r);
    }

    protected synchronized void runNow(Runnable runnable) {
        getActivity().runOnUiThread(runnable);
    }
}