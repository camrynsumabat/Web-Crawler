import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;

// code adapted from https://mkyong.com/java/jsoup-basic-web-crawler-example/
public class WebCrawler {

    private HashSet<String> links;
    private int max = 5; // change max number of links crawled here

    public WebCrawler() {
        links = new HashSet<String>();
    }

    public void getPageLinks(String URL) {

        // 4. Check if the URL has already been crawled and the page limit has not been reached
        if (!links.contains(URL) && links.size() < max) {
            try {

                // 4a. If the URL has not been crawled yet, add it to the HashSet of links
                if (links.add(URL)) {
                    System.out.println(URL);
                }

                // 2. Fetch HTML code
                Document document = Jsoup.connect(URL).get();
                // System.out.println(document); // prints complete contextual page (html tags)

                // 3. Parse the HTML and extract other URLs on the page
                Elements linksOnPage = document.select("a[href]");
                System.out.println("Number of outlinks: " + linksOnPage.size());

                // 5. For each URL, go back to step 4
                for (Element page : linksOnPage) {
                    getPageLinks(page.attr("abs:href"));
                }
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        // 1. pick a seed URL
        new WebCrawler().getPageLinks("http://www.cpp.edu"); // change seed link here
    }
}
