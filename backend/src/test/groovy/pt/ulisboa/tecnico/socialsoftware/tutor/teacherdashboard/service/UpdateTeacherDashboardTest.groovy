package pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Teacher
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import spock.lang.Unroll

@DataJpaTest
class UpdateTeacherDashboardTest extends SpockTest {
    def teacher
    def course

    def setup() {
        teacher = new Teacher(USER_1_NAME, false)
        userRepository.save(teacher)
        course = new Course(COURSE_2_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
    }
    
    def createCourseExecutionAndTeacherDashboard(academicTerm, endDate) {
        def exec = new CourseExecution(course, COURSE_2_ACRONYM, academicTerm, Course.Type.TECNICO, endDate)
        courseExecutionRepository.save(exec)
        teacher.addCourse(exec)
        teacherDashboardService.createTeacherDashboard(exec.getId(), teacher.getId())
        return exec
    }

    def createQuiz(courseExecution) {
        def quiz = new Quiz()
        quiz.setTitle("Quiz Title")
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(courseExecution)
        quiz.setCreationDate(DateHandler.now())
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)
        return quiz
    }

    def createStudent(username, courseExecution) {
        def student = new Student(USER_1_USERNAME, username, USER_1_EMAIL, false, AuthUser.Type.TECNICO)
        student.addCourse(courseExecution)
        userRepository.save(student)
        return student;
    }

    def createQuestion(courseExecution) {
        def newQuestion = new Question()
        newQuestion.setTitle(QUESTION_1_TITLE)
        newQuestion.setCourse(course)
        def questionDetails = new MultipleChoiceQuestion()
        questionDetailsRepository.save(questionDetails)
        newQuestion.setQuestionDetails(questionDetails)
        questionRepository.save(newQuestion)

        def option = new Option()
        option.setContent(OPTION_1_CONTENT)
        option.setCorrect(true)
        option.setSequence(0)
        option.setQuestionDetails(questionDetails)
        optionRepository.save(option)
        def optionKO = new Option()
        optionKO.setContent(OPTION_2_CONTENT)
        optionKO.setCorrect(false)
        optionKO.setSequence(1)
        optionKO.setQuestionDetails(questionDetails)
        optionRepository.save(optionKO)
        return newQuestion
    }

    def createQuizQuestion(courseExecution, quiz, sequence) {
        def question = createQuestion(courseExecution)
        def quizQuestion = new QuizQuestion(quiz, question, sequence)
        quizQuestionRepository.save(quizQuestion)
        return quizQuestion
    }

    def createQuestionAnswer(quizAnswer, quizQuestion, correct) {
        def questionAnswer = new QuestionAnswer()
        questionAnswer.setTimeTaken(1)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)

        def option
        if(correct) {
            option = quizQuestion.getQuestion().getQuestionDetails().getOptions().get(0)
        }
        else {
            option = quizQuestion.getQuestion().getQuestionDetails().getOptions().get(1)
        }
        def answerDetails = new MultipleChoiceAnswer(questionAnswer, option)
        questionAnswer.setAnswerDetails(answerDetails)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answerDetails)
        return questionAnswer
    }

    def createQuizAnswer(quiz, student, date = DateHandler.now(), completed = true) {
        def quizAnswer = new QuizAnswer()
        quizAnswer.setCompleted(completed)
        quizAnswer.setCreationDate(date)
        quizAnswer.setAnswerDate(date)
        quizAnswer.setStudent(student)
        quizAnswer.setQuiz(quiz)
        quizAnswerRepository.save(quizAnswer)
        return quizAnswer
    }

    def addStats(courseExecution) {
        def student1 = createStudent("student1", courseExecution)
        def student2 = createStudent("student2", courseExecution)
        def student3 = createStudent("student3", courseExecution)
        def student4 = createStudent("student4", courseExecution)

        def quiz1 = createQuiz(courseExecution)
        def quizQuestion1 = createQuizQuestion(courseExecution, quiz1, 0)

        def quiz2 = createQuiz(courseExecution)
        def quizQuestion2 = createQuizQuestion(courseExecution, quiz2, 0)

        def quiz3 = createQuiz(courseExecution)
        def quizQuestion3 = createQuizQuestion(courseExecution, quiz3, 0)

        def quiz4 = createQuiz(courseExecution)
        def quizQuestion4 = createQuizQuestion(courseExecution, quiz4, 0)

        def quiz5 = createQuiz(courseExecution)

        def quizAns1 = createQuizAnswer(quiz1, student1)
        createQuestionAnswer(quizAns1, quizQuestion1, true)

        def quizAns2 = createQuizAnswer(quiz2, student1)
        createQuestionAnswer(quizAns2, quizQuestion2, true)

        def quizAns3 = createQuizAnswer(quiz3, student1)
        createQuestionAnswer(quizAns3, quizQuestion3, true)

        def quizAns4 = createQuizAnswer(quiz1, student2)
        createQuestionAnswer(quizAns4, quizQuestion1, true)

        def quizAns5 = createQuizAnswer(quiz1, student3)
        createQuestionAnswer(quizAns5, quizQuestion1, true)

        def quizAns6 = createQuizAnswer(quiz2, student3)
        createQuestionAnswer(quizAns6, quizQuestion2, true)

        def quizAns7 = createQuizAnswer(quiz3, student3)
        createQuestionAnswer(quizAns7, quizQuestion3, false)

        def quizAns8 = createQuizAnswer(quiz4, student3)
        createQuestionAnswer(quizAns8, quizQuestion4, false)

        def quizAns9 = createQuizAnswer(quiz1, student4, DateHandler.now(), false)
        createQuestionAnswer(quizAns9, quizQuestion1, true)

        def quizAns10 = createQuizAnswer(quiz2, student4, DateHandler.now(), false)
        createQuestionAnswer(quizAns10, quizQuestion2, true)

        def quizAns11 = createQuizAnswer(quiz3, student4, DateHandler.now(), false)
        createQuestionAnswer(quizAns11, quizQuestion3, true)

        def quizAns12 = createQuizAnswer(quiz4, student4, DateHandler.now(), false)
        createQuestionAnswer(quizAns12, quizQuestion4, true)
    }

    def checkStudentStat(teacherDashboard, nExec, numStudents, numMore75CorrectQuestions, numAtLeast3Quizzes) {
        def studentStat = teacherDashboard.getStudentStats().get(nExec)
        studentStat.getNumStudents() == numStudents
        studentStat.getNumMore75CorrectQuestions() == numMore75CorrectQuestions
        studentStat.getNumAtLeast3Quizzes() == numAtLeast3Quizzes
    }

    def checkQuizStat(teacherDashboard, nExec, numQuizzes, numUniqueAnsweredQuizzes, numAverageQuizzesSolved) {
        def quizStat = teacherDashboard.getQuizStats().get(nExec)
        quizStat.getNumQuizzes() == numQuizzes
        quizStat.getNumUniqueAnsweredQuizzes() == numUniqueAnsweredQuizzes
        quizStat.getAverageQuizzesSolved() == numAverageQuizzesSolved
    }

    def checkQuestionStat(teacherDashboard, nExec, answeredQuestionsUnique, averageQuestionsAnswered, numAvailable) {
        def questionStat = teacherDashboard.getQuestionStats().get(nExec)
        questionStat.getAnsweredQuestionsUnique() == answeredQuestionsUnique
        questionStat.getAverageQuestionsAnswered() == averageQuestionsAnswered
        questionStat.getNumAvailable() == numAvailable
    }

    @Unroll
    def "update teacher dashboard that does not exist"() {
        when: "update teacher dashboard"
        teacherDashboardService.updateTeacherDashboard(dashboardId)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.DASHBOARD_NOT_FOUND

        where:
        dashboardId << [1,2,3,4]
    }

    def "update empty teacher dashboard without previous course executions"() {

        given: "teacher dashboard without stats of previous course executions"
        def exec = createCourseExecutionAndTeacherDashboard(COURSE_1_ACADEMIC_TERM, LOCAL_DATE_TODAY)
        def teacherDashboard = teacherDashboardRepository.findById(teacherDashboardService.getTeacherDashboard(exec.getId(), teacher.getId()).getId()).get()

        when: "update teacher dashboard"
        teacherDashboardService.updateTeacherDashboard(teacherDashboard.getId())

        then:
        checkStudentStat(teacherDashboard, 0, 0, 0, 0)
        checkQuizStat(teacherDashboard, 0, 0, 0, 0)
        checkQuestionStat(teacherDashboard, 0, 0, 0, 0)
    }

    def "update teacher dashboard with previous course executions and add stats for the current course execution"() {
        
        given: "course execution 1 and its teacher dashboard"
        createCourseExecutionAndTeacherDashboard(COURSE_4_ACADEMIC_TERM, LOCAL_DATE_BEFORE)

        and: "course execution 2 and its teacher dashboard"
        createCourseExecutionAndTeacherDashboard(COURSE_3_ACADEMIC_TERM, LOCAL_DATE_YESTERDAY)
        
        and: "course execution 3 and its teacher dashboard"
        createCourseExecutionAndTeacherDashboard(COURSE_1_ACADEMIC_TERM, LOCAL_DATE_TOMORROW)
        
        and: "course execution 4 and its teacher dashboard"
        def exec4 = createCourseExecutionAndTeacherDashboard(COURSE_2_ACADEMIC_TERM, LOCAL_DATE_LATER)

        and:
        def teacherDashboard = teacherDashboardRepository.findById(teacherDashboardService.getTeacherDashboard(exec4.getId(), teacher.getId()).getId()).get()

        and: "add stats"
        addStats(exec4)

        when: "update teacher dashboard"
        teacherDashboardService.updateTeacherDashboard(teacherDashboard.getId())
        
        then:
        teacherDashboard.getStudentStats().size() == 3
        teacherDashboard.getStudentStats().get(0).getCourseExecution() == exec4
        checkStudentStat(teacherDashboard, 0, 4, 2, 2)
        checkStudentStat(teacherDashboard, 1, 0, 0, 0)
        checkStudentStat(teacherDashboard, 2, 0, 0, 0)

        and:
        teacherDashboard.getQuizStats().size() == 3
        checkQuizStat(teacherDashboard, 0, 5, 4, 2)
        checkQuizStat(teacherDashboard, 1, 0, 0, 0)
        checkQuizStat(teacherDashboard, 2, 0, 0, 0)

        and:
        teacherDashboard.getQuestionStats().size() == 3
        checkQuestionStat(teacherDashboard, 0, 4, 3, 0)
        checkQuestionStat(teacherDashboard, 1, 0, 0, 0)
        checkQuestionStat(teacherDashboard, 2, 0, 0, 0)
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}

}