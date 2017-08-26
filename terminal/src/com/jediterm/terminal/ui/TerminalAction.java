package com.jediterm.terminal.ui;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * @author traff
 */
public class TerminalAction {
  private final String myName;
  private final KeyStroke[] myKeyStrokes;
  private final Predicate<KeyEvent> myRunnable;

  private Supplier<Boolean> myEnabledSupplier = null;
  private Integer myMnemonicKey = null;
  private boolean mySeparatorBefore = false;

  public TerminalAction(String name, KeyStroke[] keyStrokes, Predicate<KeyEvent> runnable) {
    myName = name;
    myKeyStrokes = keyStrokes;
    myRunnable = runnable;
  }

  public boolean matches(KeyEvent e) {
    for (KeyStroke ks : myKeyStrokes) {
      if (ks.equals(KeyStroke.getKeyStrokeForEvent(e))) {
        return true;
      }
    }
    return false;
  }

  public boolean perform(KeyEvent e) {
    if (myEnabledSupplier != null && !myEnabledSupplier.get()) {
      return false;
    }
    return myRunnable.apply(e);
  }

  public static boolean processEvent(TerminalActionProvider actionProvider, final KeyEvent e) {
    for (TerminalAction a : actionProvider.getActions()) {
      if (a.matches(e)) {
        return a.perform(e);
      }
    }

    if (actionProvider.getNextProvider() != null) {
      return processEvent(actionProvider.getNextProvider(), e);
    }

    return false;
  }

  public String getName() {
    return myName;
  }

  public TerminalAction withMnemonicKey(Integer key) {
    myMnemonicKey = key;
    return this;
  }
  
  public TerminalAction withEnabledSupplier(Supplier<Boolean> enabledSupplier) {
    myEnabledSupplier = enabledSupplier;
    return this;
  }

  public TerminalAction separatorBefore(boolean enabled) {
    mySeparatorBefore = enabled;
    return this;
  }

}
