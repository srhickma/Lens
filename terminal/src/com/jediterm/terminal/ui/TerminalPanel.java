package com.jediterm.terminal.ui;

import com.google.common.base.Ascii;
import com.google.common.collect.Lists;
import com.jediterm.terminal.*;
import com.jediterm.terminal.TextStyle.Option;
import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.emulator.charset.CharacterSets;
import com.jediterm.terminal.emulator.mouse.MouseMode;
import com.jediterm.terminal.emulator.mouse.TerminalMouseListener;
import com.jediterm.terminal.model.*;
import com.jediterm.terminal.model.hyperlinks.LinkInfo;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import com.jediterm.terminal.util.CharUtils;
import com.jediterm.terminal.util.Pair;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.font.TextHitInfo;
import java.awt.im.InputMethodRequests;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TerminalPanel extends JComponent implements TerminalDisplay, ClipboardOwner, TerminalActionProvider {
  private static final Logger log = Logger.getLogger(TerminalPanel.class);
  private static final long serialVersionUID = -1048763516632093014L;

  private static final double SCROLL_SPEED = 0.05;

  /*font related*/
  private Font myNormalFont;
  private Font myItalicFont;
  private Font myBoldFont;
  private Font myBoldItalicFont;
  private int myDescent = 0;
  private Dimension myCharSize = new Dimension();
  private Dimension myTermSize = new Dimension(80, 24);

  private TerminalStarter myTerminalStarter = null;

  private MouseMode myMouseMode = MouseMode.MOUSE_REPORTING_NONE;
  private Point mySelectionStartPoint = null;
  private TerminalSelection mySelection = null;

  private Clipboard selectionClipboard;
  private Clipboard clipboard;

  private TerminalPanelListener myTerminalPanelListener;

  private SettingsProvider mySettingsProvider;
  final private TerminalTextBuffer myTerminalTextBuffer;

  final private StyleState myStyleState;

  /*scroll and cursor*/
  final private TerminalCursor myCursor = new TerminalCursor();

  //we scroll a window [0, terminal_height] in the range [-history_lines_count, terminal_height]
  private final BoundedRangeModel myBoundedRangeModel = new DefaultBoundedRangeModel(0, 80, 0, 80);

  private boolean scrollingEnabled = true;
  private int clientScrollOrigin;
  private KeyListener myKeyListener;

  private TerminalActionProvider myNextActionProvider;
  private String myInputMethodUncommittedChars;

  private Timer myRepaintTimer;
  private AtomicBoolean needScrollUpdate = new AtomicBoolean(true);
  private AtomicBoolean needRepaint = new AtomicBoolean(true);

  private int myMaxFPS = 50;
  private int myBlinkingPeriod = 500;
  private TerminalCoordinates myCoordsAccessor;

  private LinkInfo myHoveredHyperlink = null;

  private int myCursorType = Cursor.DEFAULT_CURSOR;

  public TerminalPanel(@NotNull SettingsProvider settingsProvider, @NotNull TerminalTextBuffer terminalTextBuffer, @NotNull StyleState styleState) {
    mySettingsProvider = settingsProvider;
    myTerminalTextBuffer = terminalTextBuffer;
    myStyleState = styleState;
    myTermSize.width = terminalTextBuffer.getWidth();
    myTermSize.height = terminalTextBuffer.getHeight();
    myMaxFPS = mySettingsProvider.maxRefreshRate();

    updateScrolling();

    enableEvents(AWTEvent.KEY_EVENT_MASK | AWTEvent.INPUT_METHOD_EVENT_MASK);
    enableInputMethods(true);

    terminalTextBuffer.addModelListener(this::repaint);
  }

  @Override
  public void repaint() {
    needRepaint.set(true);
  }

  private void doRepaint() {
    super.repaint();
  }

  @Deprecated
  protected void reinitFontAndResize() {
    initFont();

    sizeTerminalFromComponent();
  }

  private void initFont() {
    myNormalFont = createFont();
    myBoldFont = myNormalFont.deriveFont(Font.BOLD);
    myItalicFont = myNormalFont.deriveFont(Font.ITALIC);
    myBoldItalicFont = myBoldFont.deriveFont(Font.ITALIC);

    establishFontMetrics();
  }

  public void init() {
    initFont();

    setUpClipboard();

    setPreferredSize(new Dimension(getPixelWidth(), getPixelHeight()));

    setFocusable(true);
    enableInputMethods(true);
    setDoubleBuffered(true);

    setFocusTraversalKeysEnabled(false);

    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        handleHyperlinks(e.getPoint(), e.isControlDown());
      }

      @Override
      public void mouseDragged(final MouseEvent e) {
        if (!isLocalMouseAction(e)) {
          return;
        }

        final Point charCoords = panelToCharCoords(e.getPoint());

        if (mySelection == null) {
          // prevent unlikely case where drag started outside terminal panel
          if (mySelectionStartPoint == null) {
            mySelectionStartPoint = charCoords;
          }
          mySelection = new TerminalSelection(new Point(mySelectionStartPoint));
        }
        repaint();
        mySelection.updateEnd(charCoords);
        if (mySettingsProvider.copyOnSelect()) {
          handleCopyOnSelect();
        }

        if (e.getPoint().y < 0) {
          moveScrollBar((int) ((e.getPoint().y) * SCROLL_SPEED));
        }
        if (e.getPoint().y > getPixelHeight()) {
          moveScrollBar((int) ((e.getPoint().y - getPixelHeight()) * SCROLL_SPEED));
        }
      }
    });

    addMouseWheelListener(e -> {
      if (isLocalMouseAction(e)) {
        int notches = e.getWheelRotation();
        moveScrollBar(notches);
      }
    });

    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          if (e.getClickCount() == 1) {
            mySelectionStartPoint = panelToCharCoords(e.getPoint());
            mySelection = null;
            repaint();
          }
        }
      }

      @Override
      public void mouseReleased(final MouseEvent e) {
        requestFocusInWindow();
        repaint();
      }

      @Override
      public void mouseClicked(final MouseEvent e) {
        requestFocusInWindow();
        Runnable hyperlink = findHyperlink(e.getPoint());
        if (hyperlink != null && (myCursorType == Cursor.HAND_CURSOR)) {
          hyperlink.run();
        } else if (e.getButton() == MouseEvent.BUTTON1 && isLocalMouseAction(e)) {
          int count = e.getClickCount();
          if (count == 1) {
            // do nothing
          } else if (count == 2) {
            // select word
            final Point charCoords = panelToCharCoords(e.getPoint());
            Point start = SelectionUtil.getPreviousSeparator(charCoords, myTerminalTextBuffer);
            Point stop = SelectionUtil.getNextSeparator(charCoords, myTerminalTextBuffer);
            mySelection = new TerminalSelection(start);
            mySelection.updateEnd(stop);

            if (mySettingsProvider.copyOnSelect()) {
              handleCopyOnSelect();
            }
          } else if (count == 3) {
            // select line
            final Point charCoords = panelToCharCoords(e.getPoint());
            int startLine = charCoords.y;
            while (startLine > -getScrollBuffer().getLineCount()
                    && myTerminalTextBuffer.getLine(startLine - 1).isWrapped()) {
              startLine--;
            }
            int endLine = charCoords.y;
            while (endLine < myTerminalTextBuffer.getHeight()
                    && myTerminalTextBuffer.getLine(endLine).isWrapped()) {
              endLine++;
            }
            mySelection = new TerminalSelection(new Point(0, startLine));
            mySelection.updateEnd(new Point(myTermSize.width, endLine));

            if (mySettingsProvider.copyOnSelect()) {
              handleCopyOnSelect();
            }
          }
        } else if (e.getButton() == MouseEvent.BUTTON2 && mySettingsProvider.pasteOnMiddleMouseClick() && isLocalMouseAction(e)) {
          handlePasteSelection();
        }
        repaint();
      }
    });

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(final ComponentEvent e) {
        sizeTerminalFromComponent();
      }
    });

    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        myCursor.cursorChanged();
      }

      @Override
      public void focusLost(FocusEvent e) {
        myCursor.cursorChanged();

        handleHyperlinks(e.getComponent(), false);
      }
    });

    myBoundedRangeModel.addChangeListener(e -> {
      clientScrollOrigin = myBoundedRangeModel.getValue();
      repaint();
    });

    createRepaintTimer();
  }

  public void setTerminalPanelListener(final TerminalPanelListener resizeDelegate) {
    myTerminalPanelListener = resizeDelegate;
  }

  private void handleHyperlinks(Point p, boolean isControlDown) {
    HyperlinkStyle hyperlinkStyle = findHyperlink(p);
    if (hyperlinkStyle != null) {
      if (hyperlinkStyle.getHighlightMode() == HyperlinkStyle.HighlightMode.ALWAYS || (hyperlinkStyle.getHighlightMode() == HyperlinkStyle.HighlightMode.HOVER && isControlDown)) {
        updateCursor(Cursor.HAND_CURSOR);

        myHoveredHyperlink = hyperlinkStyle.getLinkInfo();
        return;
      }
    }

    myHoveredHyperlink = null;
    if (myCursorType != Cursor.DEFAULT_CURSOR) {
      updateCursor(Cursor.DEFAULT_CURSOR);
      repaint();
    }
  }

  private void handleHyperlinks(Component component, boolean controlDown) {
    PointerInfo a = MouseInfo.getPointerInfo();
    if (a != null) {
      Point b = a.getLocation();
      SwingUtilities.convertPointFromScreen(b, component);
      handleHyperlinks(b, controlDown);
    }
  }

  @Nullable
  private HyperlinkStyle findHyperlink(Point p) {
    p = panelToCharCoords(p);
    if (p.x >= 0 && p.y >= 0 && p.x < myTerminalTextBuffer.getWidth() && p.y <= myTerminalTextBuffer.getHeight()) {
      TextStyle style = myTerminalTextBuffer.getStyleAt(p.x, p.y);
      if (style instanceof HyperlinkStyle) {
        return (HyperlinkStyle) style;
      }
    }
    return null;
  }

  private void updateCursor(int cursorType) {
    if (cursorType != myCursorType) {
      myCursorType = cursorType;
      //noinspection MagicConstant
      setCursor(new Cursor(myCursorType));
    }
  }

  private void createRepaintTimer() {
    if (myRepaintTimer != null) {
      myRepaintTimer.stop();
    }
    myRepaintTimer = new Timer(1000 / myMaxFPS, new WeakRedrawTimer(this));
    myRepaintTimer.start();
  }

  private boolean isLocalMouseAction(MouseEvent e) {
    return mySettingsProvider.forceActionOnMouseReporting() || (isMouseReporting() == e.isShiftDown());
  }

  private boolean isRemoteMouseAction(MouseEvent e) {
    return isMouseReporting() && !e.isShiftDown();
  }

  private void setBlinkingPeriod(int blinkingPeriod) {
    myBlinkingPeriod = blinkingPeriod;
  }

  public void setCoordAccessor(TerminalCoordinates coordAccessor) {
    myCoordsAccessor = coordAccessor;
  }

  static class WeakRedrawTimer implements ActionListener {

    private WeakReference<TerminalPanel> ref;

    WeakRedrawTimer(TerminalPanel terminalPanel) {
      this.ref = new WeakReference<>(terminalPanel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      TerminalPanel terminalPanel = ref.get();
      if (terminalPanel != null) {
        terminalPanel.myCursor.changeStateIfNeeded();
        if (terminalPanel.needScrollUpdate.getAndSet(false)) {
          terminalPanel.updateScrolling();
        }
        if (terminalPanel.needRepaint.getAndSet(false)) {
          try {
            terminalPanel.doRepaint();
          } catch (Exception ex) {
            log.error("Error while terminal panel redraw", ex);
          }
        }
      } else { // terminalPanel was garbage collected
        Timer timer = (Timer) e.getSource();
        timer.removeActionListener(this);
        timer.stop();
      }
    }
  }

  @Override
  public void terminalMouseModeSet(MouseMode mode) {
    myMouseMode = mode;
  }

  private boolean isMouseReporting() {
    return myMouseMode != MouseMode.MOUSE_REPORTING_NONE;
  }

  private void scrollToBottom() {
    myBoundedRangeModel.setValue(myTermSize.height);
  }

  private void pageUp() {
    int val = myBoundedRangeModel.getValue() - myTermSize.height;
    myBoundedRangeModel.setValue(val >= myBoundedRangeModel.getMinimum() ? val : myBoundedRangeModel.getMinimum());
  }

  private void pageDown() {
    int val = myBoundedRangeModel.getValue() + myTermSize.height;
    myBoundedRangeModel.setValue(val <= myBoundedRangeModel.getMaximum() ? val : myBoundedRangeModel.getMaximum());
  }

  private void moveScrollBar(int k) {
    myBoundedRangeModel.setValue(myBoundedRangeModel.getValue() + k);
  }

  private Font createFont() {
    return mySettingsProvider.getTerminalFont();
  }

  private Point panelToCharCoords(final Point p) {
    int x = Math.min(p.x / myCharSize.width, getColumnCount() - 1);
    x = Math.max(0, x);
    int y = Math.min(p.y / myCharSize.height, getRowCount() - 1) + clientScrollOrigin;
    return new Point(x, y);
  }

  private void setUpClipboard() {
    selectionClipboard = Toolkit.getDefaultToolkit().getSystemSelection();
    if(selectionClipboard == null){
      selectionClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    }
    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
  }

  private void copySelection(final Point selectionStart, final Point selectionEnd, @NotNull Consumer<StringSelection> copyHandler) {
    if (selectionStart == null || selectionEnd == null) {
      return;
    }

    final String selectionText = SelectionUtil
            .getSelectionText(selectionStart, selectionEnd, myTerminalTextBuffer);

    if (selectionText.length() != 0) {
      try {
        copyHandler.accept(new StringSelection(selectionText));
      } catch (final IllegalStateException e) {
        log.error("Could not set clipboard:", e);
      }
    }
  }

  private void setCopyContents(StringSelection selection) {
    clipboard.setContents(selection, this);
  }

  private void setCopySelectionContents(StringSelection selection) {
    if (selectionClipboard != null) {
      selectionClipboard.setContents(selection, this);
    }
  }

  private void pasteFromClipboard(Supplier<String> clipboardStringSupplier) {
    String text = clipboardStringSupplier.get();

    if (text == null) {
      return;
    }

    try {
      // Sanitize clipboard text to use CR as the line separator.
      // See https://github.com/JetBrains/jediterm/issues/136.
      if (!UIUtil.isWindows) {
        // On Windows, Java automatically does this CRLF->LF sanitization, but
        // other terminals on Unix typically also do this sanitization, so
        // maybe JediTerm also should.
        text = text.replace("\r\n", "\n");
      }
      text = text.replace('\n', '\r');

      myTerminalStarter.sendString(text);
    } catch (RuntimeException e) {
      log.info(e);
    }
  }

  private String getClipboardString() {
    try {
      return getClipboardContent(clipboard);
    } catch (final Exception e) {
      log.info(e);
    }
    return null;
  }

  private String getSelectionClipboardString() {
    try {
      if (selectionClipboard != null) {
        return getClipboardContent(selectionClipboard);
      } else {
        return getClipboardContent(clipboard);
      }
    } catch (final Exception e) {
      log.info(e);
    }
    return null;
  }

  protected String getClipboardContent(Clipboard clipboard) throws IOException, UnsupportedFlavorException {
    try {
      return (String) clipboard.getData(DataFlavor.stringFlavor);
    } catch (Exception e) {
      log.info(e);
      return null;
    }
  }

  /* Do not care
   */
  public void lostOwnership(final Clipboard clipboard, final Transferable contents) {
  }

  protected BufferedImage createBufferedImage(int width, int height) {
    return new BufferedImage(width, height,
            BufferedImage.TYPE_INT_RGB);
  }

  private void sizeTerminalFromComponent() {
    if (myTerminalStarter != null) {
      final int newWidth = getWidth() / myCharSize.width;
      final int newHeight = getHeight() / myCharSize.height;

      if (newHeight > 0 && newWidth > 0) {
        final Dimension newSize = new Dimension(newWidth, newHeight);

        myTerminalStarter.postResize(newSize, RequestOrigin.User);
      }
    }
  }

  public void setTerminalStarter(final TerminalStarter terminalStarter) {
    myTerminalStarter = terminalStarter;
    sizeTerminalFromComponent();
  }

  public void setKeyListener(final KeyListener keyListener) {
    this.myKeyListener = keyListener;
  }

  public Dimension requestResize(final Dimension newSize,
                                 final RequestOrigin origin,
                                 int cursorY,
                                 JediTerminal.ResizeHandler resizeHandler) {
    if (!newSize.equals(myTermSize)) {
      myTerminalTextBuffer.lock();
      try {
        myTerminalTextBuffer.resize(newSize, origin, cursorY, resizeHandler, mySelection);
        myTermSize = (Dimension) newSize.clone();

        final Dimension pixelDimension = new Dimension(getPixelWidth(), getPixelHeight());

        setPreferredSize(pixelDimension);
        if (myTerminalPanelListener != null) {
          myTerminalPanelListener.onPanelResize(pixelDimension, origin);
        }
        SwingUtilities.invokeLater(this::updateScrolling);
      } finally {
        myTerminalTextBuffer.unlock();
      }
    }

    return new Dimension(getPixelWidth(), getPixelHeight());
  }

  private void establishFontMetrics() {
    final BufferedImage img = createBufferedImage(1, 1);
    final Graphics2D graphics = img.createGraphics();
    graphics.setFont(myNormalFont);

    final float lineSpace = mySettingsProvider.getLineSpace();
    final FontMetrics fo = graphics.getFontMetrics();

    myDescent = fo.getDescent();
    myCharSize.width = fo.charWidth('W');
    // The magic +2 here is to give lines a tiny bit of extra height to avoid clipping when rendering some Apple
    // emoji, which are slightly higher than the font metrics reported character height :(
    myCharSize.height = fo.getHeight() + (int) (lineSpace * 2) + 2;
    myDescent += lineSpace;

    if (!isMonospaced(fo)) {
      log.info("WARNING: Font " + myNormalFont.getName() + " is non-monospaced");
    }

    img.flush();
    graphics.dispose();
  }

  private static boolean isMonospaced(FontMetrics fontMetrics) {
    boolean isMonospaced = true;
    int charWidth = -1;
    for (int codePoint = 0; codePoint < 128; codePoint++) {
      if (Character.isValidCodePoint(codePoint)) {
        char character = (char) codePoint;
        if (isWordCharacter(character)) {
          int w = fontMetrics.charWidth(character);
          if (charWidth != -1) {
            if (w != charWidth) {
              isMonospaced = false;
              break;
            }
          } else {
            charWidth = w;
          }
        }
      }
    }
    return isMonospaced;
  }

  private static boolean isWordCharacter(char character) {
    return Character.isLetterOrDigit(character);
  }

  private void setupAntialiasing(Graphics graphics) {
    if (graphics instanceof Graphics2D) {
      Graphics2D myGfx = (Graphics2D) graphics;
      final Object mode = mySettingsProvider.useAntialiasing() ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON
              : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
      final RenderingHints hints = new RenderingHints(
              RenderingHints.KEY_TEXT_ANTIALIASING, mode);
      myGfx.setRenderingHints(hints);
    }
  }

  @Override
  public Color getBackground() {
    return getPalette().getColor(myStyleState.getBackground());
  }

  @Override
  public Color getForeground() {
    return getPalette().getColor(myStyleState.getForeground());
  }

  @Override
  public void paintComponent(final Graphics g) {
    final Graphics2D gfx = (Graphics2D) g;

    setupAntialiasing(gfx);

    gfx.setColor(getBackground());

    gfx.fillRect(0, 0, getWidth(), getHeight());

    myTerminalTextBuffer.processHistoryAndScreenLines(clientScrollOrigin, myTermSize.height, new StyledTextConsumer() {
      final int columnCount = getColumnCount();

      @Override
      public void consume(int x, int y, @NotNull TextStyle style, @NotNull CharBuffer characters, int startRow) {
        int row = y - startRow;
        drawCharacters(x, row, style, characters, gfx);

        if (mySelection != null) {
          Pair<Integer, Integer> interval = mySelection.intersect(x, row + clientScrollOrigin, characters.length());
          if (interval != null) {
            TextStyle selectionStyle = getSelectionStyle(style);
            CharBuffer selectionChars = characters.subBuffer(interval.first - x, interval.second);

            drawCharacters(interval.first, row, selectionStyle, selectionChars, gfx);
          }
        }
      }

      @Override
      public void consumeNul(int x, int y, int nulIndex, TextStyle style, CharBuffer characters, int startRow) {
        int row = y - startRow;
        if (mySelection != null) {
          // compute intersection with all NUL areas, non-breaking
          Pair<Integer, Integer> interval = mySelection.intersect(nulIndex, row + clientScrollOrigin, columnCount - nulIndex);
          if (interval != null) {
            TextStyle selectionStyle = getSelectionStyle(style);
            drawCharacters(x, row, selectionStyle, characters, gfx);
            return;
          }
        }
        drawCharacters(x, row, style, characters, gfx);
      }

      @Override
      public void consumeQueue(int x, int y, int nulIndex, int startRow) {
        if (x < columnCount) {
          consumeNul(x, y, nulIndex, TextStyle.EMPTY, new CharBuffer(CharUtils.EMPTY_CHAR, columnCount - x), startRow);
        }
      }
    });

    int cursorY = myCursor.getCoordY();
    if ((clientScrollOrigin + getRowCount() > cursorY) && !hasUncommittedChars()) {
      int cursorX = myCursor.getCoordX();
      Pair<Character, TextStyle> sc = myTerminalTextBuffer.getStyledCharAt(cursorX, cursorY);
      String cursorChar = "" + sc.first;
      if (Character.isHighSurrogate(sc.first)) {
        cursorChar += myTerminalTextBuffer.getStyledCharAt(cursorX+1, cursorY).first;
      }
      TextStyle normalStyle = sc.second != null ? sc.second : myStyleState.getCurrent();
      TextStyle selectionStyle = getSelectionStyle(normalStyle);
      boolean inSelection = inSelection(cursorX, cursorY);
      myCursor.drawCursor(cursorChar, gfx, inSelection ? selectionStyle : normalStyle);
    }

    drawInputMethodUncommitedChars(gfx);

    drawMargins(gfx, getWidth(), getHeight());
  }

  private TextStyle getSelectionStyle(TextStyle style) {
    TextStyle selectionStyle = style.clone();
    if (mySettingsProvider.useInverseSelectionColor()) {
      selectionStyle = getInversedStyle(style);
    } else {
      TextStyle mySelectionStyle = mySettingsProvider.getSelectionColor();
      selectionStyle.setBackground(mySelectionStyle.getBackground());
      selectionStyle.setForeground(mySelectionStyle.getForeground());
    }
    return selectionStyle;
  }

  private void drawInputMethodUncommitedChars(Graphics2D gfx) {
    if (hasUncommittedChars()) {
      int xCoord = (myCursor.getCoordX() + 1) * myCharSize.width;

      int y = myCursor.getCoordY() + 1;

      int yCoord = y * myCharSize.height - 3;

      int len = (myInputMethodUncommittedChars.length()) * myCharSize.width;

      gfx.setColor(getBackground());
      gfx.fillRect(xCoord, (y - 1) * myCharSize.height - 3, len, myCharSize.height);

      gfx.setColor(getForeground());
      gfx.setFont(myNormalFont);

      gfx.drawString(myInputMethodUncommittedChars, xCoord, yCoord);
      Stroke saved = gfx.getStroke();
      BasicStroke dotted = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{0, 2, 0, 2}, 0);
      gfx.setStroke(dotted);

      gfx.drawLine(xCoord, yCoord, xCoord + len, yCoord);
      gfx.setStroke(saved);
    }
  }

  private boolean hasUncommittedChars() {
    return myInputMethodUncommittedChars != null && myInputMethodUncommittedChars.length() > 0;
  }

  private boolean inSelection(int x, int y) {
    return mySelection != null && mySelection.contains(new Point(x, y));
  }

  @Override
  public void processKeyEvent(final KeyEvent e) {
    handleKeyEvent(e);

    handleHyperlinks(e.getComponent(), e.isControlDown());

    e.consume();
  }


  private void handleKeyEvent(KeyEvent e) {
    final int id = e.getID();
    if (id == KeyEvent.KEY_PRESSED) {
      myKeyListener.keyPressed(e);
    } else if (id == KeyEvent.KEY_RELEASED) {
      /* keyReleased(e); */
    } else if (id == KeyEvent.KEY_TYPED) {
      myKeyListener.keyTyped(e);
    }
  }

  private int getPixelWidth() {
    return myCharSize.width * myTermSize.width + getInsetX();
  }

  private int getPixelHeight() {
    return myCharSize.height * myTermSize.height;
  }

  public int getColumnCount() {
    return myTermSize.width;
  }

  public int getRowCount() {
    return myTermSize.height;
  }

  private int getInsetX() {
    return 4;
  }

  public void addTerminalMouseListener(final TerminalMouseListener listener) {
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (mySettingsProvider.enableMouseReporting() && isRemoteMouseAction(e)) {
          Point p = panelToCharCoords(e.getPoint());
          listener.mousePressed(p.x, p.y, e);
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (mySettingsProvider.enableMouseReporting() && isRemoteMouseAction(e)) {
          Point p = panelToCharCoords(e.getPoint());
          listener.mouseReleased(p.x, p.y, e);
        }
      }
    });

    addMouseWheelListener(e -> {
      if (mySettingsProvider.enableMouseReporting() && isRemoteMouseAction(e)) {
        mySelection = null;
        Point p = panelToCharCoords(e.getPoint());
        listener.mouseWheelMoved(p.x, p.y, e);
      }
    });

    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        if (mySettingsProvider.enableMouseReporting() && isRemoteMouseAction(e)) {
          Point p = panelToCharCoords(e.getPoint());
          listener.mouseMoved(p.x, p.y, e);
        }
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        if (mySettingsProvider.enableMouseReporting() && isRemoteMouseAction(e)) {
          Point p = panelToCharCoords(e.getPoint());
          listener.mouseDragged(p.x, p.y, e);
        }
      }
    });
  }

  public void initKeyHandler() {
    setKeyListener(new TerminalKeyHandler());
  }

  public enum TerminalCursorState {
    SHOWING, HIDDEN, NO_FOCUS
  }

  public class TerminalCursor {

    // cursor state
    private boolean myCursorIsShown; // blinking state
    Point myCursorCoordinates = new Point();

    // terminal modes
    private boolean myShouldDrawCursor = true;
    private boolean myBlinking = true;

    private long myLastCursorChange;
    private boolean myCursorHasChanged;

    public void setX(int x) {
      myCursorCoordinates.x = x;
      cursorChanged();
    }

    public void setY(int y) {
      myCursorCoordinates.y = y;
      cursorChanged();
    }

    int getCoordX() {
      return myCursorCoordinates.x;
    }

    int getCoordY() {
      return myCursorCoordinates.y - 1 - clientScrollOrigin;
    }

    void setShouldDrawCursor(boolean shouldDrawCursor) {
      myShouldDrawCursor = shouldDrawCursor;
    }

    void setBlinking(boolean blinking) {
      myBlinking = blinking;
    }

    boolean isBlinking() {
      return myBlinking && (getBlinkingPeriod() > 0);
    }

    void cursorChanged() {
      myCursorHasChanged = true;
      myLastCursorChange = System.currentTimeMillis();
      repaint();
    }

    private boolean cursorShouldChangeBlinkState(long currentTime) {
      return currentTime - myLastCursorChange > getBlinkingPeriod();
    }

    void changeStateIfNeeded() {
      if(!isFocusOwner()){
        return;
      }
      long currentTime = System.currentTimeMillis();
      if (cursorShouldChangeBlinkState(currentTime)) {
        myCursorIsShown = !myCursorIsShown;
        myLastCursorChange = currentTime;
        myCursorHasChanged = false;
        repaint();
      }
    }

    private TerminalCursorState computeBlinkingState() {
      if (!isBlinking() || myCursorHasChanged || myCursorIsShown) {
        return TerminalCursorState.SHOWING;
      }
      return TerminalCursorState.HIDDEN;
    }

    private TerminalCursorState computeCursorState() {
      if (!myShouldDrawCursor) {
        return TerminalCursorState.HIDDEN;
      }
      if (!isFocusOwner()) {
        return TerminalCursorState.NO_FOCUS;
      }
      return computeBlinkingState();
    }

    void drawCursor(String c, Graphics2D gfx, TextStyle style) {
      TerminalCursorState state = computeCursorState();

      // hidden: do nothing
      if(state != TerminalCursorState.HIDDEN){
        final int x = getCoordX();
        final int y = getCoordY();
        if (y >= 0 && y < myTermSize.height) {
          if (state == TerminalCursorState.SHOWING) {
            TextStyle styleToDraw = getInversedStyle(style);
            drawCharacters(x, y, styleToDraw, new CharBuffer(c), gfx);
          } else if (state == TerminalCursorState.NO_FOCUS) {
            int xCoord = x * myCharSize.width;
            int yCoord = y * myCharSize.height;
            gfx.setColor(getPalette().getColor(myStyleState.getForeground(style.getForegroundForRun())));
            gfx.drawRect(xCoord, yCoord, c.length()*myCharSize.width - 1, myCharSize.height - 1);
          }
        }
      }
    }
  }

  private int getBlinkingPeriod() {
    if (myBlinkingPeriod != mySettingsProvider.caretBlinkingMs()) {
      setBlinkingPeriod(mySettingsProvider.caretBlinkingMs());
    }
    return myBlinkingPeriod;
  }

  private TextStyle getInversedStyle(TextStyle style) {
    TextStyle selectionStyle;
    selectionStyle = style.clone();
    selectionStyle.setOption(Option.INVERSE, !selectionStyle.hasOption(Option.INVERSE));
    if (selectionStyle.getForeground() == null) {
      selectionStyle.setForeground(myStyleState.getForeground());
    }
    if (selectionStyle.getBackground() == null) {
      selectionStyle.setBackground(myStyleState.getBackground());
    }
    return selectionStyle;
  }

  private void drawCharacters(int x, int y, TextStyle style, CharBuffer buf, Graphics2D gfx) {
    int xCoord = x * myCharSize.width + getInsetX();
    int yCoord = y * myCharSize.height;

    if (xCoord < 0 || xCoord > getWidth() || yCoord < 0 || yCoord > getHeight()) {
      return;
    }

    int textLength = CharUtils.getTextLengthDoubleWidthAware(buf.getBuf(), buf.getStart(), buf.length(), mySettingsProvider.ambiguousCharsAreDoubleWidth());
    int height = Math.min(myCharSize.height, getHeight() - yCoord);
    int width = Math.min(textLength * TerminalPanel.this.myCharSize.width, TerminalPanel.this.getWidth() - xCoord);

    if (style instanceof HyperlinkStyle) {
      HyperlinkStyle hyperlinkStyle = (HyperlinkStyle) style;

      if (hyperlinkStyle.getHighlightMode() == HyperlinkStyle.HighlightMode.ALWAYS || (isHoveredHyperlink(hyperlinkStyle) && hyperlinkStyle.getHighlightMode() == HyperlinkStyle.HighlightMode.HOVER)) {

        // substitute text style with the hyperlink highlight style if applicable
        style = hyperlinkStyle.getHighlightStyle();
      }
    }

    Color backgroundColor = getPalette().getColor(myStyleState.getBackground(style.getBackgroundForRun()));

    gfx.setColor(backgroundColor);


    gfx.fillRect(xCoord,
            yCoord,
            width,
            height);

    if (buf.isNul()) {
      return; // nothing more to do
    }

    drawChars(x, y, buf, style, gfx);

    gfx.setColor(getPalette().getColor(myStyleState.getForeground(style.getForegroundForRun())));

    int baseLine = (y + 1) * myCharSize.height - myDescent;

    if (style.hasOption(TextStyle.Option.UNDERLINED)) {
      gfx.drawLine(xCoord, baseLine + 3, (x + textLength) * myCharSize.width + getInsetX(), baseLine + 3);
    }
  }

  private boolean isHoveredHyperlink(@NotNull HyperlinkStyle link) {
    return myHoveredHyperlink == link.getLinkInfo();
  }

  /**
   * Draw every char in separate terminal cell to guaranty equal width for different lines.
   * Nevertheless to improve kerning we draw word characters as one block for monospaced fonts.
   */
  private void drawChars(int x, int y, CharBuffer buf, TextStyle style, Graphics2D gfx) {
    int blockLen = 1;
    int offset = 0;
    int drawCharsOffset = 0;

    // workaround to fix Swing bad rendering of bold special chars on Linux
    // TODO required for italic?
    CharBuffer renderingBuffer;
    if (mySettingsProvider.DECCompatibilityMode() && style.hasOption(TextStyle.Option.BOLD)) {
      renderingBuffer = CharUtils.heavyDecCompatibleBuffer(buf);
    } else {
      renderingBuffer = buf;
    }

    while (offset + blockLen <= buf.length()) {
      if (renderingBuffer.getBuf()[buf.getStart() + offset] == CharUtils.DWC) {
        offset += blockLen;
        drawCharsOffset += blockLen;
        continue; // dont' draw second part(fake one) of double width character
      }

      Font font = getFontToDisplay(buf.charAt(offset + blockLen - 1), style);

      if (offset + 2 <= buf.length() && Character.isSurrogatePair(renderingBuffer.getBuf()[buf.getStart() + offset], renderingBuffer.getBuf()[buf.getStart() + offset + 1])) {
        blockLen = 2;
      }


      gfx.setFont(font);

      int descent = gfx.getFontMetrics(font).getDescent();
      int baseLine = (y + 1) * myCharSize.height - descent;
      int xCoord = (x + drawCharsOffset) * myCharSize.width + getInsetX();
      int yCoord = y * myCharSize.height;

      gfx.setClip(xCoord,
              yCoord,
              getWidth() - xCoord,
              getHeight() - yCoord);

      gfx.setColor(getPalette().getColor(myStyleState.getForeground(style.getForegroundForRun())));

      gfx.drawChars(renderingBuffer.getBuf(), buf.getStart() + offset, blockLen, xCoord, baseLine);

      drawCharsOffset += blockLen;
      offset += blockLen;
      blockLen = 1;
    }
    gfx.setClip(null);
  }

  private Font getFontToDisplay(char c, TextStyle style) {
    boolean bold = style.hasOption(TextStyle.Option.BOLD);
    boolean italic = style.hasOption(TextStyle.Option.ITALIC);
    // workaround to fix Swing bad rendering of bold special chars on Linux
    if (bold && mySettingsProvider.DECCompatibilityMode() && CharacterSets.isDecBoxChar(c)) {
      return myNormalFont;
    }
    return bold ? (italic ? myBoldItalicFont : myBoldFont)
            : (italic ? myItalicFont : myNormalFont);
  }

  private ColorPalette getPalette() {
    return mySettingsProvider.getTerminalColorPalette();
  }

  private void drawMargins(Graphics2D gfx, int width, int height) {
    gfx.setColor(getBackground());
    gfx.fillRect(0, height, getWidth(), getHeight() - height);
    gfx.fillRect(width, 0, getWidth() - width, getHeight());
  }

  public void scrollArea(final int scrollRegionTop, final int scrollRegionSize, int dy) {
    if (dy < 0) {
      needScrollUpdate.set(true);
    }
    mySelection = null;
  }

  private void updateScrolling() {
    if (scrollingEnabled) {
      myBoundedRangeModel
              .setRangeProperties(0, myTermSize.height, -myTerminalTextBuffer.getHistoryBuffer().getLineCount(), myTermSize.height, false);
    } else {
      myBoundedRangeModel.setRangeProperties(0, myTermSize.height, 0, myTermSize.height, false);
    }
  }

  public void setCursor(final int x, final int y) {
    myCursor.setX(x);
    myCursor.setY(y);
  }

  public void beep() {
    if (mySettingsProvider.audibleBell()) {
      Toolkit.getDefaultToolkit().beep();
    }
  }

  public TerminalTextBuffer getTerminalTextBuffer() {
    return myTerminalTextBuffer;
  }

  public TerminalSelection getSelection() {
    return mySelection;
  }

  @Override
  public boolean ambiguousCharsAreDoubleWidth() {
    return mySettingsProvider.ambiguousCharsAreDoubleWidth();
  }

  private LinesBuffer getScrollBuffer() {
    return myTerminalTextBuffer.getHistoryBuffer();
  }

  @Override
  public void setCursorVisible(boolean shouldDrawCursor) {
    myCursor.setShouldDrawCursor(shouldDrawCursor);
  }

  public void setScrollingEnabled(boolean scrollingEnabled) {
    this.scrollingEnabled = scrollingEnabled;

    SwingUtilities.invokeLater(this::updateScrolling);
  }

  @Override
  public void setBlinkingCursor(boolean enabled) {
    myCursor.setBlinking(enabled);
  }

  @Override
  public List<TerminalAction> getActions() {
    return Lists.newArrayList(
            new TerminalAction("Open as URL", new KeyStroke[0], input ->
                    openSelectionAsURL()).withEnabledSupplier(this::selectionTextIsUrl),
            new TerminalAction("Copy", mySettingsProvider.getCopyKeyStrokes(), input ->
                    handleCopy()).withMnemonicKey(KeyEvent.VK_C).withEnabledSupplier(() -> mySelection != null),
            new TerminalAction("Paste", mySettingsProvider.getPasteKeyStrokes(), input -> {
              handlePaste();
              return true;
            }).withMnemonicKey(KeyEvent.VK_P).withEnabledSupplier(() -> getClipboardString() != null),
            new TerminalAction("Clear Buffer", mySettingsProvider.getClearBufferKeyStrokes(), input -> {
              clearBuffer();
              return true;
            }).withMnemonicKey(KeyEvent.VK_K).withEnabledSupplier(() -> !myTerminalTextBuffer.isUsingAlternateBuffer()).separatorBefore(true),
            new TerminalAction("Page Up", mySettingsProvider.getPageUpKeyStrokes(), input -> {
              pageUp();
              return true;
            }).withEnabledSupplier(() -> !myTerminalTextBuffer.isUsingAlternateBuffer()).separatorBefore(true),
            new TerminalAction("Page Down", mySettingsProvider.getPageDownKeyStrokes(), input -> {
              pageDown();
              return true;
            }).withEnabledSupplier(() -> !myTerminalTextBuffer.isUsingAlternateBuffer())

    );
  }

  @NotNull
  private Boolean selectionTextIsUrl() {
    String selectionText = getSelectionText();
    if (selectionText != null) {
      try {
        URI uri = new URI(selectionText);
        //noinspection ResultOfMethodCallIgnored
        uri.toURL();
        return true;
      } catch (Exception e) {
        //pass
      }
    }
    return false;
  }

  @Nullable
  private String getSelectionText() {
    if (mySelection != null) {
      Pair<Point, Point> points = mySelection.pointsForRun(myTermSize.width);

      if (points.first != null || points.second != null) {
        return SelectionUtil
                .getSelectionText(points.first, points.second, myTerminalTextBuffer);

      }
    }

    return null;
  }

  protected boolean openSelectionAsURL() {
    if (Desktop.isDesktopSupported()) {
      try {
        String selectionText = getSelectionText();

        if (selectionText != null) {
          Desktop.getDesktop().browse(new URI(selectionText));
        }
      } catch (Exception e) {
        //ok then
      }
    }
    return false;
  }

  private void clearBuffer() {
    if (!myTerminalTextBuffer.isUsingAlternateBuffer()) {
      myTerminalTextBuffer.clearHistory();

      if (myCoordsAccessor != null && myCoordsAccessor.getY() > 0) {
        TerminalLine line = myTerminalTextBuffer.getLine(myCoordsAccessor.getY() - 1);

        myTerminalTextBuffer.clearAll();

        myCoordsAccessor.setY(0);
        myCursor.setY(1);
        myTerminalTextBuffer.addLine(line);
      }

      updateScrolling();

      clientScrollOrigin = myBoundedRangeModel.getValue();
    }
  }

  @Override
  public TerminalActionProvider getNextProvider() {
    return myNextActionProvider;
  }

  @Override
  public void setNextProvider(TerminalActionProvider provider) {
    myNextActionProvider = provider;
  }

  private void processTerminalKeyPressed(KeyEvent e) {
    if (hasUncommittedChars()) {
      return;
    }

    try {
      final int keycode = e.getKeyCode();
      final char keychar = e.getKeyChar();

      // numLock does not change the code sent by keypad VK_DELETE
      // although it send the char '.'
      if (keycode == KeyEvent.VK_DELETE && keychar == '.') {
        myTerminalStarter.sendBytes(new byte[]{'.'});
        return;
      }
      // CTRL + Space is not handled in KeyEvent; handle it manually
      else if (keychar == ' ' && (e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
        myTerminalStarter.sendBytes(new byte[]{Ascii.NUL});
        return;
      }

      final byte[] code = myTerminalStarter.getCode(keycode, e.getModifiers());
      if (code != null) {
        myTerminalStarter.sendBytes(code);
        if (mySettingsProvider.scrollToBottomOnTyping() && isCodeThatScrolls(keycode)) {
          scrollToBottom();
        }
      } else if (Character.isISOControl(keychar)) { // keys filtered out here will be processed in processTerminalKeyTyped
        processCharacter(keychar, e.getModifiers());
      }
    } catch (final Exception ex) {
      log.error("Error sending pressed key to emulator", ex);
    }
  }

  private void processCharacter(char keychar, int modifiers) {
    final char[] obuffer;
    if (mySettingsProvider.altSendsEscape() && (modifiers & InputEvent.ALT_MASK) != 0) {
      obuffer = new char[]{Ascii.ESC, keychar};
    } else {
      obuffer = new char[]{keychar};
    }

    if (keychar == '`' && (modifiers & InputEvent.META_MASK) != 0) {
      // Command + backtick is a short-cut on Mac OSX, so we shouldn't type anything
      return;
    }

    myTerminalStarter.sendString(new String(obuffer));

    if (mySettingsProvider.scrollToBottomOnTyping()) {
      scrollToBottom();
    }
  }

  private static boolean isCodeThatScrolls(int keycode) {
    return keycode == KeyEvent.VK_UP
            || keycode == KeyEvent.VK_DOWN
            || keycode == KeyEvent.VK_LEFT
            || keycode == KeyEvent.VK_RIGHT
            || keycode == KeyEvent.VK_BACK_SPACE
            || keycode == KeyEvent.VK_DELETE;
  }

  private void processTerminalKeyTyped(KeyEvent e) {
    if (hasUncommittedChars()) {
      return;
    }

    final char keychar = e.getKeyChar();
    if (!Character.isISOControl(keychar)) { // keys filtered out here will be processed in processTerminalKeyPressed
      try {
        processCharacter(keychar, e.getModifiers());
      } catch (final Exception ex) {
        log.error("Error sending typed key to emulator", ex);
      }
    }
  }

  public class TerminalKeyHandler implements KeyListener {

    public TerminalKeyHandler() {
    }

    public void keyPressed(final KeyEvent e) {
      if (!TerminalAction.processEvent(TerminalPanel.this, e)) {
        processTerminalKeyPressed(e);
      }
    }

    public void keyTyped(final KeyEvent e) {
      processTerminalKeyTyped(e);
    }

    //Ignore releases
    public void keyReleased(KeyEvent e) {
    }
  }

  private void handlePaste() {
    pasteFromClipboard(this::getClipboardString);
  }

  private void handlePasteSelection() {
    pasteFromClipboard(this::getSelectionClipboardString);
  }

  // "unselect" is needed to handle Ctrl+C copy shortcut collision with ^C signal shortcut
  private boolean handleCopy(boolean unselect, Consumer<StringSelection> copyHandler) {
    if (mySelection != null) {
      Pair<Point, Point> points = mySelection.pointsForRun(myTermSize.width);
      copySelection(points.first, points.second, copyHandler);
      if (unselect) {
        mySelection = null;
        repaint();
      }
      return true;
    }
    return false;
  }

  private boolean handleCopy() {
    return handleCopy(true, this::setCopyContents);
  }

  private boolean handleCopyOnSelect() {
    return handleCopy(false, this::setCopySelectionContents);
  }


  /**
   * InputMethod implementation
   * For details read http://docs.oracle.com/javase/7/docs/technotes/guides/imf/api-tutorial.html
   */
  @Override
  protected void processInputMethodEvent(InputMethodEvent e) {
    int commitCount = e.getCommittedCharacterCount();

    if (commitCount > 0) {
      myInputMethodUncommittedChars = null;
      AttributedCharacterIterator text = e.getText();
      if (text != null) {
        StringBuilder sb = new StringBuilder();

        //noinspection ForLoopThatDoesntUseLoopVariable
        for (char c = text.first(); commitCount > 0; c = text.next(), commitCount--) {
          if (c >= 0x20 && c != 0x7F) { // Hack just like in javax.swing.text.DefaultEditorKit.DefaultKeyTypedAction
            sb.append(c);
          }
        }

        if (sb.length() > 0) {
          myTerminalStarter.sendString(sb.toString());
        }
      }
    } else {
      myInputMethodUncommittedChars = uncommittedChars(e.getText());
    }
  }

  private static String uncommittedChars(@Nullable AttributedCharacterIterator text) {
    if (text == null) {
      return null;
    }

    StringBuilder sb = new StringBuilder();

    for (char c = text.first(); c != CharacterIterator.DONE; c = text.next()) {
      if (c >= 0x20 && c != 0x7F) { // Hack just like in javax.swing.text.DefaultEditorKit.DefaultKeyTypedAction
        sb.append(c);
      }
    }

    return sb.toString();
  }

  @Override
  public InputMethodRequests getInputMethodRequests() {
    return new MyInputMethodRequests();
  }

  private class MyInputMethodRequests implements InputMethodRequests {
    @Override
    public Rectangle getTextLocation(TextHitInfo offset) {
      Rectangle r = new Rectangle(myCursor.getCoordX() * myCharSize.width, (myCursor.getCoordY() + 1) * myCharSize.height,
              0, 0);
      Point p = TerminalPanel.this.getLocationOnScreen();
      r.translate(p.x, p.y);
      return r;
    }

    @Nullable
    @Override
    public TextHitInfo getLocationOffset(int x, int y) {
      return null;
    }

    @Override
    public int getInsertPositionOffset() {
      return 0;
    }

    @Override
    public AttributedCharacterIterator getCommittedText(int beginIndex, int endIndex, AttributedCharacterIterator.Attribute[] attributes) {
      return null;
    }

    @Override
    public int getCommittedTextLength() {
      return 0;
    }

    @Nullable
    @Override
    public AttributedCharacterIterator cancelLatestCommittedText(AttributedCharacterIterator.Attribute[] attributes) {
      return null;
    }

    @Nullable
    @Override
    public AttributedCharacterIterator getSelectedText(AttributedCharacterIterator.Attribute[] attributes) {
      return null;
    }

  }

  public void dispose() {
    myRepaintTimer.stop();
  }
}
