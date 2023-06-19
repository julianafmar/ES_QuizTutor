package pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.domain.QuestionStats
import pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.domain.QuizStats
import pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.domain.StudentStats
import pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.domain.TeacherDashboard
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Teacher
import spock.lang.Unroll

@DataJpaTest
class RemoveTeacherDashboardTest extends SpockTest {

    def teacher

    def setup() {
        createExternalCourseAndExecution()

        teacher = new Teacher(USER_1_NAME, false)
        userRepository.save(teacher)
    }

    def createTeacherDashboard() {
        def dashboard = new TeacherDashboard(externalCourseExecution, teacher)
        teacherDashboardRepository.save(dashboard)
        return dashboard
    }

    def "remove a dashboard"() {
        given: "a dashboard"
        def dashboard = createTeacherDashboard()

        when: "the user removes the dashboard"
        teacherDashboardService.removeTeacherDashboard(dashboard.getId())

        then: "the dashboard is removed"
        teacherDashboardRepository.findAll().size() == 0L
        teacher.getDashboards().size() == 0
    }

    def "cannot remove a dashboard twice"() {
        given: "a removed dashboard"
        def dashboard = createTeacherDashboard()
        teacherDashboardService.removeTeacherDashboard(dashboard.getId())

        when: "the dashboard is removed for the second time"
        teacherDashboardService.removeTeacherDashboard(dashboard.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.DASHBOARD_NOT_FOUND
    }

    @Unroll
    def "cannot remove a dashboard that doesn't exist with the dashboardId=#dashboardId"() {
        when: "an incorrect dashboard id is removed"
        teacherDashboardService.removeTeacherDashboard(dashboardId)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.DASHBOARD_NOT_FOUND

        where:
        dashboardId << [null, 10, -1]
    }

    def "remove a dashboard that contains various statistics"() {
        given: "a dashboard with question, quiz and student statistics"
        def dashboard = createTeacherDashboard()
        def questionStats = new QuestionStats(dashboard, externalCourseExecution)
        questionStatsRepository.save(questionStats)

        def quizStats = new QuizStats(dashboard, externalCourseExecution)
        quizStatsRepository.save(quizStats)

        def studentStats = new StudentStats(dashboard, externalCourseExecution)
        studentStatsRepository.save(studentStats)

        when: "the user removes the dashboard"
        teacherDashboardService.removeTeacherDashboard(dashboard.getId())

        then: "the dashboard is removed"
        teacherDashboardRepository.findAll().size() == 0
        teacher.getDashboards().size() == 0
        questionStatsRepository.findAll().size() == 0
        quizStatsRepository.findAll().size() == 0
        studentStatsRepository.findAll().size() == 0
    }

    def "remove a dashboard that contains some statistics and not interfere with another dashboard"() {
        given: "two dashboards with question, quiz and student statistics"
        def dashboard1 = createTeacherDashboard()
        def questionStats1 = new QuestionStats(dashboard1, externalCourseExecution)
        questionStatsRepository.save(questionStats1)

        def quizStats1 = new QuizStats(dashboard1, externalCourseExecution)
        quizStatsRepository.save(quizStats1)

        def studentStats1 = new StudentStats(dashboard1, externalCourseExecution)
        studentStatsRepository.save(studentStats1)

        def dashboard2 = createTeacherDashboard()
        def questionStats2 = new QuestionStats(dashboard2, externalCourseExecution)
        questionStatsRepository.save(questionStats2)

        def quizStats2 = new QuizStats(dashboard2, externalCourseExecution)
        quizStatsRepository.save(quizStats2)

        def studentStats2 = new StudentStats(dashboard2, externalCourseExecution)
        studentStatsRepository.save(studentStats2)

        when: "the user removes a dashboard"
        teacherDashboardService.removeTeacherDashboard(dashboard1.getId())

        then: "a dashboard is removed"
        teacherDashboardRepository.findAll().size() == 1
        teacher.getDashboards().size() == 1
        questionStatsRepository.findAll().size() == 1
        quizStatsRepository.findAll().size() == 1
        studentStatsRepository.findAll().size() == 1
    }

    def "remove a dashboard and check if the statistics are removed"() {
        given: "a dashboard with question, quiz and student statistics"
        def courseExecution_1
        def courseExecution_2
        def courseExecution_3

        def course = new Course(COURSE_2_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        and:
        courseExecution_1 = new CourseExecution(course, COURSE_2_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.TECNICO, LOCAL_DATE_BEFORE)
        courseExecutionRepository.save(courseExecution_1)
        teacher.addCourse(courseExecution_1)
        def td1_Id = teacherDashboardService.createTeacherDashboard(courseExecution_1.getId(), teacher.getId()).getId()

        courseExecution_2 = new CourseExecution(course, COURSE_2_ACRONYM, COURSE_2_ACADEMIC_TERM, Course.Type.TECNICO, LOCAL_DATE_YESTERDAY)
        courseExecutionRepository.save(courseExecution_2)
        teacher.addCourse(courseExecution_2)
        def td2_Id = teacherDashboardService.createTeacherDashboard(courseExecution_2.getId(), teacher.getId()).getId()

        courseExecution_3 = new CourseExecution(course, COURSE_2_ACRONYM, COURSE_3_ACADEMIC_TERM, Course.Type.TECNICO, LOCAL_DATE_TODAY)
        courseExecutionRepository.save(courseExecution_3)
        teacher.addCourse(courseExecution_3)
        def td3_Id = teacherDashboardService.createTeacherDashboard(courseExecution_3.getId(), teacher.getId()).getId()

        when: "the user removes the dashboard"
        teacherDashboardService.removeTeacherDashboard(td2_Id)
        teacherDashboardRepository.findById(td2_Id).get()

        then: "the dashboard is removed"
        thrown(NoSuchElementException)
        def teacherDashboard1 = teacherDashboardRepository.findById(td1_Id).get()
        def teacherDashboard3 = teacherDashboardRepository.findById(td3_Id).get()

        teacherDashboardRepository.findAll().size() == 2
        teacher.getDashboards().size() == 2
        questionStatsRepository.findAll().size() == 4
        quizStatsRepository.findAll().size() == 4
        studentStatsRepository.findAll().size() == 4

        teacherDashboard1.getStudentStats().get(0).getCourseExecution() == courseExecution_1
        teacherDashboard1.getQuestionStats().get(0).getCourseExecution() == courseExecution_1
        teacherDashboard1.getQuizStats().get(0).getCourseExecution() == courseExecution_1

        teacherDashboard3.getStudentStats().get(0).getCourseExecution() == courseExecution_3
        teacherDashboard3.getQuestionStats().get(0).getCourseExecution() == courseExecution_3
        teacherDashboard3.getQuizStats().get(0).getCourseExecution() == courseExecution_3
        teacherDashboard3.getStudentStats().get(1).getCourseExecution() == courseExecution_2
        teacherDashboard3.getQuestionStats().get(1).getCourseExecution() == courseExecution_2
        teacherDashboard3.getQuizStats().get(1).getCourseExecution() == courseExecution_2
        teacherDashboard3.getStudentStats().get(2).getCourseExecution() == courseExecution_1
        teacherDashboard3.getQuestionStats().get(2).getCourseExecution() == courseExecution_1
        teacherDashboard3.getQuizStats().get(2).getCourseExecution() == courseExecution_1
    }


    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
