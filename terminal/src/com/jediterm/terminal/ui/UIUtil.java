package com.jediterm.terminal.ui;

import com.jediterm.terminal.util.Util;

/**
 * @author traff
 */
public class UIUtil {
  public static final String OS_NAME = System.getProperty("os.name");
  public static final String OS_VERSION = System.getProperty("os.version").toLowerCase();

  protected static final String _OS_NAME = OS_NAME.toLowerCase();
  public static final boolean isWindows = _OS_NAME.startsWith("windows");
  public static final boolean isOS2 = _OS_NAME.startsWith("os/2") || _OS_NAME.startsWith("os2");
  public static final boolean isMac = _OS_NAME.startsWith("mac");
  public static final boolean isLinux = _OS_NAME.startsWith("linux");
  public static final boolean isUnix = !isWindows && !isOS2;
  private static final boolean IS_ORACLE_JVM = isOracleJvm();

  public static final String JAVA_RUNTIME_VERSION = System.getProperty("java.runtime.version");

  private static boolean isOracleJvm(){
    final String vendor = getJavaVmVendor();
    return vendor != null && Util.containsIgnoreCase(vendor, "Oracle");
  }

  public static String getJavaVmVendor(){
    return System.getProperty("java.vm.vendor");
  }
}
