import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Scraper {
    public static void scrapeCompetitions() {
        try {
            Document doc = Jsoup.connect("https://www.powerliftingitalia-fipl.it/").get();
            System.out.println(doc.title());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
