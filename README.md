# Web-Crawler
*Project 1 for CS 4250 Web Search Recommender Systems | Fall 2021 | Dr. Ben Steichen* <br>
A web crawler written in Java and using the Jsoup library.<br>

## Capabilities
- Crawls 500 pages in a specified language
- Seed URLs and language codes are provided for English, Spanish, and French
- Error handling: Fixes or removes malformed links, checks for specified language code
- Downloads each page's content (pure text, no html tags) into a .txt file saved in a folder called `repository-[ language code ]`
- Each URL and the number of outlinks on the page are saved to a `report.csv` file<br>

## Resources
https://mkyong.com/java/jsoup-basic-web-crawler-example/ <br>
https://stackoverflow.com/questions/10621403/jsoup-malformed-url <br>
