package com.konjex.lens.app.cores.terminal;

import com.jediterm.terminal.ProcessTtyConnector;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;

import java.nio.charset.Charset;

/**
 * Created by shane on 22/08/17.
 */
public class LensProcessTtyConnector extends ProcessTtyConnector {
  private PtyProcess myProcess;

  public LensProcessTtyConnector(PtyProcess process, Charset charset) {
    super(process, charset);

    myProcess = process;
  }

  @Override
  protected void resizeImmediately() {
    if (getPendingTermSize() != null && getPendingPixelSize() != null) {
      myProcess.setWinSize(
          new WinSize(getPendingTermSize().width, getPendingTermSize().height, getPendingPixelSize().width, getPendingPixelSize().height));
    }
  }

  @Override
  public boolean isConnected() {
    return myProcess.isRunning();
  }

  @Override
  public String getName() {
    return "Local";
  }
}
