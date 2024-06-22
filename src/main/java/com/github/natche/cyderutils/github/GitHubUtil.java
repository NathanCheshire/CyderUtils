package com.github.natche.cyderutils.github;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.github.parsers.Issue;
import com.github.natche.cyderutils.process.ProcessUtil;
import com.github.natche.cyderutils.threads.CyderThreadFactory;
import com.github.natche.cyderutils.utils.OsUtil;
import com.github.natche.cyderutils.utils.SerializationUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.github.natche.cyderutils.strings.CyderStrings.ATTEMPTED_INSTANTIATION;

/**
 * Utilities for working with REST APIs provided by <a href="https://www.github.com">GitHub.com</a>.
 */
public final class GitHubUtil {
    /**
     * The Cyder author GitHub username.
     */
    private static final String authorUsername = "nathancheshire";

    /**
     * The name for the Cyder GitHub repository.
     */
    private static final String repoName = "cyderutils";

    /**
     * The name of the thread for cloning repos from GitHub.
     */
    private static final String GIT_REPO_CLONER_THREAD_NAME = "Git Repo Cloner";

    /**
     * Executor service used to clone github repos.
     */
    private static final ExecutorService cloningExecutor = Executors.newSingleThreadScheduledExecutor(
            new CyderThreadFactory(GIT_REPO_CLONER_THREAD_NAME));

    /**
     * The url to download Git from.
     */
    private static final String GIT_DOWNLOAD_URL = "https://git-scm.com/downloads";

    /**
     * The repos keyword for the GitHub repos API.
     */
    private static final String REPOS = "repos";

    /**
     * The repos keyword for the GitHub languages API.
     */
    private static final String LANGUAGES = "languages";

    /**
     * The repos keyword for the GitHub issues API.
     */
    private static final String ISSUES = "issues";

    /**
     * The Git command string.
     */
    private static final String GIT = "git";

    /**
     * The GitHub repos API header.
     */
    private static final String GITHUB_REPOS_API_HEADER = "https://api.github.com/" + REPOS;

    /**
     * The url to get the languages used throughout Cyder from.
     */
    private static final String cyderLanguagesUrl = GITHUB_REPOS_API_HEADER + "/" + authorUsername
            + "/" + repoName + "/" + LANGUAGES;

    /**
     * Suppress default constructor.
     */
    private GitHubUtil() {
        throw new IllegalMethodException(ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns a list of issues for Cyder.
     *
     * @return the list of currently open issues for Cyder
     */
    public static ImmutableList<Issue> getCyderIssues() {
        return getIssues(authorUsername, repoName);
    }

    /**
     * Returns a list of issues for the repo under the provided user.
     *
     * @param user       the github user
     * @param githubRepo the github repo
     * @return a list of issues
     */
    public static ImmutableList<Issue> getIssues(String user, String githubRepo) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(githubRepo);
        Preconditions.checkArgument(!user.isEmpty());
        Preconditions.checkArgument(!githubRepo.isEmpty());

        Issue[] ret = new Issue[0];

        try {
            String urlString = GITHUB_REPOS_API_HEADER + "/" + user + "/" + githubRepo + "/" + ISSUES;
            URL url = new URL(urlString);

            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();
            String rawJson = sb.toString();

            ret = SerializationUtil.fromJson(rawJson, Issue[].class);
        } catch (IOException ignored) {}

        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns the languages used to code Cyder along with the raw number of bytes of the respective language.
     *
     * @return the languages used to code Cyder along with the raw number of bytes of the respective language
     */
    public static HashMap<String, Integer> getLanguages() {
        HashMap<String, Integer> ret = new HashMap<>();

        try {
            URL url = new URL(cyderLanguagesUrl);

            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();
            String rawJson = sb.toString();

            Type type = new TypeToken<HashMap<String, Integer>>() {}.getType();
            ret = SerializationUtil.fromJson(rawJson, type);
        } catch (IOException ignored) {}

        return ret;
    }

    /**
     * Clones the provided github repo to the provided directory.
     *
     * @param cloneLink the URL of the github repository to clone
     * @param directory the directory to save the repo to
     *                  Note this directory must exist prior to method invocation
     * @return whether the repo was successfully cloned and saved
     */
    public static Future<Boolean> cloneRepoToDirectory(String cloneLink, File directory) {
        Preconditions.checkNotNull(cloneLink);
        Preconditions.checkArgument(!cloneLink.isEmpty());
        Preconditions.checkNotNull(directory);
        Preconditions.checkArgument(directory.exists());
        Preconditions.checkArgument(directory.isDirectory());

        return cloningExecutor.submit(() -> {
            GithubCloneRepoLink githubCloneRepoLink = null;

            try {
                githubCloneRepoLink = new GithubCloneRepoLink(cloneLink);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (githubCloneRepoLink == null) return false;

            String repoName = githubCloneRepoLink.getRepository();
            File saveDirectory = OsUtil.buildFile(directory.getAbsolutePath(), repoName);

            if (!saveDirectory.exists() && !saveDirectory.mkdirs()) return false;
            if (!OsUtil.isBinaryInstalled(GIT)) return false;

            try {
                String command = GIT + " "
                        + "clone" + " " + githubCloneRepoLink.getLink()
                        + " " + saveDirectory.getAbsolutePath();
                ProcessUtil.runAndWaitForProcess(command);
            } catch (Exception ignored) {
                return false;
            }

            return true;
        });
    }
}
