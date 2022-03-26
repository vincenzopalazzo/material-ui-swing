/*
 * Copyright (c) 1997, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package mdlaf.components.rootpane;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicRootPaneUI;
import mdlaf.components.titlepane.MaterialTitlePane;
import mdlaf.utils.FlatWindowResizer;

/**
 * @author Terry Kellerman // This code is inside the Open JDK
 * @author https://github.com/vincenzopalazzo
 */
public class MaterialRootPaneUIv2 extends BasicRootPaneUI {

  protected static final String[] borderKeys =
      new String[] {
        null,
        "RootPane.frameBorder",
        "RootPane.plainDialogBorder",
        "RootPane.informationDialogBorder",
        "RootPane.errorDialogBorder",
        "RootPane.colorChooserDialogBorder",
        "RootPane.fileChooserDialogBorder",
        "RootPane.questionDialogBorder",
        "RootPane.warningDialogBorder"
      };

  public static ComponentUI createUI(JComponent c) {
    return new MaterialRootPaneUIv2();
  }

  protected Window window;
  protected JComponent titlePane;
  protected LayoutManager layoutManager;
  protected LayoutManager savedOldLayout;
  protected JRootPane root;
  protected FlatWindowResizer windowResizer;

  public MaterialRootPaneUIv2() {
    super();
  }

  @Override
  protected void installListeners(JRootPane root) {
    super.installListeners(root);
  }

  @Override
  protected void uninstallListeners(JRootPane root) {
    super.uninstallListeners(root);
  }

  @Override
  public void installUI(JComponent c) {
    super.installUI(c);
    root = (JRootPane) c;
    root.setBackground(UIManager.getColor("RootPane.background"));
    windowResizer = createWindowResizer();
    int style = root.getWindowDecorationStyle();
    if (style != JRootPane.NONE) {
      installClientDecorations(root);
    }
  }

  @Override
  public void uninstallUI(JComponent c) {
    super.uninstallUI(c);
    uninstallClientDecorations(root);

    layoutManager = null;
    root = null;
  }

  protected FlatWindowResizer createWindowResizer() {
    return new FlatWindowResizer.WindowResizer(root);
  }

  protected void uninstallBorder(JRootPane root) {
    LookAndFeel.uninstallBorder(root);
  }

  protected void installLayout(JRootPane root) {
    if (layoutManager == null) {
      layoutManager = createLayoutManager();
    }
    savedOldLayout = root.getLayout();
    root.setLayout(layoutManager);
  }

  protected void uninstallLayout(JRootPane root) {
    if (savedOldLayout != null) {
      root.setLayout(savedOldLayout);
      savedOldLayout = null;
    }
  }

  protected void installClientDecorations(JRootPane root) {
    installBorder(root);
    JComponent titlePane = createTitlePane(root);
    setTitlePane(root, titlePane);
    installLayout(root);
    if (window != null) {
      root.revalidate();
      root.repaint();
    }
  }

  protected void uninstallClientDecorations(JRootPane root) {
    uninstallBorder(root);
    setTitlePane(root, null);
    uninstallLayout(root);
    int style = root.getWindowDecorationStyle();
    if (style == JRootPane.NONE) {
      root.repaint();
      root.revalidate();
    }
    if (window != null) {
      window.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    window = null;
  }

  protected JComponent createTitlePane(JRootPane root) {
    return new MaterialTitlePane(root);
  }

  protected LayoutManager createLayoutManager() {
    return new MaterialLayout();
  }

  protected void setTitlePane(JRootPane root, JComponent titlePane) {
    JLayeredPane layeredPane = root.getLayeredPane();
    JComponent oldTitlePane = getTitlePane();

    if (oldTitlePane != null) {
      oldTitlePane.setVisible(false);
      layeredPane.remove(oldTitlePane);
    }
    if (titlePane != null) {
      layeredPane.add(titlePane, JLayeredPane.FRAME_CONTENT_LAYER);
      titlePane.setVisible(true);
    }
    this.titlePane = titlePane;
  }

  protected JComponent getTitlePane() {
    return titlePane;
  }

  public void propertyChange(PropertyChangeEvent e) {
    super.propertyChange(e);

    String propertyName = e.getPropertyName();
    if (propertyName == null) {
      return;
    }

    if (propertyName.equals("windowDecorationStyle")) {
      JRootPane root = (JRootPane) e.getSource();
      int style = root.getWindowDecorationStyle();

      // This is potentially more than needs to be done,
      // but it rarely happens and makes the install/uninstall process
      // simpler. MetalTitlePane also assumes it will be recreated if
      // the decoration style changes.
      uninstallClientDecorations(root);
      if (style != JRootPane.NONE) {
        installClientDecorations(root);
      }
    }
  }

  protected static class MaterialLayout implements LayoutManager2 {

    public Dimension preferredLayoutSize(Container parent) {

      Dimension cpd, mbd, tpd;
      int cpWidth = 0;
      int cpHeight = 0;
      int mbWidth = 0;
      int mbHeight = 0;
      int tpWidth = 0;
      int tpHeight = 0;
      Insets i = parent.getInsets();
      JRootPane root = (JRootPane) parent;

      if (root.getContentPane() != null) {
        cpd = root.getContentPane().getPreferredSize();
      } else {
        cpd = root.getSize();
      }
      if (cpd != null) {
        cpWidth = cpd.width;
        cpHeight = cpd.height;
      }

      if (root.getJMenuBar() != null) {
        mbd = root.getJMenuBar().getPreferredSize();
        if (mbd != null) {
          mbWidth = mbd.width;
          mbHeight = mbd.height;
        }
      }

      if (root.getWindowDecorationStyle() != JRootPane.NONE
          && (root.getUI() instanceof MaterialRootPaneUIv2)) {
        JComponent titlePane = ((MaterialRootPaneUIv2) root.getUI()).getTitlePane();
        if (titlePane != null) {
          tpd = titlePane.getPreferredSize();
          if (tpd != null) {
            tpWidth = tpd.width;
            tpHeight = tpd.height;
          }
        }
      }

      return new Dimension(
          Math.max(Math.max(cpWidth, mbWidth), tpWidth) + i.left + i.right,
          cpHeight + mbHeight + tpWidth + i.top + i.bottom);
    }

    public Dimension minimumLayoutSize(Container parent) {
      Dimension cpd, mbd, tpd;
      int cpWidth = 0;
      int cpHeight = 0;
      int mbWidth = 0;
      int mbHeight = 0;
      int tpWidth = 0;
      int tpHeight = 0;
      Insets i = parent.getInsets();
      JRootPane root = (JRootPane) parent;

      if (root.getContentPane() != null) {
        cpd = root.getContentPane().getMinimumSize();
      } else {
        cpd = root.getSize();
      }
      if (cpd != null) {
        cpWidth = cpd.width;
        cpHeight = cpd.height;
      }

      if (root.getJMenuBar() != null) {
        mbd = root.getJMenuBar().getMinimumSize();
        if (mbd != null) {
          mbWidth = mbd.width;
          mbHeight = mbd.height;
        }
      }
      if (root.getWindowDecorationStyle() != JRootPane.NONE
          && (root.getUI() instanceof MaterialRootPaneUIv2)) {
        JComponent titlePane = ((MaterialRootPaneUIv2) root.getUI()).getTitlePane();
        if (titlePane != null) {
          tpd = titlePane.getMinimumSize();
          if (tpd != null) {
            tpWidth = tpd.width;
            tpHeight = tpd.height;
          }
        }
      }

      return new Dimension(
          Math.max(Math.max(cpWidth, mbWidth), tpWidth) + i.left + i.right,
          cpHeight + mbHeight + tpWidth + i.top + i.bottom);
    }

    public Dimension maximumLayoutSize(Container target) {
      Dimension cpd, mbd, tpd;
      int cpWidth = Integer.MAX_VALUE;
      int cpHeight = Integer.MAX_VALUE;
      int mbWidth = Integer.MAX_VALUE;
      int mbHeight = Integer.MAX_VALUE;
      int tpWidth = Integer.MAX_VALUE;
      int tpHeight = Integer.MAX_VALUE;
      Insets i = target.getInsets();
      JRootPane root = (JRootPane) target;

      if (root.getContentPane() != null) {
        cpd = root.getContentPane().getMaximumSize();
        if (cpd != null) {
          cpWidth = cpd.width;
          cpHeight = cpd.height;
        }
      }

      if (root.getJMenuBar() != null) {
        mbd = root.getJMenuBar().getMaximumSize();
        if (mbd != null) {
          mbWidth = mbd.width;
          mbHeight = mbd.height;
        }
      }

      if (root.getWindowDecorationStyle() != JRootPane.NONE
          && (root.getUI() instanceof MaterialRootPaneUIv2)) {
        JComponent titlePane = ((MaterialRootPaneUIv2) root.getUI()).getTitlePane();
        if (titlePane != null) {
          tpd = titlePane.getMaximumSize();
          if (tpd != null) {
            tpWidth = tpd.width;
            tpHeight = tpd.height;
          }
        }
      }

      int maxHeight = Math.max(Math.max(cpHeight, mbHeight), tpHeight);
      if (maxHeight != Integer.MAX_VALUE) {
        maxHeight = cpHeight + mbHeight + tpHeight + i.top + i.bottom;
      }

      int maxWidth = Math.max(Math.max(cpWidth, mbWidth), tpWidth);
      if (maxWidth != Integer.MAX_VALUE) {
        maxWidth += i.left + i.right;
      }

      return new Dimension(maxWidth, maxHeight);
    }

    public void layoutContainer(Container parent) {
      JRootPane root = (JRootPane) parent;
      Rectangle b = root.getBounds();
      Insets i = root.getInsets();
      int nextY = 0;
      int w = b.width - i.right - i.left;
      int h = b.height - i.top - i.bottom;

      if (root.getLayeredPane() != null) {
        root.getLayeredPane().setBounds(i.left, i.top, w, h);
      }
      if (root.getGlassPane() != null) {
        root.getGlassPane().setBounds(i.left, i.top, w, h);
      }
      if (root.getWindowDecorationStyle() != JRootPane.NONE
          && (root.getUI() instanceof MaterialRootPaneUIv2)) {
        JComponent titlePane = ((MaterialRootPaneUIv2) root.getUI()).getTitlePane();
        if (titlePane != null) {
          Dimension tpd = titlePane.getPreferredSize();
          if (tpd != null) {
            int tpHeight = tpd.height;
            titlePane.setBounds(0, 0, w, tpHeight);
            nextY += tpHeight;
          }
        }
      }
      if (root.getJMenuBar() != null) {
        Dimension mbd = root.getJMenuBar().getPreferredSize();
        root.getJMenuBar().setBounds(0, nextY, w, mbd.height);
        nextY += mbd.height;
      }
      if (root.getContentPane() != null) {
        // Dimension cpd = root.getContentPane().getPreferredSize();
        root.getContentPane().setBounds(0, nextY, w, h < nextY ? 0 : h - nextY);
      }
    }

    public void addLayoutComponent(String name, Component comp) {}

    public void removeLayoutComponent(Component comp) {}

    public void addLayoutComponent(Component comp, Object constraints) {}

    public float getLayoutAlignmentX(Container target) {
      return 0.0f;
    }

    public float getLayoutAlignmentY(Container target) {
      return 0.0f;
    }

    public void invalidateLayout(Container target) {}
  }

  protected void installBorder(JRootPane root) {
    int style = root.getWindowDecorationStyle();

    if (style == JRootPane.NONE) {
      LookAndFeel.uninstallBorder(root);
    } else {
      LookAndFeel.installBorder(root, borderKeys[style]);
    }
  }
}
