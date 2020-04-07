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

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.security.PrivilegedExceptionAction;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicRootPaneUI;

import mdlaf.components.titlepane.MaterialTitlePaneUI;
import mdlaf.utils.MaterialLogger;

/**
 * @author Terry Kellerman
 * The source code is here http://hg.openjdk.java.net/jdk/client/file/3ec2f3f942b4/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTabbedPaneUI.java
 * @author https://github.com/vincenzopalazzo
 */
public class MaterialRootPaneUI extends BasicRootPaneUI {
    //TODO refactoring this component
    protected static final String[] borderKeys = new String[]{
            null, "RootPane.frameBorder", "RootPane.plainDialogBorder",
            "RootPane.informationDialogBorder",
            "RootPane.errorDialogBorder", "RootPane.colorChooserDialogBorder",
            "RootPane.fileChooserDialogBorder", "RootPane.questionDialogBorder",
            "RootPane.warningDialogBorder"
    };
    protected Cursor myLastCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

    protected enum CursorState {EXITED, ENTERED, NIL}

    protected static final int CORNER_DRAG_WIDTH = 16;

    protected static final int BORDER_DRAG_THICKNESS = 5;

    protected Window window;

    protected JComponent titlePane;

    protected MaterialHandler materialHandler;

    protected LayoutManager layoutManager;

    protected LayoutManager savedOldLayout;

    protected JRootPane root;

    private boolean dragging = false;
    private boolean resizing = false;

    private void cancelResize(Window w) {
        if (resizing) {
            if (materialHandler != null) {
                materialHandler.finishMouseReleased(w);
            }
        }
    }

    private void setupDragMode(Window f) { }

    public void beginDraggingFrame(Window f) {
        setupDragMode(f);
    }

    public void dragFrame(Window w, int newX, int newY) {
        setBoundsForFrame(w, newX, newY, w.getWidth(), w.getHeight());
    }

    public void endDraggingFrame(Window f) { }

    public void beginResizingFrame(Window f, int direction) {
        setupDragMode(f);
    }

    /**
     * Calls <code>setBoundsForFrame</code> with the new values.
     *
     * @param f         the component to be resized
     * @param newX      the new x-coordinate
     * @param newY      the new y-coordinate
     * @param newWidth  the new width
     * @param newHeight the new height
     */
    public void resizeFrame(Window f, int newX, int newY, int newWidth, int newHeight) {
        setBoundsForFrame(f, newX, newY, newWidth, newHeight);
    }

    public void endResizingFrame(Window f) {
    }

    public void setBoundsForFrame(Window f, int newX, int newY, int newWidth, int newHeight) {
        f.setBounds(newX, newY, newWidth, newHeight);
        // we must validate the hierarchy to not break the hw/lw mixing
        f.revalidate();
    }

    public static ComponentUI createUI(JComponent c) {
        return new MaterialRootPaneUI();
    }

    public void installUI(JComponent c) {
        super.installUI(c);
        root = (JRootPane) c;
        root.setBackground(UIManager.getColor("RootPane.background"));
        int style = root.getWindowDecorationStyle();
        if (style != JRootPane.NONE) {
            installClientDecorations(root);
        }
    }

    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        uninstallClientDecorations(root);

        layoutManager = null;
        materialHandler = null;
        root = null;
    }

    protected void uninstallBorder(JRootPane root) {
        LookAndFeel.uninstallBorder(root);
    }

    protected void installWindowListeners(JRootPane root, Component parent) {
        if (parent instanceof Window) {
            window = (Window) parent;
        } else {
            window = SwingUtilities.getWindowAncestor(parent);
        }
        if (window != null) {
            if (materialHandler == null) {
                materialHandler = createWindowHandler(root);
            }
            window.addMouseListener(materialHandler);
            window.addMouseMotionListener(materialHandler);

            window.addWindowFocusListener(materialHandler);
            window.addWindowListener(materialHandler);
        }
    }

    protected void uninstallWindowListeners(JRootPane root) {
        if (window != null) {
            window.removeMouseListener(materialHandler);
            window.removeMouseMotionListener(materialHandler);
            window.removeWindowFocusListener(materialHandler);
            window.removeWindowListener(materialHandler);
        }
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
        installWindowListeners(root, root.getParent());
        installLayout(root);
        if (window != null) {
            root.revalidate();
            root.repaint();
        }
    }

    protected void uninstallClientDecorations(JRootPane root) {
        uninstallBorder(root);
        uninstallWindowListeners(root);
        setTitlePane(root, null);
        uninstallLayout(root);
        int style = root.getWindowDecorationStyle();
        if (style == JRootPane.NONE) {
            root.repaint();
            root.revalidate();
        }
        if (window != null) {
            window.setCursor(Cursor.getPredefinedCursor
                    (Cursor.DEFAULT_CURSOR));
        }
        window = null;
    }

    protected JComponent createTitlePane(JRootPane root) {
        return new MaterialTitlePaneUI(root);
    }

    protected MaterialHandler createWindowHandler(JRootPane root) {
        return new MaterialRootPaneUI.MaterialHandler();
    }

    protected LayoutManager createLayoutManager() {
        return new MaterialLayaut();

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

    protected JRootPane getRootPane() {
        return root;
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
        } else if (propertyName.equals("ancestor")) {
            uninstallWindowListeners(root);
            if (((JRootPane) e.getSource()).getWindowDecorationStyle() !=
                    JRootPane.NONE) {
                installWindowListeners(root, root.getParent());
            }
        }
        return;
    }

    protected static class MaterialLayaut implements LayoutManager2 {

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

            if (root.getWindowDecorationStyle() != JRootPane.NONE &&
                    (root.getUI() instanceof MaterialRootPaneUI)) {
                JComponent titlePane = ((MaterialRootPaneUI) root.getUI()).getTitlePane();
                if (titlePane != null) {
                    tpd = titlePane.getPreferredSize();
                    if (tpd != null) {
                        tpWidth = tpd.width;
                        tpHeight = tpd.height;
                    }
                }
            }

            return new Dimension(Math.max(Math.max(cpWidth, mbWidth), tpWidth) + i.left + i.right,
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
            if (root.getWindowDecorationStyle() != JRootPane.NONE &&
                    (root.getUI() instanceof MaterialRootPaneUI)) {
                JComponent titlePane = ((MaterialRootPaneUI) root.getUI()).getTitlePane();
                if (titlePane != null) {
                    tpd = titlePane.getMinimumSize();
                    if (tpd != null) {
                        tpWidth = tpd.width;
                        tpHeight = tpd.height;
                    }
                }
            }

            return new Dimension(Math.max(Math.max(cpWidth, mbWidth), tpWidth) + i.left + i.right,
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

            if (root.getWindowDecorationStyle() != JRootPane.NONE &&
                    (root.getUI() instanceof MaterialRootPaneUI)) {
                JComponent titlePane = ((MaterialRootPaneUI) root.getUI()).
                        getTitlePane();
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
            if (root.getWindowDecorationStyle() != JRootPane.NONE &&
                    (root.getUI() instanceof MaterialRootPaneUI)) {
                JComponent titlePane = ((MaterialRootPaneUI) root.getUI()).
                        getTitlePane();
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
                Dimension cpd = root.getContentPane().getPreferredSize();
                root.getContentPane().setBounds(0, nextY, w,
                        h < nextY ? 0 : h - nextY);
            }
        }

        public void addLayoutComponent(String name, Component comp) {
        }

        public void removeLayoutComponent(Component comp) {
        }

        public void addLayoutComponent(Component comp, Object constraints) {
        }

        public float getLayoutAlignmentX(Container target) {
            return 0.0f;
        }

        public float getLayoutAlignmentY(Container target) {
            return 0.0f;
        }

        public void invalidateLayout(Container target) {
        }
    }

    /**
     * Maps from positions to cursor type. Refer to calculateCorner and
     * calculatePosition for details of this.
     */
    /*protected static final int[] cursorMapping = new int[]
            {       Cursor.NW_RESIZE_CURSOR, Cursor.DEFAULT_CURSOR, Cursor.N_RESIZE_CURSOR,
                    Cursor.DEFAULT_CURSOR, Cursor.DEFAULT_CURSOR,
                    Cursor.NW_RESIZE_CURSOR, 0, 0, 0, Cursor.NE_RESIZE_CURSOR,
                    Cursor.DEFAULT_CURSOR, 0, 0, 0, Cursor.DEFAULT_CURSOR,
                    Cursor.SW_RESIZE_CURSOR, 0, 0, 0, Cursor.SE_RESIZE_CURSOR,
                    Cursor.SW_RESIZE_CURSOR, Cursor.SW_RESIZE_CURSOR, Cursor.S_RESIZE_CURSOR,
                    Cursor.DEFAULT_CURSOR, Cursor.DEFAULT_CURSOR
            };*/
    public void setMaximized() {
        Component tla = root.getTopLevelAncestor();
        //GraphicsConfiguration gc = (currentRootPaneGC != null) ? currentRootPaneGC : tla.getGraphicsConfiguration();
        GraphicsConfiguration gc = tla.getGraphicsConfiguration();
        Rectangle screenBounds = gc.getBounds();
        screenBounds.x = 0;
        screenBounds.y = 0;
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        Rectangle maxBounds = new Rectangle(
                (screenBounds.x + screenInsets.left),
                (screenBounds.y + screenInsets.top), screenBounds.width
                - ((screenInsets.left + screenInsets.right)),
                screenBounds.height
                        - ((screenInsets.top + screenInsets.bottom)));
        if (tla instanceof JFrame) {
            ((JFrame) tla).setMaximizedBounds(maxBounds);
        }
    }

    protected class MaterialHandler implements MouseInputListener, WindowListener, WindowFocusListener, SwingConstants {
        // _x & _y are the mousePressed location in absolute coordinate system
        int _x, _y;
        // __x & __y are the mousePressed location in source view's coordinate system
        int __x, __y;
        Rectangle startingBounds;
        int resizeDir;
        protected final int RESIZE_NONE = 0;
        private boolean discardRelease = false;
        int resizeCornerSize = 5;

        @SuppressWarnings("unchecked")
        private final PrivilegedExceptionAction getLocationAction = new PrivilegedExceptionAction() {
            public Object run() throws HeadlessException {
                return MouseInfo.getPointerInfo().getLocation();
            }
        };

        void updateFrameCursor(Window w) {
            if (resizing) {
                return;
            }

            Cursor s = myLastCursor;
            if (s == null) {
                s = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            }
            w.setCursor(s);
        }

        void finishMouseReleased(Window w) {
            if (discardRelease) {
                discardRelease = false;
                return;
            }
            if (resizeDir == RESIZE_NONE) {
                endDraggingFrame(w);
                dragging = false;
            } else {
                endResizingFrame(w);
                resizing = false;
                updateFrameCursor(w);
            }
            _x = 0;
            _y = 0;
            __x = 0;
            __y = 0;
            startingBounds = null;
            resizeDir = RESIZE_NONE;
            // Set discardRelease to true, so that only a mousePressed()
            // which sets it to false, will allow entry to the above code
            // for finishing a resize.
            discardRelease = true;
        }

        public void mousePressed(MouseEvent ev) {
            Point p = SwingUtilities.convertPoint((Component) ev.getSource(),
                    ev.getX(), ev.getY(), null);
            __x = ev.getX();
            __y = ev.getY();
            _x = p.x;
            _y = p.y;
            resizeDir = RESIZE_NONE;
            discardRelease = false;
            JRootPane rootPane = getRootPane();

            if (rootPane.getWindowDecorationStyle() == JRootPane.NONE) {
                return;
            }
            Point dragWindowOffset = ev.getPoint();
            Window w = (Window) ev.getSource();
            if (w != null) {
                w.toFront();
            }
            startingBounds = w.getBounds();
            Insets i = w.getInsets();
            Point ep = new Point(__x, __y);
            Point convertedDragWindowOffset = SwingUtilities.convertPoint(w, dragWindowOffset, getTitlePane());
            boolean resizable = false;
            boolean maximized = false;
            if (w instanceof Frame) {
                Frame f = (Frame) w;
                resizable = f.isResizable();
                maximized = (f.getExtendedState() & Frame.MAXIMIZED_BOTH) == 0;
            } else if (w instanceof Dialog) {
                Dialog d = (Dialog) w;
                resizable = d.isResizable();
            }

            if (getTitlePane().getBounds().contains(ev.getPoint())) {
                if (ev.getX() > i.left + resizeCornerSize &&
                        ev.getX() < w.getWidth() - i.right - resizeCornerSize &&
                        ev.getY() > i.top + resizeCornerSize) {
                    beginDraggingFrame(w);
                    dragging = true;
                    return;
                }
            }
            if (!resizable || maximized) {
                return;
            }

            if (ep.x <= i.left + resizeCornerSize) {
                if (ep.y < resizeCornerSize + i.top) {
                    resizeDir = NORTH_WEST;
                } else if (ep.y > w.getHeight()
                        - resizeCornerSize - i.bottom) {
                    resizeDir = SOUTH_WEST;
                } else {
                    resizeDir = WEST;
                }
            } else if (ep.x >= w.getWidth() - i.right - resizeCornerSize) {
                if (ep.y < resizeCornerSize + i.top) {
                    resizeDir = NORTH_EAST;
                } else if (ep.y > w.getHeight()
                        - resizeCornerSize - i.bottom) {
                    resizeDir = SOUTH_EAST;
                } else {
                    resizeDir = EAST;
                }
            } else if (ep.y <= i.top + resizeCornerSize) {
                if (ep.x < resizeCornerSize + i.left) {
                    resizeDir = NORTH_WEST;
                } else if (ep.x > w.getWidth()
                        - resizeCornerSize - i.right) {
                    resizeDir = NORTH_EAST;
                } else {
                    resizeDir = NORTH;
                }
            } else if (ep.y >= w.getHeight() - i.bottom - resizeCornerSize) {
                if (ep.x < resizeCornerSize + i.left) {
                    resizeDir = SOUTH_WEST;
                } else if (ep.x > w.getWidth()
                        - resizeCornerSize - i.right) {
                    resizeDir = SOUTH_EAST;
                } else {
                    resizeDir = SOUTH;
                }
            } else {
             /* the mouse press happened inside the frame, not in the
                border */
                discardRelease = true;
                return;
            }
            Cursor s = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            switch (resizeDir) {
                case SOUTH:
                    s = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
                    break;
                case NORTH:
                    s = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                    break;
                case WEST:
                    s = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                    break;
                case EAST:
                    s = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                    break;
                case SOUTH_EAST:
                    s = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                    break;
                case SOUTH_WEST:
                    s = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                    break;
                case NORTH_WEST:
                    s = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                    break;
                case NORTH_EAST:
                    s = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                    break;
            }
            beginResizingFrame(w, resizeDir);
            w.setCursor(s);
            resizing = true;
            return;
        }

        public void mouseReleased(MouseEvent ev) {
            Window w = (Window) ev.getSource();
            finishMouseReleased(w);
        }

        public void mouseMoved(MouseEvent ev) {
            JRootPane root = getRootPane();

            if (root.getWindowDecorationStyle() == JRootPane.NONE) {
                return;
            }

            Window w = (Window) ev.getSource();

            boolean undecorated = false;
            boolean resizable = false;
            boolean maximized = false;

            if (w instanceof Frame) {
                Frame f = (Frame) w;
                undecorated = f.isUndecorated();
                resizable = f.isResizable();
                maximized = (f.getExtendedState() & Frame.MAXIMIZED_BOTH) == 0;
            } else if (w instanceof Dialog) {
                Dialog d = (Dialog) w;
                undecorated = d.isUndecorated();
                resizable = d.isResizable();
            }

            Insets i = w.getInsets();
            Point ep = new Point(ev.getX(), ev.getY());
            if (resizable && !maximized) {
                if (ep.x <= i.left + resizeCornerSize) {
                    if (ep.y < resizeCornerSize + i.top)
                        w.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                    else if (ep.y > w.getHeight() - resizeCornerSize - i.bottom)
                        w.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                    else
                        w.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                } else if (ep.x >= w.getWidth() - i.right - resizeCornerSize) {
                    if (ev.getY() < resizeCornerSize + i.top)
                        w.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                    else if (ep.y > w.getHeight() - resizeCornerSize - i.bottom)
                        w.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                    else
                        w.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                } else if (ep.y <= i.top + resizeCornerSize) {
                    MaterialLogger.getInstance().debug(this.getClass(), ep.y + "|" + i.top + "|" + ev.getY());
                    if (ep.x < resizeCornerSize + i.left)
                        w.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                    else if (ep.x > w.getWidth() - resizeCornerSize - i.right)
                        w.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                    else
                        w.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                } else if (ep.y >= w.getHeight() - i.bottom - resizeCornerSize) {
                    if (ep.x < resizeCornerSize + i.left)
                        w.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                    else if (ep.x > w.getWidth() - resizeCornerSize - i.right)
                        w.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                    else
                        w.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                } else {
                    updateFrameCursor(w);
                }
            } else {
                updateFrameCursor(w);
            }
        }

        @SuppressWarnings("unchecked")
        public void mouseDragged(MouseEvent e) {
            if (startingBounds == null) {
                // (STEVE) Yucky work around for bug ID 4106552
                return;
            }
            Window w = (Window) e.getSource();
            Point p = SwingUtilities.convertPoint((Component) e.getSource(),
                    e.getX(), e.getY(), null);
            int deltaX = _x - p.x;
            int deltaY = _y - p.y;
            Dimension min = w.getMinimumSize();
            Dimension max = w.getMaximumSize();
            int newX, newY, newW, newH;
            Insets i = w.getInsets();


            boolean undecorated = false;
            boolean resizable = false;
            boolean maximized = false;

            if (w instanceof Frame) {
                Frame f = (Frame) w;
                undecorated = f.isUndecorated();
                resizable = f.isResizable();
                maximized = (f.getExtendedState() & Frame.MAXIMIZED_BOTH) == 0;
            } else if (w instanceof Dialog) {
                Dialog d = (Dialog) w;
                undecorated = d.isUndecorated();
                resizable = d.isResizable();
            }

            // Handle a MOVE
            if (dragging) {
                if (maximized || ((e.getModifiers() &
                        InputEvent.BUTTON1_MASK) !=
                        InputEvent.BUTTON1_MASK)) {
                    // don't allow moving of frames if maximixed or left mouse
                    // button was not used.
                    return;
                }
                int pWidth, pHeight;
                Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
                pWidth = s.width;
                pHeight = s.height;


                newX = startingBounds.x - deltaX;
                newY = startingBounds.y - deltaY;

                // Make sure we stay in-bounds
                if (newX + i.left <= -__x)
                    newX = -__x - i.left + 1;
                if (newY + i.top <= -__y)
                    newY = -__y - i.top + 1;
                if (newX + __x + i.right >= pWidth)
                    newX = pWidth - __x - i.right - 1;
                if (newY + __y + i.bottom >= pHeight)
                    newY = pHeight - __y - i.bottom - 1;

                dragFrame(w, newX, newY);
                return;
            }

            if (!resizable) {
                return;
            }

            newX = w.getX();
            newY = w.getY();
            newW = w.getWidth();
            newH = w.getHeight();

            Dimension parentBounds = Toolkit.getDefaultToolkit().getScreenSize();

            switch (resizeDir) {
                case RESIZE_NONE:
                    return;
                case NORTH:
                    if (startingBounds.height + deltaY < min.height)
                        deltaY = -(startingBounds.height - min.height);
                    else if (startingBounds.height + deltaY > max.height)
                        deltaY = max.height - startingBounds.height;
                    if (startingBounds.y - deltaY < 0) {
                        deltaY = startingBounds.y;
                    }

                    newX = startingBounds.x;
                    newY = startingBounds.y - deltaY;
                    newW = startingBounds.width;
                    newH = startingBounds.height + deltaY;
                    break;
                case NORTH_EAST:
                    if (startingBounds.height + deltaY < min.height)
                        deltaY = -(startingBounds.height - min.height);
                    else if (startingBounds.height + deltaY > max.height)
                        deltaY = max.height - startingBounds.height;
                    if (startingBounds.y - deltaY < 0) {
                        deltaY = startingBounds.y;
                    }

                    if (startingBounds.width - deltaX < min.width)
                        deltaX = startingBounds.width - min.width;
                    else if (startingBounds.width - deltaX > max.width)
                        deltaX = -(max.width - startingBounds.width);
                    if (startingBounds.x + startingBounds.width - deltaX >
                            parentBounds.width) {
                        deltaX = startingBounds.x + startingBounds.width -
                                parentBounds.width;
                    }

                    newX = startingBounds.x;
                    newY = startingBounds.y - deltaY;
                    newW = startingBounds.width - deltaX;
                    newH = startingBounds.height + deltaY;
                    break;
                case EAST:
                    if (startingBounds.width - deltaX < min.width)
                        deltaX = startingBounds.width - min.width;
                    else if (startingBounds.width - deltaX > max.width)
                        deltaX = -(max.width - startingBounds.width);
                    if (startingBounds.x + startingBounds.width - deltaX >
                            parentBounds.width) {
                        deltaX = startingBounds.x + startingBounds.width -
                                parentBounds.width;
                    }

                    newW = startingBounds.width - deltaX;
                    newH = startingBounds.height;
                    break;
                case SOUTH_EAST:
                    if (startingBounds.width - deltaX < min.width)
                        deltaX = startingBounds.width - min.width;
                    else if (startingBounds.width - deltaX > max.width)
                        deltaX = -(max.width - startingBounds.width);
                    if (startingBounds.x + startingBounds.width - deltaX >
                            parentBounds.width) {
                        deltaX = startingBounds.x + startingBounds.width -
                                parentBounds.width;
                    }

                    if (startingBounds.height - deltaY < min.height)
                        deltaY = startingBounds.height - min.height;
                    else if (startingBounds.height - deltaY > max.height)
                        deltaY = -(max.height - startingBounds.height);
                    if (startingBounds.y + startingBounds.height - deltaY >
                            parentBounds.height) {
                        deltaY = startingBounds.y + startingBounds.height -
                                parentBounds.height;
                    }

                    newW = startingBounds.width - deltaX;
                    newH = startingBounds.height - deltaY;
                    break;
                case SOUTH:
                    if (startingBounds.height - deltaY < min.height)
                        deltaY = startingBounds.height - min.height;
                    else if (startingBounds.height - deltaY > max.height)
                        deltaY = -(max.height - startingBounds.height);
                    if (startingBounds.y + startingBounds.height - deltaY >
                            parentBounds.height) {
                        deltaY = startingBounds.y + startingBounds.height -
                                parentBounds.height;
                    }

                    newW = startingBounds.width;
                    newH = startingBounds.height - deltaY;
                    break;
                case SOUTH_WEST:
                    if (startingBounds.height - deltaY < min.height)
                        deltaY = startingBounds.height - min.height;
                    else if (startingBounds.height - deltaY > max.height)
                        deltaY = -(max.height - startingBounds.height);
                    if (startingBounds.y + startingBounds.height - deltaY >
                            parentBounds.height) {
                        deltaY = startingBounds.y + startingBounds.height -
                                parentBounds.height;
                    }

                    if (startingBounds.width + deltaX < min.width)
                        deltaX = -(startingBounds.width - min.width);
                    else if (startingBounds.width + deltaX > max.width)
                        deltaX = max.width - startingBounds.width;
                    if (startingBounds.x - deltaX < 0) {
                        deltaX = startingBounds.x;
                    }

                    newX = startingBounds.x - deltaX;
                    newY = startingBounds.y;
                    newW = startingBounds.width + deltaX;
                    newH = startingBounds.height - deltaY;
                    break;
                case WEST:
                    if (startingBounds.width + deltaX < min.width)
                        deltaX = -(startingBounds.width - min.width);
                    else if (startingBounds.width + deltaX > max.width)
                        deltaX = max.width - startingBounds.width;
                    if (startingBounds.x - deltaX < 0) {
                        deltaX = startingBounds.x;
                    }

                    newX = startingBounds.x - deltaX;
                    newY = startingBounds.y;
                    newW = startingBounds.width + deltaX;
                    newH = startingBounds.height;
                    break;
                case NORTH_WEST:
                    if (startingBounds.width + deltaX < min.width)
                        deltaX = -(startingBounds.width - min.width);
                    else if (startingBounds.width + deltaX > max.width)
                        deltaX = max.width - startingBounds.width;
                    if (startingBounds.x - deltaX < 0) {
                        deltaX = startingBounds.x;
                    }

                    if (startingBounds.height + deltaY < min.height)
                        deltaY = -(startingBounds.height - min.height);
                    else if (startingBounds.height + deltaY > max.height)
                        deltaY = max.height - startingBounds.height;
                    if (startingBounds.y - deltaY < 0) {
                        deltaY = startingBounds.y;
                    }

                    newX = startingBounds.x - deltaX;
                    newY = startingBounds.y - deltaY;
                    newW = startingBounds.width + deltaX;
                    newH = startingBounds.height + deltaY;
                    break;
                default:
                    return;
            }
            resizeFrame(w, newX, newY, newW, newH);
        }

        public void mouseEntered(MouseEvent ev) {
            Window w = (Window) ev.getSource();
            updateFrameCursor(w);
            /*if (cursorState == CursorState.EXITED || cursorState == CursorState.NIL) {
                myLastCursor = w.getCursor();
            }
            cursorState = CursorState.ENTERED;
            mouseMoved(ev);*/
        }

        public void mouseExited(MouseEvent ev) {
            Window w = (Window) ev.getSource();
            updateFrameCursor(w);
            //w.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            //cursorState = CursorState.EXITED;
        }

        public void mouseClicked(MouseEvent ev) {
            Window w = (Window) ev.getSource();
            Frame f;

            if (w instanceof Frame) {
                f = (Frame) w;
            } else {
                return;
            }

            JComponent windowTitlePane = getTitlePane();
            if (windowTitlePane == null) {
                return;
            }

            Point convertedPoint = SwingUtilities.convertPoint(w, ev.getPoint(), windowTitlePane);

            int state = f.getExtendedState();
            if (windowTitlePane.contains(convertedPoint)) {
                if (((ev.getClickCount() % 2) == 0)
                        && ((ev.getModifiers() & InputEvent.BUTTON1_MASK) != 0)) {
                    if (f.isResizable()) {
                        if ((state & Frame.MAXIMIZED_BOTH) != 0) {
                            setMaximized();
                            f.setExtendedState(state & ~Frame.MAXIMIZED_BOTH);
                        } else {
                            setMaximized();
                            f.setExtendedState(state | Frame.MAXIMIZED_BOTH);
                        }
                    }
                }
            }
        }

        @Override
        public void windowOpened(WindowEvent e) {
        }

        @Override
        public void windowClosing(WindowEvent e) {
        }

        @Override
        public void windowClosed(WindowEvent e) {
            cancelResize(e.getWindow());
        }

        @Override
        public void windowIconified(WindowEvent e) {
        }

        @Override
        public void windowDeiconified(WindowEvent e) {

        }

        @Override
        public void windowActivated(WindowEvent e) {

        }

        @Override
        public void windowDeactivated(WindowEvent e) {

        }

        @Override
        public void windowGainedFocus(WindowEvent e) {

        }

        @Override
        public void windowLostFocus(WindowEvent e) {
            cancelResize(e.getWindow());
        }
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
