package ru.dpohvar.hedera;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class JarLoader {

    static Method addURL, getURLs;
    static {
        try {
            addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
            getURLs = URLClassLoader.class.getDeclaredMethod("getURLs");
            getURLs.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Error on init ClassLoaderInjector",e);
        }
    }

    public static void load(ClassLoader classLoader, URL url) {
        try {
            URL[] urls = (URL[]) getURLs.invoke(classLoader);
            for (URL t: urls) if (url.equals(t)) return;
            addURL.invoke(classLoader, url);
        } catch (Exception e) {
            throw new RuntimeException("Error on load "+url,e);
        }
    }

    public static void load(ClassLoader classLoader, File file) {
        URL url;
        try {
            url = file.toURI().toURL();
        } catch (Exception e) {
            throw new RuntimeException("Invalid file: "+file,e);
        }
        load (classLoader, url);
    }
}
