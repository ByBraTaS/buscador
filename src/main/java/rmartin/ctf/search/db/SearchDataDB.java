package rmartin.ctf.search.db;

import javax.persistence.*;

@Entity
public class SearchDataDB {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String ip;

    @Column(length = 65535, columnDefinition = "text")
    private String text;

    private String regex;

    public SearchDataDB() {
    }

    public SearchDataDB(String ip, String text, String regex) {
        this.ip = ip;
        this.text = text;
        this.regex = regex;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }


}
