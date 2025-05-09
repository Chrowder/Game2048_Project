package controller;

import entity.Score;
import net.dto.LeaderboardEntry;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import repository.ScoreRepository;

import java.util.List;

@RestController
@RequestMapping("/scores")
@CrossOrigin
public class ScoreApiController {

    private final ScoreRepository repo;
    public ScoreApiController(ScoreRepository r){ this.repo = r; }

    /** POST /scores  upload score */
    @PostMapping
    public void add(@RequestBody LeaderboardEntry dto){
        repo.save(new Score(dto.name(), dto.score()));
    }

    /** GET /scores/json  return JSON list */
    @GetMapping(value = "/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<LeaderboardEntry> topJson(){
        return repo.findTop20ByOrderByScoreDescTimeAsc()
                .stream()
                .map(s -> new LeaderboardEntry(s.getName(), s.getScore()))
                .toList();
    }
}
