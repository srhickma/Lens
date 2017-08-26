package com.konjex.lens.cores.terminal;

import com.jediterm.terminal.*;
import com.jediterm.terminal.model.JediTerminal;
import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.model.hyperlinks.TextProcessing;
import com.jediterm.terminal.ui.*;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.SwingUtilities;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by shane on 21/08/17.
 */
public class LensTerminal implements TerminalSession {

    private static final Logger log = Logger.getLogger(LensTerminal.class);

    private final TerminalPanel terminalPanel;
    private final JediTerminal jediTerminal;
    private final AtomicBoolean isSessionRunning = new AtomicBoolean();
    private PreConnectHandler preConnectHandler;
    private TtyConnector ttyConnector;
    private TerminalStarter terminalStarter;
    private Thread emulatorThread;

    public LensTerminal(){
        final SettingsProvider settingsProvider = new DefaultSettingsProvider();
        final StyleState styleState = StyleState.defaultStyle(settingsProvider);
        final TextProcessing textProcessing = new TextProcessing(settingsProvider.getHyperlinkColor(), settingsProvider.getHyperlinkHighlightingMode());
        final TerminalTextBuffer terminalTextBuffer = new TerminalTextBuffer(80, 24, styleState, settingsProvider.getBufferMaxLinesCount(), textProcessing);

        terminalPanel = new TerminalPanel(settingsProvider, terminalTextBuffer, styleState);
        jediTerminal = new JediTerminal(terminalPanel, terminalTextBuffer, styleState);
        jediTerminal.setModeEnabled(TerminalMode.AltSendsEscape, settingsProvider.altSendsEscape());

        terminalPanel.addTerminalMouseListener(jediTerminal);
        terminalPanel.setCoordAccessor(jediTerminal);

        preConnectHandler = new PreConnectHandler(jediTerminal);
        terminalPanel.setKeyListener(preConnectHandler);

        isSessionRunning.set(false);
        terminalPanel.init();
        terminalPanel.setVisible(true);
    }

    public void openSession(){
        if(!isSessionRunning.get()){
            openSession(LensTtyConnectorFactory.create());
        }
    }

    private void openSession(TtyConnector ttyConnector) {
        TerminalSession session = createTerminalSession(ttyConnector);
        session.start();
    }

    public TerminalPanel getTerminalPanel() {
        return terminalPanel;
    }

    private void setTtyConnector(@NotNull TtyConnector ttyConnector) {
        this.ttyConnector = ttyConnector;

        terminalStarter = createTerminalStarter(jediTerminal, this.ttyConnector);
        terminalPanel.setTerminalStarter(terminalStarter);
    }

    private TerminalStarter createTerminalStarter(JediTerminal terminal, TtyConnector connector) {
        return new TerminalStarter(terminal, connector, new TtyBasedArrayDataStream(connector));
    }

    @Override
    public TtyConnector getTtyConnector() {
        return ttyConnector;
    }

    @Override
    public Terminal getTerminal() {
        return jediTerminal;
    }

    public void start() {
        if (!isSessionRunning.get()) {
            emulatorThread = new Thread(new EmulatorTask());
            emulatorThread.start();
        } else {
            log.error("Should not try to start session again at this point... ");
        }
    }

    public void stop() {
        if (isSessionRunning.get() && emulatorThread != null) {
            emulatorThread.interrupt();
        }
    }

    @Override
    public TerminalTextBuffer getTerminalTextBuffer() {
        return terminalPanel.getTerminalTextBuffer();
    }

    private TerminalSession createTerminalSession(TtyConnector ttyConnector){
        setTtyConnector(ttyConnector);
        return this;
    }

    @Override
    public void close(){
        if(terminalStarter != null){
            terminalStarter.close();
        }
        terminalPanel.dispose();
    }

    class EmulatorTask implements Runnable {
        public void run(){
            try{
                isSessionRunning.set(true);
                Thread.currentThread().setName("Connector-" + ttyConnector.getName());
                if(ttyConnector.init(preConnectHandler)){
                    terminalPanel.initKeyHandler();
                    SwingUtilities.invokeLater(terminalPanel::requestFocusInWindow);
                    terminalStarter.start();
                }
            }
            catch(Exception e){
                log.error("Exception running terminal", e);
            }
            finally{
                try{
                    ttyConnector.close();
                }
                catch(Exception e){
                    log.error("Exception closing terminal connection", e);
                }
                isSessionRunning.set(false);
                terminalPanel.setKeyListener(preConnectHandler);
            }
        }
    }

}
