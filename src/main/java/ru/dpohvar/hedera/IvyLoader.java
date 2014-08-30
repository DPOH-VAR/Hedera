package ru.dpohvar.hedera;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;

@SuppressWarnings("unchecked")
public class IvyLoader {

    Ivy ivy;
    ClassLoader classLoader;

    public IvyLoader(ClassLoader classLoader, String url, File targetFile){
        this.classLoader = classLoader;
        if (!targetFile.isFile()) {
            try (
                    InputStream input = URI.create(url).toURL().openStream();
                    FileOutputStream fos = new FileOutputStream(targetFile)
            ) {
                PipeUtil.pipe(input, fos, 0x1000);
            } catch (Exception e) {
                throw new RuntimeException("Can not download ivy: "+url,e);
            }
        }
        JarLoader.load(classLoader, targetFile);
        try {
            ivy = new Ivy();
        } catch (Throwable e) {
            throw new RuntimeException("Can not load Ivy",e);
        }
    }

    public ResolveReport load(URL settingsURL, URL ivySource) throws IOException, ParseException {
        IvySettings settings = new IvySettings();
        settings.load(settingsURL);
        ivy.setSettings(settings);
        ivy.bind();
        ResolveOptions resolveOptions = new ResolveOptions()
                .setConfs(new String[]{"default"})
                .setOutputReport(false)
                .setValidate(true);
        return ivy.resolve(ivySource,resolveOptions);
    }

    public ResolveReport load(URL settingsURL, ModuleRevisionId mrId) throws IOException, ParseException {
        IvySettings settings = new IvySettings();
        settings.load(settingsURL);
        ivy.setSettings(settings);
        ivy.bind();
        ResolveOptions resolveOptions = new ResolveOptions()
                .setConfs(new String[]{"default"})
                .setOutputReport(false)
                .setValidate(true);
        return ivy.resolve(mrId, resolveOptions, true);
    }

}
