package pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.repository.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.domain.QuestionStats;
import pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.domain.QuizStats;
import pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.domain.StudentStats;
import pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.domain.TeacherDashboard;
import pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.dto.TeacherDashboardDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.repository.QuestionStatsRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.repository.QuizStatsRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.repository.StudentStatsRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.repository.TeacherDashboardRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Teacher;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.repository.TeacherRepository;

import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class TeacherDashboardService {

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private TeacherDashboardRepository teacherDashboardRepository;

    @Autowired
    private QuestionStatsRepository questionStatsRepository;

    @Autowired
    private QuizStatsRepository quizStatsRepository;

    @Autowired
    private StudentStatsRepository studentStatsRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TeacherDashboardDto getTeacherDashboard(int courseExecutionId, int teacherId) {
        CourseExecution courseExecution = courseExecutionRepository.findById(courseExecutionId)
                .orElseThrow(() -> new TutorException(COURSE_EXECUTION_NOT_FOUND));
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, teacherId));

        if (!teacher.getCourseExecutions().contains(courseExecution))
            throw new TutorException(TEACHER_NO_COURSE_EXECUTION);

        Optional<TeacherDashboard> dashboardOptional = teacher.getDashboards().stream()
                .filter(dashboard -> dashboard.getCourseExecution().getId().equals(courseExecutionId))
                .findAny();

        return dashboardOptional.
                map(TeacherDashboardDto::new).
                orElseGet(() -> createAndReturnTeacherDashboardDto(courseExecution, teacher));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TeacherDashboardDto createTeacherDashboard(int courseExecutionId, int teacherId) {
        CourseExecution courseExecution = courseExecutionRepository.findById(courseExecutionId)
                .orElseThrow(() -> new TutorException(COURSE_EXECUTION_NOT_FOUND));
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, teacherId));

        if (teacher.getDashboards().stream().anyMatch(dashboard -> dashboard.getCourseExecution().equals(courseExecution)))
            throw new TutorException(TEACHER_ALREADY_HAS_DASHBOARD);

        if (!teacher.getCourseExecutions().contains(courseExecution))
            throw new TutorException(TEACHER_NO_COURSE_EXECUTION);

        return createAndReturnTeacherDashboardDto(courseExecution, teacher);
    }

    private TeacherDashboardDto createAndReturnTeacherDashboardDto(CourseExecution courseExecution, Teacher teacher) {
        TeacherDashboard teacherDashboard = new TeacherDashboard(courseExecution, teacher);

        // get the last three courseExecution
        List<CourseExecution> courseExecutions = courseExecution.getCourse().getCourseExecutions()
                                                .stream().filter(ce -> ce.getCourse() == courseExecution.getCourse())
                                                .filter(ce -> ce.getEndDate() != null && ce.getEndDate().compareTo(courseExecution.getEndDate()) <= 0)
                                                .sorted(Comparator.comparing(CourseExecution::getEndDate).reversed())
                                                .limit(3).collect(Collectors.toList());

        List<StudentStats> studentS = courseExecutions.stream().map(ce -> studentStatsRepository.save(new StudentStats(teacherDashboard, ce))).collect(Collectors.toList());

        List<QuizStats> quizS = courseExecutions.stream().map(ce -> quizStatsRepository.save(new QuizStats(teacherDashboard, ce))).collect(Collectors.toList());

        List<QuestionStats> questionS = courseExecutions.stream().map(ce -> questionStatsRepository.save(new QuestionStats(teacherDashboard, ce))).collect(Collectors.toList());

        teacherDashboard.setStudentStats(studentS);
        teacherDashboard.setQuizStats(quizS);
        teacherDashboard.setQuestionStats(questionS);

        teacherDashboard.update();
        teacherDashboardRepository.save(teacherDashboard);

        return new TeacherDashboardDto(teacherDashboard);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeTeacherDashboard(Integer dashboardId) {
        if (dashboardId == null)
            throw new TutorException(DASHBOARD_NOT_FOUND, -1);

        TeacherDashboard teacherDashboard = teacherDashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(DASHBOARD_NOT_FOUND, dashboardId));

        questionStatsRepository.deleteAll(teacherDashboard.getQuestionStats());
        quizStatsRepository.deleteAll(teacherDashboard.getQuizStats());
        studentStatsRepository.deleteAll(teacherDashboard.getStudentStats());

        teacherDashboard.remove();
        teacherDashboardRepository.delete(teacherDashboard);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateTeacherDashboard(int dashboardId) {
        TeacherDashboard td = teacherDashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new TutorException(DASHBOARD_NOT_FOUND, dashboardId));
        td.update();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateAllTeacherDashboards() {
        teacherDashboardRepository.findAll().stream().forEach(TeacherDashboard::update);
    }

}
