/*
 * MIT License
 *
 * Copyright (c) 2026 Vincenzo Palazzo vincenzopalazzodev@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package mdlaf.utils;

import java.awt.AWTEvent;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.AbstractMaterialTheme;
import mdlaf.themes.MaterialTheme;

/**
 * Watches every Window opened while {@link MaterialLookAndFeel} is installed and refreshes Swing's
 * font defaults when a window's {@link GraphicsConfiguration} changes — i.e. it crossed to a
 * display with a different DPI/scale.
 *
 * <p>Issue #205 (Java 17 mixed-DPI font scaling): the JRE applies per-display transforms via {@code
 * sun.java2d.uiScale}, but cached {@code FontUIResource} instances and component metrics computed
 * at construction time can be stale after the move. This watcher invalidates the typeface cache,
 * asks the active theme to re-derive its fonts, refreshes only the {@code *.font} entries in the
 * current {@code UIDefaults} (so colors, borders, and runtime customizations made by the host app
 * are not touched), then triggers {@code updateComponentTreeUI} on the affected window.
 *
 * <p>The watcher is a no-op in headless environments and silently degrades if installing an AWT
 * event listener is denied (e.g. restrictive {@code SecurityManager}).
 */
public final class MaterialDisplayScaleWatcher {

  private static final String GRAPHICS_CONFIGURATION = "graphicsConfiguration";

  private static MaterialDisplayScaleWatcher INSTANCE;

  private final Map<Window, PropertyChangeListener> windowListeners =
      Collections.synchronizedMap(new WeakHashMap<>());

  private AWTEventListener windowOpenedListener;
  private boolean installed;

  private MaterialDisplayScaleWatcher() {}

  /** Install the watcher (idempotent). Safe to call from L&amp;F initialize. */
  public static synchronized void install() {
    if (INSTANCE == null) {
      INSTANCE = new MaterialDisplayScaleWatcher();
    }
    INSTANCE.doInstall();
  }

  /** Uninstall the watcher (idempotent). Safe to call from L&amp;F uninitialize. */
  public static synchronized void uninstall() {
    if (INSTANCE != null) {
      INSTANCE.doUninstall();
    }
  }

  private void doInstall() {
    if (installed || GraphicsEnvironment.isHeadless()) {
      return;
    }
    windowOpenedListener =
        new AWTEventListener() {
          @Override
          public void eventDispatched(AWTEvent event) {
            if (event.getID() == WindowEvent.WINDOW_OPENED && event instanceof WindowEvent) {
              attach(((WindowEvent) event).getWindow());
            }
          }
        };
    try {
      Toolkit.getDefaultToolkit()
          .addAWTEventListener(windowOpenedListener, AWTEvent.WINDOW_EVENT_MASK);
    } catch (SecurityException e) {
      windowOpenedListener = null;
      return;
    }
    // Pick up windows that already exist when the L&F is installed.
    for (Window w : Window.getWindows()) {
      attach(w);
    }
    installed = true;
  }

  private void doUninstall() {
    if (!installed) {
      return;
    }
    if (windowOpenedListener != null) {
      try {
        Toolkit.getDefaultToolkit().removeAWTEventListener(windowOpenedListener);
      } catch (SecurityException ignored) {
        // best-effort
      }
      windowOpenedListener = null;
    }
    synchronized (windowListeners) {
      for (Map.Entry<Window, PropertyChangeListener> entry : windowListeners.entrySet()) {
        Window w = entry.getKey();
        if (w != null) {
          w.removePropertyChangeListener(GRAPHICS_CONFIGURATION, entry.getValue());
        }
      }
      windowListeners.clear();
    }
    installed = false;
  }

  private void attach(Window window) {
    if (window == null || windowListeners.containsKey(window)) {
      return;
    }
    PropertyChangeListener listener =
        new PropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent evt) {
            // Different GraphicsConfiguration means a different GraphicsDevice (or the same
            // device with new properties) — either way the per-display scale may have changed.
            Object oldGc = evt.getOldValue();
            Object newGc = evt.getNewValue();
            if (oldGc == newGc) {
              return;
            }
            scheduleRefresh(window);
          }
        };
    window.addPropertyChangeListener(GRAPHICS_CONFIGURATION, listener);
    windowListeners.put(window, listener);
  }

  private static void scheduleRefresh(final Window window) {
    if (SwingUtilities.isEventDispatchThread()) {
      refreshNow(window);
    } else {
      SwingUtilities.invokeLater(
          new Runnable() {
            @Override
            public void run() {
              refreshNow(window);
            }
          });
    }
  }

  /**
   * Drop cached typefaces, ask the active theme to re-derive its fonts, refresh only the {@code
   * *.font} entries in {@code UIManager.getLookAndFeelDefaults()}, then trigger {@code
   * updateComponentTreeUI} for {@code window}. Avoids reinstalling the L&amp;F so colors, borders,
   * and any runtime customizations made by the host app are preserved. Package-private so tests can
   * exercise it without an actual display change.
   */
  static void refreshNow(Window window) {
    MaterialFontFactory.getInstance().invalidateScaleCache();
    if (UIManager.getLookAndFeel() instanceof MaterialLookAndFeel) {
      MaterialLookAndFeel laf = (MaterialLookAndFeel) UIManager.getLookAndFeel();
      MaterialTheme theme = laf.getTheme();
      if (theme instanceof AbstractMaterialTheme) {
        ((AbstractMaterialTheme) theme).refreshFonts();
      }
      MaterialLookAndFeel.installFontDefaults(UIManager.getLookAndFeelDefaults(), theme);
    }
    if (window != null) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
