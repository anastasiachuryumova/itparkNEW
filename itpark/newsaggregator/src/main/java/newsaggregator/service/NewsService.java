package newsaggregator.service;

import newsaggregator.model.nosql.News;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface NewsService {

    public void save(News news);
    public boolean isExists(String title);
    public List<News> getAllNews(String title);

}
