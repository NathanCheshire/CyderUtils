package cyder.enums;

/**
 * Things Cyder inspects at runtime.
 */
public enum CyderInspection {
    /**
     * Inspections related to methods annotated by {@link cyder.annotations.Vanilla}.
     */
    VanillaInspection,

    /**
     * Inspections related to unit tests, {@link cyder.annotations.GuiTest}s, etc.
     */
    TestInspection,

    /**
     * Inspections related to methods annotated by {@link cyder.annotations.Widget}.
     */
    WidgetInspection,

    /**
     * Inspects related to methods annotated by {@link cyder.annotations.Handle}.
     */
    HandleInspection,
}
