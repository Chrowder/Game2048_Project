
package controller;

import entity.Score;
import net.dto.LeaderboardEntry;
import org.springframework.web.bind.annotation.*;
import repository.ScoreRepository;

import java.util.List;

@RestController
@RequestMapping("/scores")
@CrossOrigin
public class ScoreController {
    private final ScoreRepository repo;
    public ScoreController(ScoreRepository r){this.repo=r;}

    @GetMapping
    public List<LeaderboardEntry> top(){
        return repo.findTop20ByOrderByScoreDescTimeAsc()
                   .stream()
                   .map(s->new LeaderboardEntry(s.getName(),s.getScore()))
                   .toList();
    }

    @PostMapping
    public void add(@RequestBody LeaderboardEntry dto){
        repo.save(new Score(dto.name(),dto.score()));
    }
}
