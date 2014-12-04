package com.cic.quadsim.input;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Logger;

/**
 * Jinput requires either that its native drivers are in the current run directory, or that it is given an absolute
 * path to their location.
 *
 * Since I am using jMonkey, I am reliyng on the fact that it includes these jinput native drivers along with openGL
 * and openAL native drivers in lwjgl-natives-x.x.x.jar and I extract them into the current directory before any
 * call to jinput.
 *
 * @author Cic
 */
public class JinputDriverExtractor {

    private static final Logger log = Logger.getLogger(GamePad.class.getName());

    private static final String pathWindows = "native/windows";
    private static final String pathLinux = "native/linux";
    private static final String pathOSX = "native/macosx";

    private static final String filesWindows[] = new String[]{"jinput-dx8.dll", "jinput-raw.dll"};
    private static final String filesWindows64[] = new String[]{"jinput-dx8_64.dll", "jinput-raw_64.dll"};
    private static final String filesLinux[] = new String[]{"libjinput-linux.so"};
    private static final String filesLinux64[] = new String[]{"libjinput-linux64.so"};
    private static final String filesOSX[] = new String[]{"libjinput-osx.jnilib"};

    private static boolean doneExtracting = false;

    private static String getPrivilegedProperty(final String property) {
        return (String) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return System.getProperty(property);
            }
        });
    }

    private static String getPrivilegedProperty(final String property, final String default_value) {
        return (String) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return System.getProperty(property, default_value);
            }
        });
    }

    /**
     * Detects the os/architecture and extracts the appropriate drivers.
     */
    public static void extract() {
        // Ensure extracting is done only once.
        if (doneExtracting) {
            return;
        }

        String path = null;
        String[] files = null;

        String osName = getPrivilegedProperty("os.name", "").trim();
        if (osName.equals("Linux")) {
            path = pathLinux;
            if ("i386".equals(getPrivilegedProperty("os.arch"))) {
                files = filesLinux;
            } else {
                files = filesLinux64;
            }
        } else if (osName.equals("Mac OS X")) {
            path = pathOSX;
            files = filesOSX;
        } else if (osName.startsWith("Windows")) {
            path = pathWindows;
            if ("x86".equals(getPrivilegedProperty("os.arch"))) {
                files = filesWindows;
            } else {
                files = filesWindows64;
            }
        } else {
            log.warning("No known jinput native driver location for os: "+osName);
        }

        if (files != null) {
            for (String file : files) {
                extractNativeDriver(path, file);
            }
        }

        doneExtracting = true;
    }

    /**
     * Extracts single resource.
     * @param filePath
     * @param fileName
     */
    private static void extractNativeDriver(String filePath, String fileName) {
        String resourceName = filePath + "/" + fileName;
        log.fine("Extracting to current directory native jinput driver: " + resourceName);
        try {
            InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(resourceName);

            BufferedInputStream bis = new BufferedInputStream(is);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("./" + fileName));

            byte[] buf = new byte[512];
            int size;

            while ((size = bis.read(buf)) > -1) {
                bos.write(buf, 0, size);
            }
            bos.close();
        } catch (Exception e) {
            log.severe("Failed extracting to current directory native jinput driver: " + resourceName);
            e.printStackTrace();
        }
    }
}
