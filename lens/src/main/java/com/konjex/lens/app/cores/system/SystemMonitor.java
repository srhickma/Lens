package com.konjex.lens.app.cores.system;

import org.hyperic.sigar.cmd.CpuInfo;

/**
 * Created by shane on 27/08/17.
 */
public class SystemMonitor {

    private static CpuInfo info = new CpuInfo();

    public static void getInfo(){
        System.out.println(info.getUsageShort());
    }

}
