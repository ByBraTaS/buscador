package rmartin.ctf.search;

public class SearchRequest {
    private String text;
    private String search;

    public SearchRequest() {}

    public SearchRequest(String text, String search) {
        this.text = text;
        this.search = search;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    @Override
    public String toString() {
        return "SearchRequest{" +
                "text='" + text + '\'' +
                ", search='" + search + '\'' +
                '}';
    }
}
