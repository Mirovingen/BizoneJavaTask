import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultipleEntryDetectorWithDate {
    private static String SEPARATOR = ",";
    private static String DATEFORMAT = "yyyy-MM-dd hh:mm:ss";
    private static Path PATH;

    private Date DateBefore;
    private Date DateAfter;


    public static String getSEPARATOR() {
        return SEPARATOR;
    }

    public static void setSEPARATOR(String SEPARATOR) {
        MultipleEntryDetectorWithDate.SEPARATOR = SEPARATOR;
    }

    public static String getDATEFORMAT() {
        return DATEFORMAT;
    }

    public static void setDATEFORMAT(String DATEFORMAT) {
        MultipleEntryDetectorWithDate.DATEFORMAT = DATEFORMAT;
    }

    public static Path getPATH() {
        return PATH;
    }

    public static void setPATH(String PATH) {
        MultipleEntryDetectorWithDate.PATH = Paths.get(PATH);
    }

    public String getDateBefore() {
        return DateBefore.toString();
    }

    public void setDateBefore(String dateBefore) {
        DateBefore = convert2Date(dateBefore);
    }

    public String getDateAfter() {
        return DateAfter.toString();
    }

    public void setDateAfter(String dateAfter) {
        DateAfter = convert2Date(dateAfter);
    }

    private MultipleEntryDetectorWithDate(String dateBefore, String dateAfter, Path path) {
        DateBefore = convert2Date(dateBefore);
        DateAfter = convert2Date(dateAfter);
        PATH = path;
    }

    public static MultipleEntryDetectorWithDate MultipleEntryDetectorUnsortedFactory(String Path) {
        return new MultipleEntryDetectorWithDate(LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ofPattern(DATEFORMAT)),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATEFORMAT)), Paths.get(Path));
    }

    private boolean timeFilter(Date date, Date dateBefore, Date dateAfter) {
        //String formattedDateString = formatter.format(date);
        return date.after(dateBefore) && date.before(dateAfter);
    }

    private Date convert2Date(String dateInString){
        SimpleDateFormat formatter = new SimpleDateFormat(DATEFORMAT, Locale.ENGLISH);

        Date date = null;
        try {
            date = formatter.parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private Map<String, List<LogElement>> ifNoSortedLogv2() {
        try (Stream<String> stream = Files.newBufferedReader(PATH).lines()) {
            Map<String, List<LogElement>> map = stream
                    .map(s -> s.replace("\"", ""))
                    .map((line) -> Arrays.asList(line.split(SEPARATOR)))
                    .map((el) -> LogElement.LogElementFactory(el.get(0), el.get(1), convert2Date(el.get(2))))
                    .filter(cell -> timeFilter(cell.getDate(), DateBefore, DateAfter))
                    .collect(Collectors.toMap(LogElement::getIP, el -> Arrays.asList(el), (k,v) -> {
                        List<LogElement> b = new ArrayList<>(k);
                        b.addAll(v);
                        return b;
                    }));

            System.out.println(map);

            map.values().removeIf(value -> value.size() == 1);
            return map;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.<String, List<LogElement>>emptyMap();

        }
    }

    /*If needed*/
    private String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public void collect() {
        Map<String, List<LogElement>> map = ifNoSortedLogv2();

        List<ArrayList<String>> outerList = new ArrayList<>();
        map.forEach((k, v) -> {
            ArrayList<String> outList = new ArrayList<>();
            outList.add(k);
            outList.add(v.get(0).getDate().toString());
            outList.add(v.get(v.size() - 1).getDate().toString());

            StringJoiner joiner = new StringJoiner(",");
            for (LogElement el: v) {
                joiner.add(el.getName().concat(":").concat(el.getIP()));
            }
            outList.add(joiner.toString());
            outerList.add(outList);
        });

        File csvOutputFile = new File("output1hour.csv");
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            outerList.forEach(pw::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
