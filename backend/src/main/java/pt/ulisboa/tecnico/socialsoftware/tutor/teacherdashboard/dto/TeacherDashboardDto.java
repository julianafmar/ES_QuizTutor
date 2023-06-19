package pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.domain.TeacherDashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeacherDashboardDto {
    private Integer id;
    private Integer numberOfStudents;
    private List<StudentStatsDto> studentStats = new ArrayList<>();

    private List<QuizStatsDto> quizStats = new ArrayList<>();

    private List<QuestionStatsDto> questionStats = new ArrayList<>();

    public TeacherDashboardDto() {
    }

    public TeacherDashboardDto(TeacherDashboard teacherDashboard) {
        this.id = teacherDashboard.getId();
        // For the number of students, we consider only active students
        this.numberOfStudents = teacherDashboard.getCourseExecution().getNumberOfActiveStudents();
        this.studentStats = teacherDashboard.getStudentStats().stream().map(StudentStatsDto::new).collect(Collectors.toList());
        this.quizStats = teacherDashboard.getQuizStats().stream().map(QuizStatsDto::new).collect(Collectors.toList());
        this.questionStats = teacherDashboard.getQuestionStats().stream().map(QuestionStatsDto::new).collect(Collectors.toList());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumberOfStudents() {
        return numberOfStudents;
    }

    public void setNumberOfStudents(Integer numberOfStudents) {
        this.numberOfStudents = numberOfStudents;
    }

    public List<StudentStatsDto> getStudentStats() {
        return studentStats;
    }

    public void setStudentStats(List<StudentStatsDto> studentStats) {
        this.studentStats = studentStats;
    }

    public List<QuizStatsDto> getQuizStats() {
        return quizStats;
    }

    public void setQuizStats(List<QuizStatsDto> quizStats) {
        this.quizStats = quizStats;
    }

    public List<QuestionStatsDto> getQuestionStats() {
        return questionStats;
    }

    public void setQuestionStats(List<QuestionStatsDto> questionStats) {
        this.questionStats = questionStats;
    }

    @Override
    public String toString() {
        return "TeacherDashboardDto{" +
                "id=" + id +
                ", numberOfStudents=" + this.getNumberOfStudents() +
                ", studentStats=" + this.getStudentStats() +
                ", quizStats=" + this.getQuizStats() +
                ", questionStats=" + this.getQuestionStats() +
                "}";
    }
}