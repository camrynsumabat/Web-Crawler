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
    private static final String DOMAIN = "cpp.edu";
    private static final int MAX_SITE = 50;
    private static final String REPORT_CSV = ".\\outlinks-report.csv";
    private HashSet<String> links;

    public WebCrawler() {
        links = new HashSet<String>();
    }

    public void getPageLinks(String URL, int depth, CSVPrinter csvPrinter) {

        if (!links.contains(URL) && links.size() < MAX_SITE) {
            try {
                Document document = Jsoup.connect(URL).get();

                // restrict domain
                if(URL.contains(DOMAIN)) {
                    if (links.add(URL)) {
                        System.out.println(URL);
                    }

                    Elements linksOnPage = document.select("a[href]");
                    Elements relativeLinks = document.select("a[href*=#]");

                    int absLinksOnPageCount = linksOnPage.size() - relativeLinks.size();

                    HashSet<String> absLinksOnPage = new HashSet<>();
                    HashSet<String> updatedAbsLinksOnPage = new HashSet<>();

                    //check if relative
                    for (Element link : linksOnPage) {
                        if (!relativeLinks.contains(link)) {
                            String absLink = link.attr("href");
                            absLinksOnPage.add(absLink);
                        }
                    }

                    for (String link : absLinksOnPage) {
                        if (link.startsWith("/") || (link.startsWith("www"))) {
                            if (link.startsWith("/"))
                                link = SEED_SITE + link;
                            if (link.startsWith("www"))
                                link = "http://" + link;
                        }
                        else if(link.startsWith("https://www")){

                        }
                        else {
                            link = URL;
                        }
                        updatedAbsLinksOnPage.add(link);
                    }

                    System.out.println("Number of outlinks: " + absLinksOnPageCount);
                    System.out.println("Depth: " + depth);
                    System.out.println();

                    csvPrinter.printRecord(URL, absLinksOnPageCount);
                    depth++;

                    // For each URL, recurse
                    for (String absLink : updatedAbsLinksOnPage) {
                        getPageLinks(absLink, depth, csvPrinter);
                    }
                } else {
                    System.err.println("Issue crawling " + URL + ": Not in the domain restriction for " + DOMAIN);
                }
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
    }

    /*
    // Extracts pure text from html document and writes it to the repository folder
    public void writeFile(Document document) throws IOException {
        String filename = "site" + links.size() + ".txt";
        FileWriter fw = new FileWriter( REPOSITORY_FOLDER + " " + LANGUAGE + "\\" + filename);
        fw.write(document.text());
        fw.close();
    }
     */

    public static void main(String[] args) {

        try {
            // new File(REPOSITORY_FOLDER + " " + LANGUAGE).mkdir();     // creates repository folder

            BufferedWriter writer = Files.newBufferedWriter(Paths.get(REPORT_CSV)); // creates csv file

            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("URL", "Outlinks")); // creates headers in report.csv

            new WebCrawler().getPageLinks(SEED_SITE, 0, csvPrinter);

            csvPrinter.flush(); // clears buffer
        } catch (IOException e) {
            System.err.println("Cannot write outlinks-report.csv file");
        }

    }
}
