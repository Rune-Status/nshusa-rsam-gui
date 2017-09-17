package io.nshusa.util;

public final class OSUtils {

    private OSUtils() {

    }

    public enum OSType {
        LINUX,
        MAC,
        SOLARIS,
        UNKNOWN,
        WINDOWS;

        public boolean isLinux() {
            return this == LINUX || this == SOLARIS;
        }

        public boolean isMac() {
            return this == MAC;
        }

        public boolean isWindows() {
            return this == WINDOWS;
        }
    }


    public static OSType getOs() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return OSType.WINDOWS;
        } else if (os.contains("mac")) {
            return OSType.MAC;
        } else if (os.contains("solaris")) {
            return OSType.SOLARIS;
        } else if (os.contains("sunos")) {
            return OSType.SOLARIS;
        } else if (os.contains("linux")) {
            return OSType.LINUX;
        } else if (os.contains("unix")) {
            return OSType.LINUX;
        } else {
            return OSType.UNKNOWN;
        }
    }

}
