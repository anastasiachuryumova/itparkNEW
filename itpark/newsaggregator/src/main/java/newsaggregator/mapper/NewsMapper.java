package newsaggregator.mapper;

import org.mapstruct.Mapper;

@Mapper
public interface NewsMapper extends AbstractMapper<newsaggregator.model.sql.News, newsaggregator.model.nosql.News>{
}
