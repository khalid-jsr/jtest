import me.test.*;


public class Main {
    public static void main(String[] args) {
//        NonAsciiCharRemoval.testControlCharsRemoval();
//        HashTest.hashPassword("campus123", "xMjnq");
        //System.out.println(Guid.getGuid());
//        System.out.println(Guid.addHyphensToUUID("c4e0b3e0-7b1b-4b1b-8b1b-9b1b1b1b1b1b"));
        System.out.println("Original: [B0E477D57B514A5386FCDC7452946AA8], formatted: [" +Guid.formatUUID("B0E477D57B514A5386FCDC7452946AA8") + "]");
        System.out.println("Original: [b0e477d57b514a5386fcdc7452946aa8], formatted: [" +Guid.formatUUID("b0e477d57b514a5386fcdc7452946aa8") + "]");
        System.out.println("Original: [e91aacdb-2416-4bb6-80b5-b80c51be4a0c], formatted: [" +Guid.formatUUID("e91aacdb-2416-4bb6-80b5-b80c51be4a0c") + "]");
        System.out.println("Original: [E91AACDB-2416-4BB6-80B5-B80C51BE4A0C], formatted: [" +Guid.formatUUID("E91AACDB-2416-4BB6-80B5-B80C51BE4A0C") + "]");
        System.out.println("Original: [E91AACDB-2416-4BB6-80B5-B80C51B], formatted: [" + Guid.formatUUID("E91AACDB-2416-4BB6-80B5-B80C51B") + "]");
        System.out.println("Original: [NULL], formatted: [" + Guid.formatUUID(null) + "]");
    }

}
