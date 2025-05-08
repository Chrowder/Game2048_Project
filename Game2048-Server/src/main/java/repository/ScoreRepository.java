
package repository;

import entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScoreRepository extends JpaRepository<Score,Long>{
    List<Score> findTop20ByOrderByScoreDescTimeAsc();
}
