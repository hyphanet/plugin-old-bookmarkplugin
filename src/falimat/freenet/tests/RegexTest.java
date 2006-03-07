package falimat.freenet.tests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import falimat.freenet.network.RegExes;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class RegexTest extends TestCase {

    public void testFreenetUri() {
        Pattern pattern = RegExes.FREENET_URI;

        assertMatches(pattern, "KSK@test.txt");
        assertMatches(pattern, "freenet:KSK@test.txt");

        assertMatches(pattern,
                "SSK@uw8TPvhHbWe7GBH-PKMXy9A41w2hdMQZSY-RRrDBaiQ,AQnmRFKMOHx2uRulAfvjThGWWi~MbT521w1rmNuLVgk,AQABAAE/hack-a-bike///");
        assertMatches(
                pattern,
                "freenet:SSK@uw8TPvhHbWe7GBH-PKMXy9A41w2hdMQZSY-RRrDBaiQ,AQnmRFKMOHx2uRulAfvjThGWWi~MbT521w1rmNuLVgk,AQABAAE/hack-a-bike///");

        assertMatches(pattern,
                "CHK@INMq6ryw8nYcxwMMSdKd~pSMI26rlVWFbFFD~ikFv1c,-jzPOS4lNJl62IY2QjHzfGMo0gOgqCU28A1AF0a48Ps,AAEC--8");
        assertMatches(pattern,
                "freenet:CHK@INMq6ryw8nYcxwMMSdKd~pSMI26rlVWFbFFD~ikFv1c,-jzPOS4lNJl62IY2QjHzfGMo0gOgqCU28A1AF0a48Ps,AAEC--8");

        assertNotMatches(pattern, "http://www.google.com");
        assertNotMatches(pattern, "GSG@9");
    }

    public void testFproxyUri() {
        Pattern pattern = RegExes.FPROXY_URI;

        assertMatches(pattern, "http://localhost:8888/KSK@test.txt");
        assertMatches(
                pattern,
                "http://192.168.0.1/CHK@INMq6ryw8nYcxwMMSdKd~pSMI26rlVWFbFFD~ikFv1c,-jzPOS4lNJl62IY2QjHzfGMo0gOgqCU28A1AF0a48Ps,AAEC--8");
        assertMatches(
                pattern,
                "https://192.168.0.1/SSK@uw8TPvhHbWe7GBH-PKMXy9A41w2hdMQZSY-RRrDBaiQ,AQnmRFKMOHx2uRulAfvjThGWWi~MbT521w1rmNuLVgk,AQABAAE/hack-a-bike///");

        assertNotMatches(pattern, "http://www.google.com");
        assertNotMatches(pattern, "http://192.168.0.1/KSK@");
    }
    
    public void testTags() {
        Pattern pattern = RegExes.TAGS;
        assertMatches(pattern, "test");
        assertMatches(pattern, "abc def");
        assertMatches(pattern, "abc def ghi");
        assertMatches(pattern, "abc, def");
        assertMatches(pattern, "abc, def,ghi");
        
        assertNotMatches(pattern, "Lצהצה.-");
        assertNotMatches(pattern, " ");
    }

    private void assertMatches(Pattern pattern, String string) {
        Matcher matcher = pattern.matcher(string);
        if (!matcher.matches()) {
            throw new AssertionFailedError("String '" + string + "' does not match pattern '" + pattern.pattern() + "'");
        }
    }

    private void assertNotMatches(Pattern pattern, String string) {
        Matcher matcher = pattern.matcher(string);
        if (matcher.matches()) {
            throw new AssertionFailedError("String '" + string + "' unexpectedly matches pattern '" + pattern.pattern()
                    + "' but it shouldn't");
        }
    }
}
