import com.sun.istack.internal.NotNull;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertTrue;

public class TimeFilterTest {
    private static String DATEFORMAT = "yyyy-MM-dd hh:mm:ss";

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
            System.out.println(date);
            System.out.println(dateAfter);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.before(dateAfter);
    }

    private boolean timeFilter(String dateInString, String dateBeforeInString, String dateAfterInString) {
        //String formattedDateString = formatter.format(date);
        return after(dateInString, dateBeforeInString) && before(dateInString, dateAfterInString);
    }

    @Test
    public void TimeFilterTest() {
        assertTrue(timeFilter("2015-11-30 23:11:40", "2015-11-30 22:11:40", "2015-12-01 00:11:46"));
    }
}
