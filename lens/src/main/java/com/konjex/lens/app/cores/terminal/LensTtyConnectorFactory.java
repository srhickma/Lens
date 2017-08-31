package com.konjex.lens.app.cores.terminal;

import com.google.common.collect.Maps;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.UIUtil;
import com.pty4j.PtyProcess;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by shane on 21/08/17.
 */
public class LensTtyConnectorFactory {

    public static TtyConnector create(){
        try{
            Map<String, String> envs = Maps.newHashMap(System.getenv());
            String[] command;

            if(UIUtil.isWindows){
                command = new String[]{"cmd.exe"};
            }
            else{
                command = new String[]{"/bin/bash", "--login"};
                envs.put("TERM", "xterm");
            }

            PtyProcess process = PtyProcess.exec(command, envs, null);
            return new LensProcessTtyConnector(process, Charset.forName("UTF-8"));
        }
        catch(Exception e){
            throw new IllegalStateException(e);
        }
    }

}
