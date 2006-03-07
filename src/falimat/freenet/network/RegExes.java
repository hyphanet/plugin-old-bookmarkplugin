package falimat.freenet.network;

import java.util.regex.Pattern;

public class RegExes {
    private final static Pattern FREENET_KEY = Pattern.compile("(?:CHK|SSK|KSK)@.+");

    public final static Pattern FPROXY_URI = Pattern.compile("^https?\\:.*/" + FREENET_KEY + "$");

    public final static Pattern FREENET_URI = Pattern.compile("^(freenet\\:)?" + FREENET_KEY + "$");

    public final static Pattern TAG = Pattern.compile("[a-z]+");

    public final static Pattern TAGS = Pattern.compile("^(?:" + TAG + "[\\s,]*)*" + TAG + "$");

    public static final Pattern SSK_KEY = Pattern.compile("SSK@.+");

    public static final Pattern CHANNEL_URI = Pattern.compile(SSK_KEY+"/.+-[0-9]+");

    public static final Pattern INTEGER = Pattern.compile("[0-9]+");

}
