package cyder.network.scrapers;

/**
 * HTML class names used by {@link WhatsMyIpScraper} to locate document elements.
 */
public enum HtmlClassName {
    ISP("block text-4xl", true),
    CITY_STATE_COUNTRY("grid grid-cols-3 gap-2 px-6 pb-6", true),
    HOSTNAME("prose", false),
    IP("px-14 font-semibold break-all", true);

    /**
     * The HTML name for this class.
     */
    private final String htmlClassName;
    private final boolean isTailwindCss;

    HtmlClassName(String htmlClassName, boolean isTailwindCss) {
        this.htmlClassName = htmlClassName;
        this.isTailwindCss = isTailwindCss;
    }

    /**
     * Returns the HTML name for this class.
     *
     * @return the HTML name for this class
     */
    public String getHtmlClassName() {
        return htmlClassName;
    }

    /**
     * Returns whether this HTML class name is Tailwind.
     *
     * @return whether this HTML class name is Tailwind
     */
    public boolean isTailwindCss() {
        return isTailwindCss;
    }
}
