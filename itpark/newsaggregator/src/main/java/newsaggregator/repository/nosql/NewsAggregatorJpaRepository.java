package newsaggregator.repository.nosql;

import newsaggregator.model.nosql.News;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.*;
import java.util.Random;

@Entity
public interface NewsAggregatorJpaRepository extends JpaRepository<News, Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    Long id = new Random().nextLong();
}
