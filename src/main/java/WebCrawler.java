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

public class WebCrawler {

    // english test seed
    private static final String SEED_SITE = "https://www.cpp.edu";
    private static final String LANGUAGE = "en";
    private static final String LANGUAGE_ALT = "en-US";

    // spanish test seed (need a better one, only 4 crawlable outlinks)
    // private static final String SEED_SITE = "https://www.latimes.com/espanol/eeuu/articulo/2021-09-29/opinion-mexico-es-un-campamento-patio-o-sala-de-espera-de-estados-unidos";
    // private static final String LANGUAGE = "es";
    // private static final String LANGUAGE_ALT = "es-US";

    // one more language
    // private static final String SEED_SITE = "";
    // private static final String LANGUAGE = "";

    private static final String REPORT_CSV = ".\\report.csv";
    private static final String REPOSITORY_FOLDER = ".\\repository";
    private static final int MAX_SITES = 20;
    private static final int MAX_DEPTH = 2;
    private HashSet<String> links;

    public WebCrawler() {
        links = new HashSet<String>();
    }

    public void getPageLinks(String URL, int depth, CSVPrinter csvPrinter) {

        // 2. Check that the URL has not been crawled yet and the page limit and depth has not been reached
        if (!links.contains(URL) && links.size() < MAX_SITES && depth < MAX_DEPTH) {
            try {

                // 3. Fetch HTML code, check language
                Document document = Jsoup.connect(URL).header("Accept-Language", LANGUAGE).get();
                Element html = document.select("html").first();
                String lang = html.attr("lang");

                if (lang.equals(LANGUAGE) || lang.equals(LANGUAGE_ALT)) {
                    // 4. If the URL has not been crawled yet, add it to the HashSet of links
                    if (links.add(URL)) {
                        System.out.println(URL);
                    }

                    // 5. Extract text content and write to file
                    writeFile(document);

                    // 6. Parse the HTML and extract other URLs on the page, removing relative URLs
                    Elements linksOnPage = document.select("a[href]");
                    Elements relativeLinks = document.select("a[href*=#]");

                    int absLinksOnPageCount = linksOnPage.size() - relativeLinks.size();

                    HashSet<String> absLinksOnPage = new HashSet<>();

                    for (Element link : linksOnPage) {
                        if (!relativeLinks.contains(link)) {
                            String absLink = link.attr("href");
                            absLinksOnPage.add(absLink);
                        }
                    }

                    // 7. Write absolute URL and number of outlinks to report.csv
                    System.out.println("Number of outlinks: " + absLinksOnPageCount);
                    System.out.println("Depth: " + depth);
                    System.out.println();

                    csvPrinter.printRecord(URL, absLinksOnPageCount);
                    depth++;

                    // 8. For each URL, go back to step 2
                    for (String absLink : absLinksOnPage) {
                        getPageLinks(absLink, depth, csvPrinter);
                    }
                } else {
                    System.err.println("For '" + URL + "': \n" + "Site language code: " + lang + "\nSearching for sites with language code: " + LANGUAGE + " or " + LANGUAGE_ALT );
                    System.err.println("This site does not match the desired language.\n");
                }
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
    }

    // Extracts pure text from html document and writes it to the repository folder
    public void writeFile(Document document) throws IOException {
        String filename = "site" + links.size() + ".txt";
        FileWriter fw = new FileWriter(REPOSITORY_FOLDER + "\\" + filename);
        fw.write(document.text());
        fw.close();
    }

    public static void main(String[] args) {

        try {
            new File(REPOSITORY_FOLDER).mkdir();     // creates repository folder

            BufferedWriter writer = Files.newBufferedWriter(Paths.get(REPORT_CSV)); // creates csv file

            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("URL", "Outlinks")); // creates headers in report.csv

            // 1. Start with the seed URL
            new WebCrawler().getPageLinks(SEED_SITE, 0, csvPrinter);

            csvPrinter.flush(); // clears buffer
        } catch (IOException e) {
            System.err.println("Cannot write report.csv file");
        }

    }
}
