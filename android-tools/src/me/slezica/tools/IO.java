package me.slezica.tools;

import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class IO {
    public static String readInputStream(InputStream i) {
          return new Scanner(i).useDelimiter("\\A").next();
    }
    
    @Deprecated /* obviously do not use this method in production code */
    public static String justHttpGetMe(String url) {
        try   { return readInputStream(new URL(url).openStream()); } 
        catch (Exception e) { throw new RuntimeException(e); }
    }
    
    @Deprecated /* obviously do not use this method in production code */
    public static String justHttpGetMe(String url, int retries) {
        if (retries == 0) return justHttpGetMe(url);
        else try {
            return justHttpGetMe(url);
        } catch (Exception ex) { return justHttpGetMe(url, retries - 1); }
    }
}
