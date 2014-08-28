package ru.dpohvar.hedera;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PipeUtil {

    static void pipe(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int len;
        while ( (len=input.read(buffer)) > 0 ) output.write(buffer, 0, len);
    }

}
