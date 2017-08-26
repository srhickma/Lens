package com.jediterm.terminal.ui;

import com.jediterm.terminal.RequestOrigin;

import java.awt.Dimension;

public interface TerminalPanelListener {
  void onPanelResize(Dimension pixelDimension, RequestOrigin origin);
}
