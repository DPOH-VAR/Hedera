package ru.dpohvar.hedera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourceManager {

    public static void copyResource(String resource, File file){
        if (!file.isFile()) {
            file.getParentFile().mkdirs();
            try (
                    FileOutputStream fos = new FileOutputStream(file);
                    InputStream is = HederaPlugin.class.getClassLoader().getResourceAsStream(resource)
            ) {
                PipeUtil.pipe(is, fos, 0x100);
            } catch (IOException e) {
                throw new RuntimeException("Can not copy resource "+resource,e);
            }
        }
    }

}
