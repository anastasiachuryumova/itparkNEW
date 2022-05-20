package newsaggregator.service.impl;

import newsaggregator.model.nosql.News;
import newsaggregator.repository.nosql.NewsAggregatorJpaRepository;
import newsaggregator.service.NewsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsServiceImpl implements NewsService {

    NewsAggregatorJpaRepository newsRepository;

    @Override
    public void save(News news) {
        newsRepository.save(new newsaggregator.model.nosql.News());
    }

    @Override
    public boolean isExists(String title) {
        List<newsaggregator.model.nosql.News> allNews = newsRepository.findAll();
        for (newsaggregator.model.nosql.News news: allNews) {
            if (news.getTitle().equals(title)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<News> getAllNews(String title) {
        return newsRepository.findAll();
    }
}
