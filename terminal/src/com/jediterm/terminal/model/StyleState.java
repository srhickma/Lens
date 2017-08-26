package com.jediterm.terminal.model;

import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import org.jetbrains.annotations.NotNull;

public class StyleState {
  private TextStyle currentStyle = TextStyle.EMPTY;
  private TextStyle defaultStyle = TextStyle.EMPTY;
  
  private TextStyle mergedStyle = null;

  private StyleState() {
    this(TextStyle.EMPTY);
  }

  private StyleState(TextStyle textStyle) {
    currentStyle = textStyle;
  }

  public static StyleState defaultStyle(SettingsProvider settingsProvider){
      StyleState styleState = new StyleState();
      styleState.setDefaultStyle(settingsProvider.getDefaultStyle());
      return styleState;
  }

  public TextStyle getCurrent() {
    return TextStyle.getCanonicalStyle(getMergedStyle());
  }

  private static TextStyle merge(@NotNull TextStyle style, @NotNull TextStyle defaultStyle) {
    TextStyle newStyle = style.clone();
    if (newStyle.getBackground() == null && defaultStyle.getBackground() != null) {
      newStyle.setBackground(defaultStyle.getBackground());
    }
    if (newStyle.getForeground() == null && defaultStyle.getForeground() != null) {
      newStyle.setForeground(defaultStyle.getForeground());
    }
    
    return newStyle.readonlyCopy();
  }

  void reset() {
    currentStyle = defaultStyle.clone();
    mergedStyle = null;
  }

  public void set(StyleState styleState) {
    setCurrent(styleState.getCurrent());
  }

  private void setDefaultStyle(TextStyle defaultStyle) {
    this.defaultStyle = defaultStyle;
    mergedStyle = null;
  }

  public TerminalColor getBackground() {
    return getBackground(null);
  }

  public TerminalColor getBackground(TerminalColor color) {
    return color != null ? color : defaultStyle.getBackground();
  }

  public TerminalColor getForeground() {
    return getForeground(null);
  }

  public TerminalColor getForeground(TerminalColor color) {
    return color != null ? color : defaultStyle.getForeground();
  }

  public StyleState clone() {
    return new StyleState(currentStyle);
  }

  void setCurrent(TextStyle current) {
    currentStyle = current;
    mergedStyle = null;
  }

  private TextStyle getMergedStyle() {
    if (mergedStyle == null) {
      mergedStyle = merge(currentStyle, defaultStyle);
    }
    return mergedStyle;
  }
}
