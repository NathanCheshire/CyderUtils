package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.BletchyThread;
import cyder.threads.CyderThreadRunner;
import cyder.utilities.IOUtil;
import cyder.utilities.OSUtil;
import cyder.utilities.StringUtil;
import cyder.utilities.YoutubeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * A handler for commands that play audio.
 */
public class PlayAudioHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private PlayAudioHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Handle({"heyya", "windows", "lightsaber", "xbox", "startrek", "toystory",
            "logic", "18002738255", "xxx", "blackpanther", "chadwickboseman", "f17", "play"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().inputWithoutSpacesIs("heyya")) {
            IOUtil.playAudio("static/audio/hey.mp3");
        } else if (getInputHandler().commandIs("windows")) {
            IOUtil.playAudio("static/audio/windows.mp3");
        } else if (getInputHandler().commandIs("lightsaber")) {
            IOUtil.playAudio("static/audio/Lightsaber.mp3");
        } else if (getInputHandler().commandIs("xbox")) {
            IOUtil.playAudio("static/audio/xbox.mp3");
        } else if (getInputHandler().commandIs("startrek")) {
            IOUtil.playAudio("static/audio/StarTrek.mp3");
        } else if (getInputHandler().commandIs("toystory")) {
            IOUtil.playAudio("static/audio/theclaw.mp3");
        } else if (getInputHandler().commandIs("logic")) {
            IOUtil.playAudio("static/audio/commando.mp3");
        } else if (getInputHandler().getCommand()
                .replace("-", "").equals("18002738255")) {
            IOUtil.playAudio("static/audio/1800.mp3");
        } else if (getInputHandler().commandIs("xxx")) {
            CyderIcons.setCurrentCyderIcon(CyderIcons.xxxIcon);
            ConsoleFrame.INSTANCE.getConsoleCyderFrame()
                    .setIconImage(new ImageIcon("static/pictures/print/x.png").getImage());
            IOUtil.playAudio(OSUtil.buildPath("static", "audio", "x.mp3"));
        } else if (getInputHandler().inputWithoutSpacesIs("blackpanther")
                || getInputHandler().inputWithoutSpacesIs("chadwickboseman")) {
            CyderThreadRunner.submit(() -> {
                getInputHandler().getOutputArea().setText("");

                IOUtil.playAudio(OSUtil.buildPath("static", "audio", "allthestars.mp3"));
                getInputHandler().getOutputArea().setFont(new Font("BEYNO",
                        Font.BOLD, getInputHandler().getOutputArea().getFont().getSize()));
                BletchyThread.bletchy("RIP CHADWICK BOSEMAN",
                        false, 15, false);

                try {
                    Thread.sleep(4000);
                } catch (Exception e) {
                    ExceptionHandler.silentHandle(e);
                }

                getInputHandler().getOutputArea().setFont(ConsoleFrame.INSTANCE.generateUserFont());
            }, "Chadwick Boseman");
        } else if (getInputHandler().commandIs("f17")) {
            if (getInputHandler().getRobot() != null) {
                getInputHandler().getRobot().keyPress(KeyEvent.VK_F17);
            } else {
                getInputHandler().println("Mr. Robot didn't start :(");
            }
        } else if (getInputHandler().commandIs("play")) {
            if (StringUtil.isNull(getInputHandler().argsToString())) {
                getInputHandler().println("Play command usage: Play [video_url/playlist_url/search query]");
            }

            CyderThreadRunner.submit(() -> {
                String url = getInputHandler().argsToString();

                if (YoutubeUtil.isPlaylistUrl(url)) {
                    YoutubeUtil.downloadPlaylist(url);
                } else {
                    String extractedUuid = getInputHandler().argsToString()
                            .replace(CyderUrls.YOUTUBE_VIDEO_HEADER, "");

                    if (extractedUuid.replace(" ", "").length() != 11) {
                        getInputHandler().println("Searching youtube for: " + url);
                        String uuid = YoutubeUtil.getFirstUUID(url);
                        url = CyderUrls.YOUTUBE_VIDEO_HEADER + uuid;
                    }

                    YoutubeUtil.downloadVideo(url);
                }
            }, "YouTube Download Initializer");
        } else {
            ret = false;
        }

        return ret;
    }
}