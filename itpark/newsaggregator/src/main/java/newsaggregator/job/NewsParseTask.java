package newsaggregator.job;

import newsaggregator.model.nosql.News;
import newsaggregator.service.NewsService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NewsParseTask {

    NewsService newsService;

    @Scheduled(fixedDelay = 10000)
    public void parseNews() {
        String url = "https://lenta.ru/";

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Chrome")
                    .timeout(5000)
                    .referrer("https://google.com")
                    .get();
            Elements news = doc.getElementsByClass("storylink");
            for (Element el: news) {
                String title = el.ownText();
                if (!newsService.isExists(title)) {
                    News obj = new News();
                    obj.setTitle(title);
                    newsService.save(obj);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
