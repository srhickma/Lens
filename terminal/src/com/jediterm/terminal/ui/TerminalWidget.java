package com.jediterm.terminal.ui;

import com.jediterm.terminal.TtyConnector;

/**
 * @author traff
 */
public interface TerminalWidget {
  TerminalSession createTerminalSession(TtyConnector ttyConnector);

  boolean canOpenSession();
}
