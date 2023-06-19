package pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.domain.StudentStats;

public class StudentStatsDto {
    private Integer id;

    private int numStudents = 0;
    private int numMore75CorrectQuestions = 0;
    private int numAtLeast3Quizzes = 0;
    private int year;

    public StudentStatsDto(StudentStats studentStats) {
        this.id = studentStats.getId();
        numStudents = studentStats.getNumStudents();
        numMore75CorrectQuestions = studentStats.getNumMore75CorrectQuestions();
        numAtLeast3Quizzes = studentStats.getNumAtLeast3Quizzes();
        year = studentStats.getCourseExecution().getYear();
    }

    public Integer getId() {
        return id;
    }

    public int getNumStudents() {
        return numStudents;
    }

    public void setNumStudents(int numStudents) {
        this.numStudents = numStudents;
    }

    public int getNumMore75CorrectQuestions() {
        return numMore75CorrectQuestions;
    }

    public void setNumMore75CorrectQuestions(int numMore75CorrectQuestions) {
        this.numMore75CorrectQuestions = numMore75CorrectQuestions;
    }

    public int getNumAtLeast3Quizzes() {
        return numAtLeast3Quizzes;
    }

    public void setNumAtLeast3Quizzes(int numAtLeast3Quizzes) {
        this.numAtLeast3Quizzes = numAtLeast3Quizzes;
    }

    public int getYear() {
        return year;
    }

    @Override
    public String toString() {
        return "StudentStatsDto {id=" + id + 
                ", numStudents=" + this.getNumStudents() + 
                ", numMore75CorrectQuestions=" + this.getNumMore75CorrectQuestions() + 
                ", numAtLeast3Quizzes=" + this.getNumAtLeast3Quizzes() +
                "}";
    }
}
