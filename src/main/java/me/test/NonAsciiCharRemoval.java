package me.test;

import java.util.HashMap;
import java.util.Map;

public class NonAsciiCharRemoval {


    public static void testControlCharsRemoval() {
        System.out.println("==> Starting [testControlCharsRemoval] at " + CommonConfig.sdf.format(System.currentTimeMillis()));

        String originalText = "1ˢᵗ“2ⁿᵈ”3ʳᵈ‘4’5 zzz María Gómez DD\nCC\rBB\u00000\u00011\u00022\u00033\u00044\u00055\u00066\u00077\u00088\t9 a\u000bb\u000ccd\u000ee\u000ff\u0010g\u0011h\u0012i\u0013j\u0014k\u0015l\u0016m\u0017n\u0018o\u0019p\u001aq\u001br\u001cs\u001dt\u001eu\u001fv\u0080w\u0081x\u0082y\u0083z\u0084A\u0085B\u0086C\u0087D\u0088E\u0089F\u008aG\u008bH\u008cI\u008dJ\u008eK\u008fL\u0090M\u0091N\u0092O\u0093P\u0094Q\u0095R\u0096S\u0097T\u0098U\u0099V\u009aW\u009bX\u009cY\u009dZ\u009eAV\u009fMW";
        String processedText = replaceControlCharsAndMsWordCharsByPrintableChars(originalText);
        System.out.println("(1) Original text: \n[" + originalText + "]\n");
        System.out.println("(1) Processed text: \n[" + processedText + "]");

        System.out.println("--------------------------------------------------------");
        originalText = "a\u007Fb\u0081c\u008Dd\u008Fe\u0090f\u009Dg\u201Ah\u201Ei\u2026j\u02C6k\u2030l\u2039m\u2018n\u2019o\u201Cp\u201Dq\u2022r\u2013s\u2014t\u02DCu\u2122v\u203Aw\u00A1x\u00A9y\u00AAz\u00ABA\u00ACB\u00ADC\u00AED\u00B7E\u00BAF\u00BBG\u00BCH\u00BDI\u00BEJ\u00BFK\u00D7L\u00F7M\u2192N\u2190O\u2264P\u2265Q\u2260R\u2032S\u2033";
        processedText = replaceControlCharsAndMsWordCharsByPrintableChars(originalText);
        System.out.println("(2) Original text: \n[" + originalText + "]\n");
        System.out.println("(2) Processed text: \n[" + processedText + "]");

        System.out.println("\n<== Ending [testControlCharsRemoval] at " + CommonConfig.sdf.format(System.currentTimeMillis()));
    }


    private static String replaceControlCharsAndMsWordCharsByPrintableChars(String inputString) {
        Map<String, String> stringReplacements = new HashMap<>();
        stringReplacements.put("1\u02E2\u1D57", "1st"); // '1ˢᵗ'
        stringReplacements.put("2\u207F\u1D48", "2nd"); // '2ⁿᵈ'
        stringReplacements.put("3\u02B3\u1D48", "3rd"); // '3ʳᵈ'

        stringReplacements.put("[\\p{Cc}&&[^\r\n\t]]", ""); // Control characters except whitespaces - carriage return, newline, tab

        stringReplacements.put("\u007F", ""); // DEL character
        stringReplacements.put("[\u0081\u008D\u008F\u0090\u009D]", ""); // Some unused character codes
        stringReplacements.put("\u201A", "'");  // Single low quotation [‚]
        stringReplacements.put("\u201E", "\"");  // Double low quotation [„]
        stringReplacements.put("\u2026", "...");  // Ellipsis […]
        stringReplacements.put("\u02C6", "^");  // Circumflex accent [ˆ]
        stringReplacements.put("\u2030", "%o");  // Per mille sign [‰]
        stringReplacements.put("\u2039", "<");  // Single left-pointing angle quotation [‹]
        stringReplacements.put("\u2018", "'");  // Left single quotation mark [‘]
        stringReplacements.put("\u2019", "'");  // Right single quotation mark [’]
        stringReplacements.put("\u201C", "\"");  // Left double quotation mark [“]
        stringReplacements.put("\u201D", "\"");  // Right double quotation mark [”]
        stringReplacements.put("\u2022", "*");  // Bullet [•]
        stringReplacements.put("\u2013", "-");  // En dash [–]
        stringReplacements.put("\u2014", "-");  // Em dash [—]
        stringReplacements.put("\u02DC", "~");  // Small tilde [˜]
        stringReplacements.put("\u2122", "(TM)");  // Trade mark [™]
        stringReplacements.put("\u203A", ">");  // Single right-pointing angle quotation [›]
        stringReplacements.put("\u00A1", "!");  // Inverted exclamation [¡]
        stringReplacements.put("\u00A9", "(C)");  // Copyright [©]
        stringReplacements.put("\u00AA", "a");  // Feminine ordinal indicator [ª]
        stringReplacements.put("\u00AB", "<<");  // Left double angle quotes [«]
        stringReplacements.put("\u00AC", "-");  // Negation [¬]
        stringReplacements.put("\u00AD", "-");  // Soft hyphen [­SHY]
        stringReplacements.put("\u00AE", "(R)");  // Registered trade mark [®]
        stringReplacements.put("\u00B7", ",");  // Middle dot - Georgian comma [·]
        stringReplacements.put("\u00BA", "o");  // Masculine ordinal indicator [º]
        stringReplacements.put("\u00BB", ">>");  // Right double angle quotes [»]
        stringReplacements.put("\u00BC", "1/4");  // Fraction one quarter [¼]
        stringReplacements.put("\u00BD", "1/2");  // Fraction one half [½]
        stringReplacements.put("\u00BE", "3/4");  // Fraction three quarters [¾]
        stringReplacements.put("\u00BF", "?");  // Inverted question [¿]
        stringReplacements.put("\u00D7", "x");  // Multiplication sign [×]
        stringReplacements.put("\u00F7", "/");  // Division sign [÷]
        stringReplacements.put("\u2192", "->");  // Right Arrow [→]
        stringReplacements.put("\u2190", "<-");  // Left Arrow [←]
        stringReplacements.put("\u2264", "<=");  // Less Than or Equal To [≤]
        stringReplacements.put("\u2265", ">=");  // Greater Than or Equal To [≥]
        stringReplacements.put("\u2260", "!=");  // Not Equal To [≠]
        stringReplacements.put("\u2032", "'");  // Single Prime [′]
        stringReplacements.put("\u2033", "\"");  // Double Prime [″]

        String result = inputString;
        for (Map.Entry<String, String> entry : stringReplacements.entrySet()) {
            result = result.replaceAll(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
