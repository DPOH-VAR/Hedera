package ru.dpohvar.hedera;

import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.dpohvar.hedera.Plugin.pluginClassLoader;
import static ru.dpohvar.hedera.Plugin.pluginHome;
import static ru.dpohvar.hedera.ResourceManager.copyResource;

public class Hedera {

    private static final Hedera instance = new Hedera();
    public static Hedera getInstance(){
        return instance;
    }

    private IvyLoader ivyLoader;
    private Logger logger = Bukkit.getLogger();
    private URL defaultIvySettingsXml;

    Hedera(){
        File configFile = new File(pluginHome, "config.yml");
        File ivysettingsFile = new File(pluginHome, "ivysettings.xml");
        copyResource("config.yml", configFile);
        copyResource("ivy.xml", new File(pluginHome, "ivy.xml"));
        copyResource("ivysettings.xml", new File(pluginHome, "ivysettings.xml"));
        try { defaultIvySettingsXml = ivysettingsFile.toURI().toURL();}
        catch (MalformedURLException ignored) { /* never happen */ }

        Map config = YamlReader.read(configFile);
        Map ivySettings = (Map) config.get("ivy");
        String ivyDownloadURL = (String) ivySettings.get("download-url");
        File ivyJarFile = new File((String) ivySettings.get("jar"));
        ivyLoader = new IvyLoader(pluginClassLoader, ivyDownloadURL, ivyJarFile);
    }

    void checkPlugins(){
        for (File pluginFile : getAllPluginsFiles()) {
            try {
                checkPlugin(pluginFile);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Hedera: can not check dependencies: "+pluginFile, e);
            }
        }
    }

    void checkPlugin(File pluginFile) throws IOException, ParseException {
        // check plugin depends on Hedera
        URL pluginYmlResource = getResourceURL(pluginFile, "plugin.yml");
        Map pluginYml = YamlReader.read(pluginYmlResource);
        Object dependYml = pluginYml.get("depend");
        if (!(dependYml instanceof List)) return;
        if (!((List)dependYml).contains("Hedera")) return;

        // get ivy.xml and ivysettings.xml
        URL ivyXmlResource = getResourceURL(pluginFile,"ivy.xml");
        if (!checkURL(ivyXmlResource)) {
            logger.log(Level.WARNING, "Hedera: ivy.xml not found in "+pluginFile);
            return;
        }
        URL ivysettingsXmlResource = getResourceURL(pluginFile,"ivysettings.xml");
        if (!checkURL(ivysettingsXmlResource)) ivysettingsXmlResource = defaultIvySettingsXml;

        // resolve dependencies
        ResolveReport report = ivyLoader.load(ivysettingsXmlResource, ivyXmlResource);
        logger.log(Level.INFO, "Hedera: "+pluginFile.getName()+" report: "+report);
        for (ArtifactDownloadReport r: report.getAllArtifactsReports()) {
            JarLoader.load(pluginClassLoader, r.getLocalFile());
            logger.log(Level.INFO, "Hedera: "+pluginFile.getName()+" file: "+r.getLocalFile());
        }

    }

    private static List<File> getAllPluginsFiles(){
        List<File> result = new ArrayList<>();
        File[] plFiles = new File("plugins").listFiles();
        if (plFiles != null) for (File file : plFiles) {
            if (!file.getName().toLowerCase().endsWith(".jar")) continue;
            if (!file.isFile()) continue;
            result.add(file);
        }
        return result;
    }

    static URL getResourceURL(File file, String resource) {
        try { return new URL("jar:file:"+file+"!/"+resource);}
        catch (MalformedURLException e) {return null;}
    }

    static boolean checkURL(URL url){
        try (InputStream ignored = url.openStream()) {
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
