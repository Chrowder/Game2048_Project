//
//package controller;
//
//import entity.Score;
//import net.dto.LeaderboardEntry;
//import org.springframework.http.MediaType;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import repository.ScoreRepository;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/scores")
//@CrossOrigin
//public class ScoreController {
//
//    private final ScoreRepository repo;
//    public ScoreController(ScoreRepository r){ this.repo = r; }
//
//    /** JSON 列表（供 Swing 调用） */
//    @GetMapping(value = "/json", produces = MediaType.APPLICATION_JSON_VALUE)
//    public List<LeaderboardEntry> json() {
//        return repo.findTop20ByOrderByScoreDescTimeAsc()
//                .stream()
//                .map(s -> new LeaderboardEntry(s.getName(), s.getScore()))
//                .toList();
//    }
//
//    /** HTML 视图（浏览器访问） */
//    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
//    public String html(Model model) {
//        model.addAttribute("scores", json());
//        return "leaderboard";           // templates/leaderboard.html
//    }
//
//    @PostMapping
//    public void add(@RequestBody LeaderboardEntry dto) {
//        repo.save(new Score(dto.name(), dto.score()));
//    }
//}
