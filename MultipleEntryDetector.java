import com.sun.istack.internal.NotNull;
import org.junit.Test;

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

import static org.junit.Assert.assertTrue;

public class MultipleEntryDetector {
    private static String SEPARATOR = ",";
    private static String DATEFORMAT = "yyyy-MM-dd hh:mm:ss";
    private static Path PATH;

    private boolean SORTED;
    private String DateBefore;
    private String DateAfter;


    public static String getSEPARATOR() {
        return SEPARATOR;
    }

    public static void setSEPARATOR(String SEPARATOR) {
        MultipleEntryDetector.SEPARATOR = SEPARATOR;
    }

    public static String getDATEFORMAT() {
        return DATEFORMAT;
    }

    public static void setDATEFORMAT(String DATEFORMAT) {
        MultipleEntryDetector.DATEFORMAT = DATEFORMAT;
    }

    public static Path getPATH() {
        return PATH;
    }

    public static void setPATH(String PATH) {
        MultipleEntryDetector.PATH = Paths.get(PATH);
    }

    public String getDateBefore() {
        return DateBefore;
    }

    public void setDateBefore(String dateBefore) {
        DateBefore = dateBefore;
    }

    public String getDateAfter() {
        return DateAfter;
    }

    public void setDateAfter(String dateAfter) {
        DateAfter = dateAfter;
    }

    private MultipleEntryDetector(String dateBefore, String dateAfter, boolean sorted, Path path) {
        DateBefore = dateBefore;
        DateAfter = dateAfter;
        SORTED = sorted;
        PATH = path;
    }

    public static MultipleEntryDetector MultipleEntryDetectorUnsortedFactory(String Path) {
        return new MultipleEntryDetector(LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ofPattern(DATEFORMAT)),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATEFORMAT)), false, Paths.get(Path));
    }

    public static MultipleEntryDetector MultipleEntryDetectorSortedFactory(String Path) {
        return new MultipleEntryDetector(LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ofPattern(DATEFORMAT)),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATEFORMAT)), true, Paths.get(Path));
    }

    private boolean after(@NotNull String dateInString, @NotNull String dateBeforeInString) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATEFORMAT, Locale.ENGLISH);

        Date date = null;
        Date dateBefore = null;
        try {
            date = formatter.parse(dateInString);
            dateBefore = formatter.parse(dateBeforeInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.after(dateBefore);
    }

    private boolean before(@NotNull String dateInString, @NotNull String dateAfterInString) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATEFORMAT, Locale.ENGLISH);

        Date date = null;
        Date dateAfter = null;
        try {
            date = formatter.parse(dateInString);
            dateAfter = formatter.parse(dateAfterInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.before(dateAfter);
    }

    private boolean timeFilter(String dateInString, String dateBeforeInString, String dateAfterInString) {
        //String formattedDateString = formatter.format(date);
        return after(dateInString, dateBeforeInString) && before(dateInString, dateAfterInString);
    }

    private Map<String, List<String>> ifNoSortedLog() {
        try (Stream<String> stream = Files.lines(PATH)) {
            Map<String, List<String>> map = stream
                    .map(s -> s.replace("\"", ""))
                    .map((line) -> Arrays.asList(line.split(SEPARATOR)))
                    .filter(cell -> timeFilter(cell.get(2)/*.replace("\"", "")*/, DateBefore, DateAfter))
                    .collect(Collectors.toMap(el -> el.get(1), el -> Arrays.asList(el.get(0), el.get(2)), (s, a) -> {
                                List<String> b = new ArrayList<>(s);
                                b.addAll(Arrays.asList(a.get(0), a.get(1)));
                                return b;
                            })
                    );

            map.values().removeIf(value -> value.size() == 2);
            return map;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.<String, List<String>>emptyMap();

        }
    }

    private Map<String, List<String>> ifSortedLog() {
        try (Stream<String> stream = Files.lines(PATH)) {
            List<List<String>> list = stream
                    .map(s -> s.replace("\"", ""))
                    .map((line) -> Arrays.asList(line.split(SEPARATOR)))
                    .collect(Collectors.toList());

            Map<String, List<String>> map = new HashMap<>();
            for (List<String> temp : list) {
                if (before(temp.get(2), getDateBefore())) {
                    continue;
                }
                if (after(temp.get(2), getDateAfter())) {
                    break;
                }

                map.compute(temp.get(1), (k, v) -> {
                    if (map.containsKey(k)) {
                        List<String> b = new ArrayList<>(v);
                        b.addAll(Arrays.asList(temp.get(0), temp.get(2)));
                        return b;
                    } else {
                        return Arrays.asList(temp.get(0), temp.get(2));
                    }
                });
            }
            System.out.println(map);
            map.values().removeIf(value -> value.size() == 2);
            return map;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.<String, List<String>>emptyMap();
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
        Map<String, List<String>> map = SORTED ? ifSortedLog() : ifNoSortedLog();

        List<ArrayList<String>> outerList = new ArrayList<>();
        map.forEach((k, v) -> {
            ArrayList<String> outList = new ArrayList<>();
            outList.add(k);
            outList.add(v.get(1));
            outList.add(v.get(v.size() - 1));

            StringJoiner joiner = new StringJoiner(",");
            for (int i = 0; i < v.size(); i += 2) {
                joiner.add(v.get(i).concat(":").concat(v.get(i + 1)));
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
