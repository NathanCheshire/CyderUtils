package com.github.natche.cyderutils.ui.pane;

import com.google.common.base.Preconditions;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.layouts.CyderLayout;
import com.github.natche.cyderutils.strings.ToStringUtil;
import com.github.natche.cyderutils.ui.frame.CyderFrame;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * CyderPanels are what hold and manage where components go on them.
 * They are essentially a wrapper for layouts (classes which extends {@link CyderLayout}).
 */
public final class CyderPanel extends JLabel {
    /*
     * Note to maintainers: this class extends JLabel to allow it to be a content pane
     * for a CyderFrame or act like a regular component.
     */

    /** The message to show as an error if the default constructor is invoked. */
    private static final String INSTANTIATION_ERROR_MESSAGE = "Cannot instantiate cyder panel without a valid layout";

    /** Suppress default constructor. */
    private CyderPanel() {
        throw new IllegalMethodException(INSTANTIATION_ERROR_MESSAGE);
    }

    /**
     * Constructs a new cyder panel with the provided layout.
     *
     * @param cyderLayout the layout that manages components
     */
    public CyderPanel(CyderLayout cyderLayout) {
        this.cyderLayout = Preconditions.checkNotNull(cyderLayout);
        cyderLayout.setAssociatedPanel(this);
        revalidateComponents();
    }

    /** The layout which we are wrapping with this panel. */
    private final CyderLayout cyderLayout;

    /**
     * Sets the layout manager to null due to this being a CyderPanel.
     * Use a custom {@link CyderLayout} class, add that to a CyderPanel,
     * and then set the panel to a {@link CyderFrame}'s content pane to use layouts with Cyder.
     */
    @Override
    @Deprecated
    public void setLayout(LayoutManager layout) {
        super.setLayout(null);
    }

    /** Whether the content pane should be repainted. */
    private boolean disableContentRepainting;

    /**
     * Returns whether content pane painting is disabled.
     *
     * @return whether content pane painting is disabled
     */
    public boolean isContentRepaintingDisabled() {
        return disableContentRepainting;
    }

    /**
     * Sets the state of disable content repainting.
     *
     * @param disableContentRepainting whether the content pane should be repainted
     */
    public void setDisableContentRepainting(boolean disableContentRepainting) {
        this.disableContentRepainting = disableContentRepainting;
    }

    /** {@inheritDoc} */
    @Override
    public void repaint() {
        if (!disableContentRepainting) {
            super.repaint();
            revalidateComponents();
        }
    }

    /** Revalidates the components managed by the linked layout. */
    public void revalidateComponents() {
        if (cyderLayout != null) {
            cyderLayout.revalidateComponents();
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return ToStringUtil.commonUiComponentToString(this);
    }

    /**
     * Returns the components managed by the layout.
     *
     * @return the components managed by the layout
     */
    public Collection<Component> getLayoutComponents() {
        return cyderLayout.getLayoutComponents();
    }
}
