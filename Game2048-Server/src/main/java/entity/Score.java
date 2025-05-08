
package entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Score {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int score;
    private LocalDateTime time = LocalDateTime.now();

    public Score(){}
    public Score(String n,int s){name=n;score=s;}

    public String getName(){return name;}
    public int getScore(){return score;}
    public LocalDateTime getTime(){return time;}
}
