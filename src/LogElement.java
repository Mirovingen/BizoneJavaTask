import java.util.Date;

public class LogElement {
    private final String name;
    private final String IP;
    private final Date date;

    public LogElement(String name, String ip, Date date){
        this.name = name;
        this.IP = ip;
        this.date = date;
    }

    public LogElement(LogElement el){
        this.name = el.name;
        this.IP = el.IP;
        this.date = el.date;
    }

    public static LogElement LogElementFactory(String name, String ip, Date date){
        return new LogElement(name, ip, date);
    }

    public String getName() {
        return name;
    }

    public String getIP() {
        return IP;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "LogElement{" +
                "name='" + name + '\'' +
                ", IP='" + IP + '\'' +
                ", date=" + date +
                '}';
    }
}
