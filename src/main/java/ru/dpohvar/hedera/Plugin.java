package ru.dpohvar.hedera;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Plugin extends JavaPlugin {

    public static final String pluginName = "Hedera";
    public static final File pluginHome = new File("plugins/"+pluginName);
    public static final ClassLoader pluginClassLoader = Plugin.class.getClassLoader();
    private static final Hedera hedera = new Hedera();

    static {
        hedera.checkPlugins();
    }

}
