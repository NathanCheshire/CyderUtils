package cyder.network.scrapers;

/**
 * Element indices used for extracting HTML elements from the
 * {@link org.jsoup.Jsoup} document by {@link WhatsMyIpScraper}.
 */
enum HtmlElementIndex {
    CITY(2),
    STATE(4),
    COUNTRY(6),
    HOSTNAME(3),
    IP(0);

    private final int index;

    HtmlElementIndex(int index) {
        this.index = index;
    }

    /**
     * Returns the index for obtaining this HTML element.
     *
     * @return the index for obtaining this HTML element
     */
    public int getIndex() {
        return index;
    }
}
