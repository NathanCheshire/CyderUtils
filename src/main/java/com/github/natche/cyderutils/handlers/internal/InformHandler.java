package com.github.natche.cyderutils.handlers.internal;

import com.github.natche.cyderutils.bounds.BoundsString;
import com.github.natche.cyderutils.color.CyderColors;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.ui.drag.CyderDragLabel;
import com.github.natche.cyderutils.ui.frame.CyderFrame;
import com.github.natche.cyderutils.ui.frame.enumerations.FrameType;
import com.github.natche.cyderutils.ui.label.CyderLabel;
import com.github.natche.cyderutils.utils.HtmlUtil;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// todo rename maybe PopupManager?

/** Information frames throughout Cyder. */
public final class InformHandler {
    /** The width padding on each side of the information pane. */
    private static final int frameXPadding = 10;

    /** The offset to translate the text label on the information pane down by. */
    private static final int frameYOffset = CyderDragLabel.DEFAULT_HEIGHT;

    /** THe height padding on each side of the information pane. */
    private static final int frameYPadding = 10;

    /** Suppress default constructor. */
    private InformHandler() {
        throw new IllegalMethodException("Instances of InformHandler are not allowed");
    }

    /**
     * A quick information pane.
     *
     * @param text the possibly html styled text to display
     * @return a reference to the shown inform frame
     * @throws IllegalArgumentException if the provided text is null
     */
    @CanIgnoreReturnValue /* Calls don't always need the reference */
    public static CyderFrame inform(String text) {
        Preconditions.checkNotNull(text);

        return inform(new Builder(text).setTitle(Builder.DEFAULT_TITLE));
    }

    /**
     * Opens an information using the information provided by builder.
     *
     * @param builder the builder to use for the construction of the information pane
     * @return a reference to the shown inform frame
     * @throws IllegalArgumentException if the provided builder is null
     */
    @CanIgnoreReturnValue /* Calls don't usually need the reference  */
    public static CyderFrame inform(Builder builder) {
        Preconditions.checkNotNull(builder);

        CyderFrame informFrame;

        // custom container
        if (builder.getContainer() != null) {
            int containerWidth = builder.getContainer().getWidth();
            int containerHeight = builder.getContainer().getHeight();

            int xPadding = 1;
            int yTopPadding = CyderDragLabel.DEFAULT_HEIGHT - 4;
            int yBottomPadding = 2;

            informFrame = new CyderFrame.Builder()
                    .setWidth(containerWidth + 2 * xPadding)
                    .setHeight(containerHeight + yTopPadding + yBottomPadding)
                    .setTitle(builder.getTitle())
                    .build();

            JLabel container = builder.getContainer();
            container.setBounds(xPadding, yTopPadding,
                    containerWidth, containerHeight);
            informFrame.getContentPane().add(container);
        }
        // intended to generate a text inform pane
        else {
            CyderLabel textLabel = new CyderLabel(builder.getHtmlText());
            textLabel.setOpaque(false);
            textLabel.setForeground(CyderColors.defaultLightModeTextColor);

            BoundsString bounds = new BoundsString.Builder(builder.getHtmlText())
                    .setMaxWidth(1200)
                    .build();

            int containerWidth = (int) bounds.getWidth();
            int containerHeight = (int) bounds.getHeight();
            String centeredHtml = HtmlUtil.addCenteringToHtml(bounds.getText());

            textLabel.setText(centeredHtml);

            builder.setContainer(textLabel);

            informFrame = new CyderFrame.Builder()
                    .setWidth(containerWidth + frameXPadding * 2)
                    .setHeight(containerHeight + frameYOffset + 2 * frameYPadding)
                    .build();
            informFrame.setTitle(builder.getTitle());

            int containerX = informFrame.getWidth() / 2 - containerWidth / 2;
            int containerY = informFrame.getHeight() / 2 - containerHeight / 2 + 5;

            JLabel container = builder.getContainer();
            container.setBounds(containerX, containerY, containerWidth, containerHeight);
            informFrame.add(container);
        }

        if (builder.getPreCloseAction() != null) {
            informFrame.addPreCloseAction(builder.getPreCloseAction());
        }
        if (builder.getPostCloseAction() != null) {
            informFrame.addPostCloseAction(builder.getPostCloseAction());
        }

        Component relativeTo = builder.getRelativeTo();

        // if intended to disable relative to
        if (relativeTo != null && builder.isDisableRelativeTo()) {
            relativeTo.setEnabled(false);
            informFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    relativeTo.setEnabled(true);
                }
            });
        }

        informFrame.setFrameType(FrameType.POPUP);
        informFrame.setVisible(true);
        informFrame.setLocationRelativeTo(relativeTo);

        return informFrame;
    }

    /** A builder for an information pane. */
    public static class Builder {
        /** The default title for an information pane which are provided no title. */
        public static final String DEFAULT_TITLE = "Information";

        /** The text, possibly styled with html elements, to display on the information pane. */
        private final String htmlText;

        /** The title of this information pane. */
        private String title = DEFAULT_TITLE;

        /** The component to set this inform frame relative to before the call to setVisible(). */
        private Component relativeTo;

        /** The action to invoke before the frame is closed. */
        private Runnable preCloseAction;

        /** The action to invoke after the frame is closed. */
        private Runnable postCloseAction;

        /** The custom container component to layer on the CyderFrame content pane. */
        private JLabel container;

        /** Whether to disable the relative to component. */
        private boolean disableRelativeTo;

        /**
         * Default constructor for an inform pane with the required parameters.
         *
         * @param htmlText the html styled text to display on the inform pane
         */
        public Builder(String htmlText) {
            Preconditions.checkNotNull(htmlText);
            // Preconditions.checkArgument(StringUtil.getTextLengthIgnoringHtmlTags(htmlText) >= MINIMUM_TEXT_LENGTH);

            this.htmlText = htmlText;
        }

        /**
         * Returns the text associated with this builder, possibly containing html style tags.
         *
         * @return the text associated with this builder, possibly containing html style tags
         */
        public String getHtmlText() {
            return htmlText;
        }

        /**
         * Returns the title for the frame.
         *
         * @return the title for the frame
         */
        public String getTitle() {
            return title;
        }

        /**
         * Sets the title for the frame.
         *
         * @param title the title for the frame
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Returns the component to set the frame relative to.
         *
         * @return the component to set the frame relative to
         */
        public Component getRelativeTo() {
            return relativeTo;
        }

        /**
         * Sets the component to set the frame relative to.
         *
         * @param relativeTo the component to set the frame relative to
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setRelativeTo(Component relativeTo) {
            this.relativeTo = relativeTo;
            return this;
        }

        /**
         * Returns the pre close action to invoke before disposing the frame.
         *
         * @return the pre close action to invoke before disposing the frame
         */
        public Runnable getPreCloseAction() {
            return preCloseAction;
        }

        /**
         * Sets the pre close action to invoke before disposing the frame.
         *
         * @param preCloseAction the pre close action to invoke before disposing the frame
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setPreCloseAction(Runnable preCloseAction) {
            this.preCloseAction = preCloseAction;
            return this;
        }

        /**
         * Returns the post close action to invoke before disposing the frame.
         *
         * @return the post close action to invoke before disposing the frame
         */
        public Runnable getPostCloseAction() {
            return postCloseAction;
        }

        /**
         * Sets the post close action to invoke before closing the frame.
         *
         * @param postCloseAction the post close action to invoke before closing the frame
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setPostCloseAction(Runnable postCloseAction) {
            this.postCloseAction = postCloseAction;
            return this;
        }

        /**
         * Returns the container to use for the frame's pane.
         *
         * @return the container to use for the frame's pane
         */
        public JLabel getContainer() {
            return container;
        }

        /**
         * Sets the container to use for the frame's pane.
         *
         * @param container the container to use for the frame's pane
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setContainer(JLabel container) {
            this.container = container;
            return this;
        }

        /**
         * Returns whether to disable the relative to component upon showing this dialog.
         *
         * @return whether to disable the relative to component upon showing this dialog
         */
        public boolean isDisableRelativeTo() {
            return disableRelativeTo;
        }

        /**
         * Sets whether to disable the relative to component upon showing this dialog.
         *
         * @param disableRelativeTo to disable the relative to component upon showing this dialog
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setDisableRelativeTo(boolean disableRelativeTo) {
            this.disableRelativeTo = disableRelativeTo;
            return this;
        }

        /** Creates an information pane using the parameters set by this builder */
        public void inform() {
            InformHandler.inform(this);
        }
    }
}