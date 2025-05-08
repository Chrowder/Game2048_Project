package controller;

import net.dto.LeaderboardEntry;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import repository.ScoreRepository;

import java.util.List;

@Controller                   // **不是** RestController
@CrossOrigin
public class LeaderboardPageController {

    private final ScoreRepository repo;
    public LeaderboardPageController(ScoreRepository r){ this.repo = r; }

    /** GET /scores  返回美化后的排行榜页面 */
    @GetMapping(value = "/scores", produces = MediaType.TEXT_HTML_VALUE)
    public String leaderboard(Model model){
        List<LeaderboardEntry> list = repo.findTop20ByOrderByScoreDescTimeAsc()
                .stream()
                .map(s -> new LeaderboardEntry(s.getName(), s.getScore()))
                .toList();
        model.addAttribute("scores", list);
        return "leaderboard";           // ↔ templates/leaderboard.html
    }
}
