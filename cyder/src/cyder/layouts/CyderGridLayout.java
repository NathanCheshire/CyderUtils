package cyder.layouts;

import cyder.ui.CyderPanel;
import cyder.utils.ReflectionUtil;

import java.awt.*;
import java.util.ArrayList;

public class CyderGridLayout extends CyderLayout {
    /**
     * The amount of horizontal cells allowable for this grid layout.
     */
    private final int horizontalCells;

    /**
     * The amount of vertical cells allowable for this grid layout.
     */
    private final int verticalCells;

    /**
     * The list of components to be linked to the CyderPanel this LayoutManager is managing.
     */
    private final GridComponent[][] components;

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<Component> getLayoutComponents() {
        ArrayList<Component> ret = new ArrayList<>();

        for (GridComponent[] component : components) {
            for (int j = 0 ; j < components[0].length ; j++) {
                if (component[j] != null)
                    ret.add(component[j].getComponent());
            }
        }

        return ret;
    }

    /**
     * Constructs a new CyderGridLayout with a singular grid cell.
     */
    public CyderGridLayout() {
        this(1, 1);
    }

    /**
     * Constructs a new CyderGridLayout with the provided grid dimensions.
     *
     * @param xCells the amount of horizontal cells to have in the Layout
     * @param yCells the amount of vertical cells to have in the Layout
     */
    public CyderGridLayout(int xCells, int yCells) {
        if (xCells < 1 || yCells < 1)
            throw new IllegalArgumentException("Provided cell length does not meet the minimum requirement");

        horizontalCells = xCells;
        verticalCells = yCells;

        components = new GridComponent[xCells][yCells];
    }

    //the CyderPanel this LM will manager
    private CyderPanel associatedPanel;

    /**
     * Sets the CyderPanel to manage. Components this LM has been given thus far
     * will be evaluated and added to the Panel.
     *
     * @param associatedPanel the CyderPanel to manage
     */
    public void setAssociatedPanel(CyderPanel associatedPanel) {
        this.associatedPanel = associatedPanel;
        revalidateComponents();
    }

    /**
     * Revalidates the bounds of all components on the grid.
     */
    public void revalidateComponents() {
        //if the process would be in vein, return
        if (associatedPanel == null)
            return;

        //partition width into how many horizontal grid spaces we have
        int widthPartition = associatedPanel.getWidth() / horizontalCells;
        //partition height into how many vertical grid spaces we have
        int heightPartition = associatedPanel.getHeight() / verticalCells;

        // keep track of a possible focus owner
        Component focusOwner = null;

        //for all the cells in our grid
        for (int xCell = 0 ; xCell < horizontalCells ; xCell++) {
            for (int yCell = 0; yCell < verticalCells; yCell++) {
                //if no component exists at this location then continue
                if (components[xCell][yCell] == null)
                    continue;

                //figure out the starting values for this cell
                int startX = xCell * widthPartition;
                int startY = yCell * heightPartition;

                //instantiate a reference component for the current component
                GridComponent refComponent = components[xCell][yCell];

                //if this is the first focused component we have come across
                // set it as the focus owner
                if (refComponent.getComponent().isFocusOwner() && focusOwner == null)
                    focusOwner = refComponent.getComponent();

                //if the component is a CyderPanel, give it as much space to work with as possible
                if (refComponent.getComponent() instanceof CyderPanel) {
                    refComponent.getComponent().setBounds(startX, startY, widthPartition, heightPartition);
                    ((CyderPanel) (refComponent.getComponent())).revalidateComponents();
                }
                //otherwise if it doesn't fit in the partitioned space,
                // set the size to as big as we can let it be so now overflow is visible
                else if (refComponent.getOriginalWidth() >= widthPartition ||
                        refComponent.getOriginalHeight() >= heightPartition) {
                    refComponent.getComponent().setBounds(startX, startY,
                            //only one might be over the max value so take the min of partition and len
                            Math.min(widthPartition, refComponent.getOriginalWidth()),
                            Math.min(heightPartition, refComponent.getOriginalHeight()));
                }
                //otherwise it fits so calculate how to place it in the grid space
                else {
                    //figure out values to center the component in the grid space
                    int addX = (widthPartition - refComponent.getOriginalWidth()) / 2;
                    int addY = (heightPartition - refComponent.getOriginalHeight()) / 2;

                    //figure out values for how to move the component from the centered
                    // position to any possible Position value
                    int adjustX = 0;
                    int adjustY = 0;

                    switch (refComponent.getPosition()) {
                        case TOP_LEFT:
                            //move up and to the left
                            adjustX = -(widthPartition - refComponent.getOriginalWidth()) / 2;
                            adjustY = -(heightPartition - refComponent.getOriginalHeight()) / 2;
                            break;
                        case TOP_CENTER:
                            //move up
                            adjustY = -(heightPartition - refComponent.getOriginalHeight()) / 2;
                            break;
                        case TOP_RIGHT:
                            //move up and right
                            adjustX = (widthPartition - refComponent.getOriginalWidth()) / 2;
                            adjustY = -(heightPartition - refComponent.getOriginalHeight()) / 2;
                            break;
                        case MIDDLE_LEFT:
                            //move left
                            adjustX = -(widthPartition - refComponent.getOriginalWidth()) / 2;
                            break;
                        case MIDDLE_CENTER:
                            //relative to origin so nothing done here
                            break;
                        case MIDDLE_RIGHT:
                            // move right
                            adjustX = (widthPartition - refComponent.getOriginalWidth()) / 2;
                            break;
                        case BOTTOM_LEFT:
                            //move down and left
                            adjustX = -(widthPartition - refComponent.getOriginalWidth()) / 2;
                            adjustY = (heightPartition - refComponent.getOriginalHeight()) / 2;
                            break;
                        case BOTTOM_CENTER:
                            //move down
                            adjustY = (heightPartition - refComponent.getOriginalHeight()) / 2;
                            break;
                        case BOTTOM_RIGHT:
                            //move down and right
                            adjustX = (widthPartition - refComponent.getOriginalWidth()) / 2;
                            adjustY = (heightPartition - refComponent.getOriginalHeight()) / 2;
                            break;
                    }

                    //set the component bounds with the original size since the component fits
                    refComponent.getComponent().setBounds(startX + addX + adjustX,
                            startY + addY + adjustY,
                            refComponent.getOriginalWidth(),
                            refComponent.getOriginalHeight());
                }

                //add the component to the panel
                associatedPanel.add(refComponent.getComponent());
            }
        }

        // return focus to original owner
        if (focusOwner != null)
            focusOwner.requestFocus();
    }

    /**
     * Adds the provided component to the grid at the first available space.
     *
     * @param component the component to add to the grid if possible
     */
    public void addComponent(Component component) {
        for (int x = 0 ; x < horizontalCells ; x++) {
            for (int y = 0; y < verticalCells; y++) {
                if (components[x][y] == null) {
                    components[x][y] = new GridComponent(component, //defaults here
                            component.getWidth(), component.getHeight(), GridPosition.MIDDLE_CENTER);
                    repaint();
                    return;
                }
            }
        }

    }

    /**
     * Adds the provided component to the grid at the first available space.
     *
     * @param component       the component to add to the grid if possible
     * @param sectionPosition the position to set the component to if it fits
     *                        in the partitioned space or how to position the component should it overflow the partitioned space
     * @return whether the component was added successfully
     */
    public boolean addComponent(Component component, GridPosition sectionPosition) {
        for (int x = 0 ; x < horizontalCells ; x++) {
            for (int y = 0; y < verticalCells; y++) {
                if (components[x][y] == null) {
                    components[x][y] = new GridComponent(component,
                            component.getWidth(), component.getHeight(), sectionPosition);
                    repaint();
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Adds the provided component to the grid at the specified location.
     *
     * @param component the component to add to the grid
     * @param x         the x value to add the component to
     * @param y         the y value to add the component to
     */
    public void addComponent(Component component, int x, int y) {
        if (components == null)
            throw new IllegalStateException("Components not yet initialized");
        if (x < 0 || x > horizontalCells - 1 || y < 0 || y > verticalCells - 1)
            throw new IllegalArgumentException("Provided grid location is invalid");

        if (components[x][y] != null) {
            //component already here, figure out how to handle this case
            return;
        }

        components[x][y] = new GridComponent(component, component.getWidth(),
                component.getHeight(), GridPosition.MIDDLE_CENTER);
        repaint();
    }

    /**
     * Adds the component to the grid at the specified location with the provided Position value.
     *
     * @param component       the component to add to the grid
     * @param x               the x value to add the component to
     * @param y               the y value to add the component to
     * @param sectionPosition the position value to use to
     *                        figure out how to place the component in its cell
     */
    public void addComponent(Component component, int x, int y, GridPosition sectionPosition) {
        if (components == null)
            throw new IllegalStateException("Components not yet initialized");
        if (x < 0 || x > horizontalCells - 1 || y < 0 || y > verticalCells - 1)
            throw new IllegalArgumentException("Provided grid location is invalid");

        if (components[x][y] != null) {
            //component already here, figure out how to handle this case
            return;
        }

        components[x][y] = new GridComponent(component, component.getWidth(),
                component.getHeight(), sectionPosition);
        repaint();
    }

    /**
     * Removes the specified component from the grid.
     *
     * @param component the component to remove from the panel
     */
    public void removeComponent(Component component) {
        if (components == null)
            throw new IllegalStateException("Components not yet initialized");

        for (int x = 0 ; x < horizontalCells ; x++) {
            for (int y = 0; y < verticalCells; y++) {
                if (components[x][y].getComponent() == component) {
                    components[x][y] = null;
                    return;
                }
            }
        }
    }

    /**
     * Removes the component at the specified location from the grid.
     *
     * @param x the x value of the component to remove
     * @param y the y value of the component to remove
     * @return whether the component was successfully removed
     */
    public boolean removeComponent(int x, int y) {
        if (components == null)
            throw new IllegalStateException("Components not yet initialized");
        if (x < 0 || x > horizontalCells - 1 || y < 0 || y > verticalCells - 1)
            throw new IllegalArgumentException("Provided grid location is invalid");

        //no component there
        if (components[x][y] == null)
            return false;

        //found component so remove and return true
        components[x][y] = null;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPackSize() {
        int sumWidth = 0;
        int sumHeight = 0;

        // todo

        return new Dimension(sumWidth, sumHeight);
    }

    /**
     * Standard overridden toString() method to use reflection.
     *
     * @return a String representation of this LM
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUiToString(this);
    }

}
