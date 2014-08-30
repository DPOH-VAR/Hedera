package ru.dpohvar.hedera;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class HederaPlugin extends JavaPlugin implements Listener {

    public static final String pluginName = "Hedera";
    public static final File pluginHome = new File("plugins/"+pluginName);
    public static final ClassLoader pluginClassLoader = HederaPlugin.class.getClassLoader();
    public static final Hedera hedera = Hedera.getInstance();

    static { hedera.checkPlugins(); }
}
