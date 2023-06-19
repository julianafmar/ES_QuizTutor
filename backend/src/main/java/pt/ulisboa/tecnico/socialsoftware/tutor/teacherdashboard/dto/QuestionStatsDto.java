package pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.domain.QuestionStats;

import java.io.Serializable;

public class QuestionStatsDto implements Serializable {
    private Integer id;
    private int numAvailable;
    private int answeredQuestionsUnique;
    private float averageQuestionsAnswered;

    private int year;

    public QuestionStatsDto(QuestionStats questionStats) {
        this.id = questionStats.getId();
        this.numAvailable = questionStats.getNumAvailable();
        this.answeredQuestionsUnique = questionStats.getAnsweredQuestionsUnique();
        this.averageQuestionsAnswered = questionStats.getAverageQuestionsAnswered();
        this.year = questionStats.getCourseExecution().getYear();
    }

    public Integer getId() {
        return id;
    }

    public int getYear() {
        return year;
    }

    public int getNumAvailable() {
        return numAvailable;
    }

    public void setNumAvailable(int numAvailable) {
        this.numAvailable = numAvailable;
    }

    public int getAnsweredQuestionsUnique() {
        return answeredQuestionsUnique;
    }

    public void setAnsweredQuestionsUnique(int answeredQuestionsUnique) {
        this.answeredQuestionsUnique = answeredQuestionsUnique;
    }

    public float getAverageQuestionsAnswered() {
        return averageQuestionsAnswered;
    }

    public void setAverageQuestionsAnswered(float averageQuestionsAnswered) {
        this.averageQuestionsAnswered = averageQuestionsAnswered;
    }

    @Override
    public String toString() {
        return "QuestionStatsDto {id=" + id +
                ", numAvailable=" + this.getNumAvailable() +
                ", answeredQuestionsUnique=" + this.getAnsweredQuestionsUnique() +
                ", averageQuestionsAnswered=" + this.getAverageQuestionsAnswered() +
                "}";
    }
}