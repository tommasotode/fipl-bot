import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Scraper {
    private static final String URL = "https://www.powerliftingitalia-fipl.it/";

    public static String getCompetitions()
    {
        String res = "";
        try {
            List<Comp> competitions = scrapeComps();
            for (Comp competition : competitions) {
                res += "Gara: " + competition.getName() + "\n";
                res += "Data: " + competition.getDate() + "\n";
                res += "Link: " + competition.getLink() + "\n\n";
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static List<Comp> scrapeComps() throws IOException {
        Document doc = Jsoup.connect(URL).get();
        List<Comp> competitions = new ArrayList<>();

        Elements competitionElements = doc.select("section .riga .colonna-1-4.Text14Black");
        for (Element c : competitionElements) {
            String name = c.select("h1.Text14Black").text();

            Elements grid = c.select("table tbody tr");
            String date = grid.get(1).select("td").text();

            String link = URL;
            try {
                link += grid.get(3).select("a").attr("href");
            } catch (Exception e) {}

            link = link.replace(" ", "%20");
            competitions.add(new Comp(name, date, link));
        }

        return competitions;
    }

    static class Comp {
        private String name;
        private String date;
        private String link;

        public Comp(String name, String date, String link) {
            this.name = name;
            this.date = date;
            this.link = link;
        }

        public String getName() {
            return name;
        }

        public String getDate() {
            return date;
        }

        public String getLink() {
            return link;
        }
    }
}