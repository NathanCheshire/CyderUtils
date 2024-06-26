package com.github.natche.cyderutils.github;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.network.NetworkUtil;
import com.github.natche.cyderutils.strings.CyderStrings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A wrapper for a GitHub link to clone a repository. */
@Immutable
public class GithubCloneRepoLink {
    /** The regex to extract the user and repo name from a valid GitHub ".git" repo clone link. */
    private static final String githubRepoCloneRegex = "^((http|https)://)?(www\\.)?github\\.com/(.*)/(.*)\\.git";

    /** The compiled pattern matcher for {@link #githubRepoCloneRegex}. */
    private static final Pattern githubRepoClonePattern = Pattern.compile(githubRepoCloneRegex);

    /** The world wide web subdomain. */
    private static final String www = "www";

    /** The colon slash-slash protocol suffix. */
    private static final String colonSlashSlash = "://";

    /** The safe hyper text transfer protocol. */
    private static final String https = "https";

    /** The hyper text transfer protocol. */
    private static final String http = "http";

    /** The safe hyper text transfer protocol with colon slash-slash suffix. */
    private static final String httpsColonSlashSlash = https + colonSlashSlash;

    /** The hyper text transfer protocol with colon slash-slash suffix. */
    private static final String httpColonSlashSlash = http + colonSlashSlash;

    /** The .git link for the GitHub repository. */
    private final String link;

    /** The user/organization the repository belongs to. */
    private final String user;

    /** The repository name. */
    private final String repository;

    /** Suppress default constructor. */
    private GithubCloneRepoLink() {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Constructs a new GitHub clone repo link.
     *
     * @param link the https clone link
     */
    public GithubCloneRepoLink(String link) {
        Preconditions.checkNotNull(link);
        Preconditions.checkArgument(!link.isEmpty());

        Matcher matcher = githubRepoClonePattern.matcher(link);
        Preconditions.checkArgument(matcher.matches());

        this.link = correctCloneLink(link);
        this.user = matcher.group(4);
        this.repository = matcher.group(5);
    }

    /**
     * Returns the raw link.
     *
     * @return the raw link
     */
    public String getLink() {
        return link;
    }

    /**
     * Returns the owner of this repo.
     *
     * @return the owner of this repo
     */
    public String getUser() {
        return user;
    }

    /**
     * Returns the repository name.
     *
     * @return the repository name
     */
    public String getRepository() {
        return repository;
    }

    /**
     * Returns whether the link actually exists.
     *
     * @return whether the link actually exists
     */
    public boolean urlExists() {
        try {
            return !NetworkUtil.readUrl(link).isEmpty();
        } catch (Exception ignored) {}

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "GithubCloneRepoLink{"
                + "link="
                + "\""
                + link
                + "\""
                + ", user="
                + "\""
                + user
                + "\""
                + ", repository="
                + "\""
                + repository
                + "\""
                + "}";
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof GithubCloneRepoLink)) {
            return false;
        }

        GithubCloneRepoLink other = (GithubCloneRepoLink) o;
        return link.equals(other.getLink());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return link.hashCode();
    }

    /**
     * Corrects the provided GitHub clone link to ensure it begins with
     * <a href="https://www.github.com">https://www.github.com</a>.
     *
     * @param link the clone link which should be valid but not necessarily in proper form
     * @return the corrected link
     */
    private static String correctCloneLink(String link) {
        Preconditions.checkNotNull(link);
        Preconditions.checkArgument(!link.isEmpty());

        StringBuilder ret = new StringBuilder(link);

        // Insert www if start is domain
        if (!link.startsWith(www) && !link.startsWith(httpsColonSlashSlash) && !link.startsWith(httpColonSlashSlash)) {
            ret.insert(0, www + ".");
        }
        // Insert https if http not present
        if (!link.startsWith(httpsColonSlashSlash) && !link.startsWith(httpColonSlashSlash)) {
            ret.insert(0, httpsColonSlashSlash);
        }
        // Convert non-safe to safe
        if (ret.toString().startsWith(http) && !ret.toString().startsWith(https)) ret.insert(4, "s");

        String[] parts = ret.toString().split(colonSlashSlash);
        String protocol = parts[0];
        String domainAndRemainingUrl = parts[1];
        // Ensure www precedes domain name
        if (!domainAndRemainingUrl.startsWith(www)) domainAndRemainingUrl = www + "." + domainAndRemainingUrl;

        return protocol + colonSlashSlash + domainAndRemainingUrl;
    }
}
