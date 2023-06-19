package pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.domain.QuizStats;

public class QuizStatsDto {
    
    private Integer id;

    private int numQuizzes;

    private int numUniqueAnsweredQuizzes;

    private float averageQuizzesSolved;

    private int year;

    public QuizStatsDto(QuizStats quizStats) {
        this.id = quizStats.getId();
        this.numQuizzes = quizStats.getNumQuizzes();
        this.numUniqueAnsweredQuizzes = quizStats.getNumUniqueAnsweredQuizzes();
        this.averageQuizzesSolved = quizStats.getAverageQuizzesSolved();
        this.year = quizStats.getCourseExecution().getYear();
    }

    public Integer getId() {
        return this.id;
    }

    public int getNumQuizzes() {
        return this.numQuizzes;
    }

    public void setNumQuizzes(int numQuizzes) {
        this.numQuizzes = numQuizzes;
    }

    public int getNumUniqueAnsweredQuizzes() {
        return this.numUniqueAnsweredQuizzes;
    }

    public void setNumUniqueAnsweredQuizzes(int numUniqueAnsweredQuizzes) {
        this.numUniqueAnsweredQuizzes = numUniqueAnsweredQuizzes;
    }

    public float getAverageQuizzesSolved() {
        return this.averageQuizzesSolved;
    }

    public void setAverageQuizzesSolved(float averageQuizzesSolved) {
        this.averageQuizzesSolved = averageQuizzesSolved;
    }

    public int getYear() {
        return year;
    }

    @Override
    public String toString() {
        return "QuizStatsDto {id=" + id + 
                ", numQuizzes=" + this.getNumQuizzes() + 
                ", numUniqueAnsweredQuizzes=" + this.getNumUniqueAnsweredQuizzes() + 
                ", averageQuizzesSolved=" + this.getAverageQuizzesSolved() +
                "}";
    }
    
}