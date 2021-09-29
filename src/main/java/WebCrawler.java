import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

// code adapted from https://mkyong.com/java/jsoup-basic-web-crawler-example/
public class WebCrawler {

    private static final int MAX_DEPTH = 2; // represents depth of link extraction
    private HashSet<String> links;
    private int max = 5; // change max number of links crawled here

    public WebCrawler() {
        links = new HashSet<String>();
    }

    public void getPageLinks(String URL, int depth) {

        // 4. Check if the URL has already been crawled and the page limit has not been reached
        if (!links.contains(URL) && links.size() < max && depth < MAX_DEPTH) {
            try {

                // 4a. If the URL has not been crawled yet, add it to the HashSet of links
                if (links.add(URL)) {
                    System.out.println(URL);
                }

                // 2. Fetch HTML code and write to file
                Document document = Jsoup.connect(URL).get();
                writeFile(document);

                // 3. Parse the HTML and extract other URLs on the page
                Elements linksOnPage = document.select("a[href]");
                System.out.println("Number of outlinks: " + linksOnPage.size());
                System.out.println("Depth: " + depth);
                System.out.println();
                depth++;

                // 5. For each URL, go back to step 4
                for (Element page : linksOnPage) {
                    getPageLinks(page.attr("abs:href"), depth);
                }
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
    }

    public void writeFile(Document document) throws IOException {
        String filename = document.title() + ".txt";
        FileWriter fw = new FileWriter(filename);
        fw.write(document.toString());
        fw.close();
    }

    public static void main(String[] args) {
        // 1. pick a seed URL
        new WebCrawler().getPageLinks("http://www.cpp.edu", 0); // change seed link here
    }
}
