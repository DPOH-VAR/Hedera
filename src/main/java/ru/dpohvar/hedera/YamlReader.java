package ru.dpohvar.hedera;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;
import java.util.Map;

public class YamlReader {
    static Yaml yaml = new Yaml();

    static Map read(File file){
        try(
                InputStream is = new FileInputStream(file);
                Reader reader = new InputStreamReader(is,"UTF8")
        ) {
            return (Map) yaml.load(reader);
        } catch (Exception e) {
            throw new RuntimeException("Can not load yaml: "+file,e);
        }
    }

    static Map read(URL url){
        try(
                InputStream is = url.openStream();
                Reader reader = new InputStreamReader(is,"UTF8")
        ) {
            return (Map) yaml.load(reader);
        } catch (Exception e) {
            throw new RuntimeException("Can not load yaml: "+url,e);
        }
    }
}
