public class engine {


    public static void main(String[] __args) {
        MultipleEntryDetector multipleEntryDetector = MultipleEntryDetector.MultipleEntryDetectorUnsortedFactory("logins0.csv");
        multipleEntryDetector.setDateAfter("2015-11-30 23:17:48");
        multipleEntryDetector.setDateBefore("2015-11-30 23:11:40");
        multipleEntryDetector.collect();
    }

}
