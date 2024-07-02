package com.github.natche.cyderutils.network;

/** Common network interface service ports. */
public enum CommonServicePort {
    HTTP(80, "HTTP", "Web service"),
    HTTPS(443,"HTTPS", "Secure web service"),
    FTP(21, "FTP", "File Transfer Protocol"),
    SMTP(25, "SMTP","Simple Mail Transfer Protocol"),
    DNS(53, "DNS","Domain Name System"),
    SSH(22, "SSH","Secure Shell"),
    IMAP(143,"IMAP","Internet Message Access Protocol"),
    IMAPS(993, "IMAPS","Secure IMAP"),
    POP3(110, "POP3","Post Office Protocol"),
    POP3S(995, "POP3S","Secure POP3"),
    NTP(123, "NTP","Network Time Protocol");

    private final int port;
    private final String acronym;
    private final String description;

    CommonServicePort(int port, String acronym, String description) {
        this.port = port;
        this.acronym = acronym;
        this.description = description;
    }

    /**
     * Returns this port number.
     *
     * @return this port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the acronym for this common service port.
     *
     * @return the acronym for this common service port
     */
    public String getAcronym() {
        return acronym;
    }

    /**
     * Returns the description of this service port.
     *
     * @return the description of this service port.
     */
    public String getDescription() {
        return description;
    }
}
