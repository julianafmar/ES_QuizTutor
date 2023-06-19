package pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.domain.QuestionStats
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Teacher
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import spock.lang.Unroll

import java.time.LocalDateTime

@DataJpaTest
class CreateTeacherDashboardTest extends SpockTest {
    def teacher

    def setup() {
        createExternalCourseAndExecution()

        teacher = new Teacher(USER_1_NAME, false)
        userRepository.save(teacher)
    }


    def "create an empty dashboard"() {
        given: "a teacher in a course execution"
        teacher.addCourse(externalCourseExecution)

        when: "a dashboard is created"
        teacherDashboardService.getTeacherDashboard(externalCourseExecution.getId(), teacher.getId())

        then: "an empty dashboard is created"
        teacherDashboardRepository.count() == 1L
        def result = teacherDashboardRepository.findAll().get(0)
        result.getId() != 0
        result.getCourseExecution().getId() == externalCourseExecution.getId()
        result.getTeacher().getId() == teacher.getId()

        and: "the teacher has a reference for the dashboard"
        teacher.getDashboards().size() == 1
        teacher.getDashboards().contains(result)
    }

    def "cannot create multiple dashboards for a teacher on a course execution"() {
        given: "a teacher in a course execution"
        teacher.addCourse(externalCourseExecution)

        and: "an empty dashboard for the teacher"
        teacherDashboardService.createTeacherDashboard(externalCourseExecution.getId(), teacher.getId())

        when: "a second dashboard is created"
        teacherDashboardService.createTeacherDashboard(externalCourseExecution.getId(), teacher.getId())

        then: "there is only one dashboard"
        teacherDashboardRepository.count() == 1L

        and: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TEACHER_ALREADY_HAS_DASHBOARD
    }

    def "cannot create a dashboard for a user that does not belong to the course execution"() {
        when: "a dashboard is created"
        teacherDashboardService.createTeacherDashboard(externalCourseExecution.getId(), teacher.getId())

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TEACHER_NO_COURSE_EXECUTION
    }

    @Unroll
    def "cannot create a dashboard with courseExecutionId=#courseExecutionId"() {
        when: "a dashboard is created"
        teacherDashboardService.createTeacherDashboard(courseExecutionId, teacher.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.COURSE_EXECUTION_NOT_FOUND

        where:
        courseExecutionId << [0, 100]
    }

    @Unroll
    def "cannot create a dashboard with teacherId=#teacherId"() {
        when: "a dashboard is created"
        teacherDashboardService.createTeacherDashboard(externalCourseExecution.getId(), teacherId)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_FOUND

        where:
        teacherId << [0, 100]
    }

    @Unroll
    def "create different courses and check if stats are correct"(){
        given:
        def courseExecution_1
        def courseExecution_2
        def courseExecution_3
        def courseExecution_4
        def courseExecution_5

        def course = new Course(COURSE_2_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        def course_2 = new Course(COURSE_3_NAME, Course.Type.TECNICO)
        courseRepository.save(course_2)

        and:
        def teacher_2 = new Teacher(USER_2_NAME, false)
        userRepository.save(teacher_2)

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
        teacher_2.addCourse(courseExecution_3)
        def td3_Id = teacherDashboardService.createTeacherDashboard(courseExecution_3.getId(), teacher_2.getId()).getId()

        courseExecution_4 = new CourseExecution(course_2, COURSE_3_ACRONYM, COURSE_4_ACADEMIC_TERM, Course.Type.TECNICO, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution_4)
        teacher.addCourse(courseExecution_4)
        def td4_Id = teacherDashboardService.createTeacherDashboard(courseExecution_4.getId(), teacher.getId()).getId()

        courseExecution_5 = new CourseExecution(course, COURSE_2_ACRONYM, COURSE_5_ACADEMIC_TERM, Course.Type.TECNICO, LOCAL_DATE_LATER)
        courseExecutionRepository.save(courseExecution_5)
        teacher.addCourse(courseExecution_5)
        def td5_Id = teacherDashboardService.createTeacherDashboard(courseExecution_5.getId(), teacher.getId()).getId()

        when:
        def teacherDashboard1 = teacherDashboardRepository.findById(td1_Id).get()
        def teacherDashboard2 = teacherDashboardRepository.findById(td2_Id).get()
        def teacherDashboard3 = teacherDashboardRepository.findById(td3_Id).get()
        def teacherDashboard4 = teacherDashboardRepository.findById(td4_Id).get()
        def teacherDashboard5 = teacherDashboardRepository.findById(td5_Id).get()

        then:
        //teacherDashboard1 has the right stats
        teacherDashboard1.getStudentStats().size() == 1
        teacherDashboard1.getQuestionStats().size() == 1
        teacherDashboard1.getQuizStats().size() == 1
        teacherDashboard1.getStudentStats().get(0).getCourseExecution() == courseExecution_1
        teacherDashboard1.getQuestionStats().get(0).getCourseExecution() == courseExecution_1
        teacherDashboard1.getQuizStats().get(0).getCourseExecution() == courseExecution_1

        // teacherDashboard2 has the right stats
        teacherDashboard2.getStudentStats().size() == 2
        teacherDashboard2.getQuestionStats().size() == 2
        teacherDashboard2.getQuizStats().size() == 2
        teacherDashboard2.getStudentStats().get(0).getCourseExecution() == courseExecution_2
        teacherDashboard2.getQuestionStats().get(0).getCourseExecution() == courseExecution_2
        teacherDashboard2.getQuizStats().get(0).getCourseExecution() == courseExecution_2
        teacherDashboard2.getStudentStats().get(1).getCourseExecution() == courseExecution_1
        teacherDashboard2.getQuestionStats().get(1).getCourseExecution() == courseExecution_1
        teacherDashboard2.getQuizStats().get(1).getCourseExecution() == courseExecution_1

        // teacherDashboard3 has the right stats (different teacher)
        teacherDashboard3.getStudentStats().size() == 3
        teacherDashboard3.getQuestionStats().size() == 3
        teacherDashboard3.getQuizStats().size() == 3
        teacherDashboard3.getStudentStats().get(0).getCourseExecution() == courseExecution_3
        teacherDashboard3.getQuestionStats().get(0).getCourseExecution() == courseExecution_3
        teacherDashboard3.getQuizStats().get(0).getCourseExecution() == courseExecution_3
        teacherDashboard3.getStudentStats().get(1).getCourseExecution() == courseExecution_2
        teacherDashboard3.getQuestionStats().get(1).getCourseExecution() == courseExecution_2
        teacherDashboard3.getQuizStats().get(1).getCourseExecution() == courseExecution_2
        teacherDashboard3.getStudentStats().get(2).getCourseExecution() == courseExecution_1
        teacherDashboard3.getQuestionStats().get(2).getCourseExecution() == courseExecution_1
        teacherDashboard3.getQuizStats().get(2).getCourseExecution() == courseExecution_1

        // teacherDashboard4 has the right stats (different course)
        teacherDashboard4.getStudentStats().size() == 1
        teacherDashboard4.getQuestionStats().size() == 1
        teacherDashboard4.getQuizStats().size() == 1
        teacherDashboard4.getStudentStats().get(0).getCourseExecution() == courseExecution_4
        teacherDashboard4.getQuestionStats().get(0).getCourseExecution() == courseExecution_4
        teacherDashboard4.getQuizStats().get(0).getCourseExecution() == courseExecution_4

        // teacherDashboard5 has the right stats
        teacherDashboard5.getStudentStats().size() == 3
        teacherDashboard5.getQuestionStats().size() == 3
        teacherDashboard5.getQuizStats().size() == 3
        teacherDashboard5.getStudentStats().get(0).getCourseExecution() == courseExecution_5
        teacherDashboard5.getQuestionStats().get(0).getCourseExecution() == courseExecution_5
        teacherDashboard5.getQuizStats().get(0).getCourseExecution() == courseExecution_5
        teacherDashboard5.getStudentStats().get(1).getCourseExecution() == courseExecution_3
        teacherDashboard5.getQuestionStats().get(1).getCourseExecution() == courseExecution_3
        teacherDashboard5.getQuizStats().get(1).getCourseExecution() == courseExecution_3
        teacherDashboard5.getStudentStats().get(2).getCourseExecution() == courseExecution_2
        teacherDashboard5.getQuestionStats().get(2).getCourseExecution() == courseExecution_2
        teacherDashboard5.getQuizStats().get(2).getCourseExecution() == courseExecution_2

    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
