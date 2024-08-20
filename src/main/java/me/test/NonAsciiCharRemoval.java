package me.test;

import java.text.SimpleDateFormat;

public class NonAsciiCharRemoval {
    public static void testControlCharsRemoval() {
        System.out.println("==> Starting [testControlCharsRemoval] at " + CommonConfig.sdf.format(System.currentTimeMillis()));

        String originalText = "1“2”3‘4’5 zzz María Gómez DD\nCC\rBB\u00000\u00011\u00022\u00033\u00044\u00055\u00066\u00077\u00088\t9 a\u000bb\u000ccd\u000ee\u000ff\u0010g\u0011h\u0012i\u0013j\u0014k\u0015l\u0016m\u0017n\u0018o\u0019p\u001aq\u001br\u001cs\u001dt\u001eu\u001fv\u0080w\u0081x\u0082y\u0083z\u0084A\u0085B\u0086C\u0087D\u0088E\u0089F\u008aG\u008bH\u008cI\u008dJ\u008eK\u008fL\u0090M\u0091N\u0092O\u0093P\u0094Q\u0095R\u0096S\u0097T\u0098U\u0099V\u009aW\u009bX\u009cY\u009dZ\u009eAV\u009fMW";
        String processedText = replaceUnicodeControlCharsButNotWhitespace(originalText);
        System.out.println("Original text: \n[" + originalText + "]\n");
        System.out.println("Processed text: \n[" + processedText + "]");

        System.out.println("<== Ending [testControlCharsRemoval] at " + CommonConfig.sdf.format(System.currentTimeMillis()));
    }


    private static String replaceUnicodeControlCharsButNotWhitespace(String text) {
//        return text.replaceAll("[\\p{Cc}&&[^\\p{Z}]]", "");
        return text.replaceAll("[\\p{Cc}&&[^\r\n\t]]", "").replaceAll("[\u201C\u201D]", "\"").replaceAll("[\u2018\u2019]", "'");
    }
}
