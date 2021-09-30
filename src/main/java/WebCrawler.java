import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

// code adapted from https://mkyong.com/java/jsoup-basic-web-crawler-example/
public class WebCrawler {

    private static final String SEED_SITE = "http://www.cpp.edu";
    private static final String LANGUAGE = "en";
    private static final String REPORT_CSV = ".\\report.csv";
    private static final String REPOSITORY_FOLDER = ".\\repository";
    private static final int MAX_SITES = 5;
    private static final int MAX_DEPTH = 2;
    private HashSet<String> links;

    public WebCrawler() {
        links = new HashSet<String>();
    }

    public void getPageLinks(String URL, int depth, CSVPrinter csvPrinter) {

        // 4. Check if the URL has already been crawled and the page limit has not been reached
        if (!links.contains(URL) && links.size() < MAX_SITES && depth < MAX_DEPTH) {
            try {

                // 4a. If the URL has not been crawled yet, add it to the HashSet of links
                if (links.add(URL)) {
                    System.out.println(URL);
                }

                // 2. Fetch HTML code, check language, and write to file
                Document document = Jsoup.connect(URL).header("Accept-Language", LANGUAGE).get();
                writeFile(document);

                // 3. Parse the HTML and extract other URLs on the page, then print URL and outlink count to csv
                Elements linksOnPage = document.select("a[href]");

                System.out.println("Number of outlinks: " + linksOnPage.size());
                System.out.println("Depth: " + depth);
                System.out.println();

                csvPrinter.printRecord(URL, linksOnPage.size());
                depth++;

                // 5. For each URL, go back to step 4
                for (Element page : linksOnPage) {
                    getPageLinks(page.attr("abs:href"), depth, csvPrinter);
                }
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
    }

    public void writeFile(Document document) throws IOException {
        String filename = document.title() + ".txt";
        FileWriter fw = new FileWriter(REPOSITORY_FOLDER + "\\" + filename);
        fw.write(document.text());
        fw.close();
    }

    public static void main(String[] args) {

        try {
            Path path = Path.of((REPOSITORY_FOLDER));

            new File(String.valueOf(path)).mkdir();     // creates repository folder if it doesn't exist

            BufferedWriter writer = Files.newBufferedWriter(Paths.get(REPORT_CSV));

            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("URL", "Outlinks"));

            // 1. pick a seed URL
            new WebCrawler().getPageLinks(SEED_SITE, 0, csvPrinter);

            csvPrinter.flush();
        } catch (IOException e) {
            System.err.println("Cannot write report.csv file");
        }

    }
}
