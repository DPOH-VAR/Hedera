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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.dpohvar.hedera.HederaPlugin.pluginClassLoader;
import static ru.dpohvar.hedera.HederaPlugin.pluginHome;
import static ru.dpohvar.hedera.ResourceManager.copyResource;

public class Hedera {

    private static final Hedera instance = new Hedera();
    public static Hedera getInstance(){
        return instance;
    }

    private IvyLoader ivyLoader;
    private Logger logger = Bukkit.getLogger();
    private URL defaultIvySettingsXml;
    private Map<String, ResolveReport> resolveReports = new HashMap<>();

    private Hedera(){
        File configFile = new File(pluginHome, "config.yml");
        File ivysettingsFile = new File(pluginHome, "ivysettings.xml");
        copyResource("config.yml", configFile);
        copyResource("ivysettings.xml", new File(pluginHome, "ivysettings.xml"));
        try { defaultIvySettingsXml = ivysettingsFile.toURI().toURL();}
        catch (MalformedURLException ignored) { /* never happen */ }

        Map config = YamlReader.read(configFile);
        Map ivySettings = (Map) config.get("ivy");
        String ivyDownloadURL = (String) ivySettings.get("download-url");
        File ivyJarFile = new File((String) ivySettings.get("jar"));
        ivyLoader = new IvyLoader(pluginClassLoader, ivyDownloadURL, ivyJarFile);
    }

    public boolean hasReport(String pluginName){
        return resolveReports.containsKey(pluginName);
    }

    public ResolveReport getReport(String pluginName){
        return resolveReports.get(pluginName);
    }

    void checkPlugins(){
        for (File pluginFile : getAllPluginsFiles()) {
            try {
                checkPlugin(pluginFile);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Hedera: can not check dependencies: "+pluginFile,e);
            }
        }
    }

    void checkPlugin(File pluginFile) throws IOException, ParseException {
        // check plugin depends on Hedera
        URL pluginYmlResource = getResourceURL(pluginFile, "plugin.yml");
        Map pluginYml = YamlReader.read(pluginYmlResource);
        String pluginName = (String) pluginYml.get("name");
        Object depend = pluginYml.get("depend");
        Object softdepend = pluginYml.get("softdepend");

        boolean hasDependency = false;
        if (depend instanceof List && ((List)depend).contains(HederaPlugin.pluginName)) hasDependency = true;
        if (softdepend instanceof List && ((List)softdepend).contains(HederaPlugin.pluginName)) hasDependency = true;
        if (!hasDependency) return;

        // put null-report
        resolveReports.put(pluginName,null);

        // get ivy.xml and ivysettings.xml
        URL ivyXmlResource = getResourceURL(pluginFile,"ivy.xml");
        if (!checkURL(ivyXmlResource)) {
            logger.log(Level.WARNING, "Hedera: ivy.xml not found in " + pluginFile);
            return;
        }
        URL ivysettingsXmlResource = getResourceURL(pluginFile,"ivysettings.xml");
        if (!checkURL(ivysettingsXmlResource)) ivysettingsXmlResource = defaultIvySettingsXml;

        // resolve dependencies
        ResolveReport report = ivyLoader.load(ivysettingsXmlResource, ivyXmlResource);
        resolveReports.put(pluginName,report);
        if (report.hasError()) {
            List problems = report.getAllProblemMessages();
            String message = "Error on load dependencies "+ pluginFile.getName() +"\n" + problems;
            logger.log(Level.WARNING, message);
            throw new RuntimeException(message);
        }
        for (ArtifactDownloadReport r: report.getAllArtifactsReports()) {
            logger.log(Level.INFO, "load "+r.getLocalFile());
            JarLoader.load(pluginClassLoader, r.getLocalFile());
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
