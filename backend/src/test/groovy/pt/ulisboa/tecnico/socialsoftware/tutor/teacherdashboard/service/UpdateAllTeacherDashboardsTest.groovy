package pt.ulisboa.tecnico.socialsoftware.tutor.teacherdashboard.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
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

@DataJpaTest
class UpdateAllTeacherDashboardsTest extends SpockTest {

    def setup() {
        createExternalCourseAndExecution()
    }

    def createTeacher(name, courseExecution) {
        def teacher = new Teacher(name, false)
        userRepository.save(teacher)
        teacher.addCourse(courseExecution)
        return(teacher)
    }

    def createQuiz(courseExecution) {
        def quiz = new Quiz()
        quiz.setTitle("Quiz Title")
        quiz.setType(Quiz.QuizType.EXAM.name())
        quiz.setCourseExecution(courseExecution)
        quiz.setCreationDate(DateHandler.now())
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)

        return quiz
    }

    def createQuestion() {
        def newQuestion = new Question()
        newQuestion.setTitle(QUESTION_1_TITLE)
        newQuestion.setCourse(externalCourse)
        def questionDetails = new MultipleChoiceQuestion()
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

        return newQuestion;
    }

    def createStudent(username, courseExecution) {
        def student = new Student(USER_1_USERNAME, username, USER_1_EMAIL, false, AuthUser.Type.TECNICO)
        student.addCourse(courseExecution)
        userRepository.save(student)
        return student;
    }

    def createQuizQuestion(quiz, sequence, courseExecution) {
        def question = createQuestion()
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

    def createCourseExecution(acronym, term, date) {
        def courseExecution = new CourseExecution(externalCourse, acronym, term, Course.Type.TECNICO, date)
        courseExecutionRepository.save(courseExecution)
        return courseExecution
    }

    def "update teacher dashboards but there are no teacher dashboards"() {
        when: "update dashboards"
        teacherDashboardService.updateAllTeacherDashboards()

        then: "there are no dashboards"
        teacherDashboardRepository.findAll().size() == 0L
    }

    def "update an empty dashboard"() {
        given: "a teacher and its dashboard"
        def teacher = createTeacher(USER_1_NAME, externalCourseExecution)
        def dashboard = teacherDashboardService.createTeacherDashboard(externalCourseExecution.getId(), teacher.getId())

        when: "update dashboard"
        teacherDashboardService.updateAllTeacherDashboards()

        then: "the dashboard is updated but it's still empty"
        teacherDashboardRepository.findAll().size() == 1L

        def result = teacherDashboardRepository.findAll().get(0)

        result.getId() == dashboard.getId()
        result.getCourseExecution().getId() == externalCourseExecution.getId()
        result.getTeacher().getId() == teacher.getId()

        result.getQuizStats().size() == 1
        result.getQuestionStats().size() == 1
        result.getStudentStats().size() == 1

        result.getStudentStats().get(0).getNumStudents() == 0
        result.getStudentStats().get(0).getNumMore75CorrectQuestions() == 0
        result.getStudentStats().get(0).getNumAtLeast3Quizzes() == 0

        result.getQuizStats().get(0).getNumQuizzes() == 0
        result.getQuizStats().get(0).getNumUniqueAnsweredQuizzes() == 0
        result.getQuizStats().get(0).getAverageQuizzesSolved() == 0
        
        result.getQuestionStats().get(0).getNumAvailable() == 0
        result.getQuestionStats().get(0).getAnsweredQuestionsUnique() == 0
        result.getQuestionStats().get(0).getAverageQuestionsAnswered() == 0
    }

    def "update a teacher dashboard with a quiz and a student"() {
        given: "a teacher and its dashboard"
        def teacher = createTeacher(USER_1_NAME, externalCourseExecution)
        def dashboard = teacherDashboardService.createTeacherDashboard(externalCourseExecution.getId(), teacher.getId())

        and: "a quiz and a student"
        def quiz = createQuiz(externalCourseExecution)
        def student = createStudent("student", externalCourseExecution)

        when: "update dashboard"
        teacherDashboardService.updateAllTeacherDashboards()

        then: "the dashboard is updated and has the right data"
        teacherDashboardRepository.findAll().size() == 1L

        def result = teacherDashboardRepository.findAll().get(0)

        result.getId() == dashboard.getId()
        result.getCourseExecution().getId() == externalCourseExecution.getId()
        result.getTeacher().getId() == teacher.getId()

        result.getQuizStats().size() == 1
        result.getQuestionStats().size() == 1
        result.getStudentStats().size() == 1

        result.getStudentStats().get(0).getNumStudents() == 1
        result.getStudentStats().get(0).getNumMore75CorrectQuestions() == 0
        result.getStudentStats().get(0).getNumAtLeast3Quizzes() == 0

        result.getQuizStats().get(0).getNumQuizzes() == 1
        result.getQuizStats().get(0).getNumUniqueAnsweredQuizzes() == 0
        result.getQuizStats().get(0).getAverageQuizzesSolved() == 0

        result.getQuestionStats().get(0).getNumAvailable() == 0
        result.getQuestionStats().get(0).getAnsweredQuestionsUnique() == 0
        result.getQuestionStats().get(0).getAverageQuestionsAnswered() == 0
    }
    
    def "given four course executions update four teacher dashboards where two are empty and the other ones have different data"() {
        given: "a teacher"
        def teacher = createTeacher(USER_1_NAME, externalCourseExecution)

        and: "three course executions plus the current one"
        def externalCourseExecution1 = createCourseExecution(COURSE_1_ACRONYM, "2 Semestre 2018/2019", LOCAL_DATE_YESTERDAY)
        teacher.addCourse(externalCourseExecution1)

        def externalCourseExecution2 = createCourseExecution(COURSE_1_ACRONYM, "1 Semestre 2018/2019", LOCAL_DATE_BEFORE)
        teacher.addCourse(externalCourseExecution2)

        def externalCourseExecution3 = createCourseExecution(COURSE_1_ACRONYM, COURSE_2_ACADEMIC_TERM, LOCAL_DATE_TOMORROW)
        teacher.addCourse(externalCourseExecution3)

        and: "two empty teacher dashboards"
        teacherDashboardService.createTeacherDashboard(externalCourseExecution2.getId(), teacher.getId())
        teacherDashboardService.createTeacherDashboard(externalCourseExecution1.getId(), teacher.getId())

        and: "a teacher dashboard with a student that answer two quizzes with one question each and is correct"
        def dashboard1 = teacherDashboardService.createTeacherDashboard(externalCourseExecution.getId(), teacher.getId())
        def quiz2 = createQuiz(externalCourseExecution)
        def quiz3 = createQuiz(externalCourseExecution)
        def student2 = createStudent("student2", externalCourseExecution)
        def quizQuestion2 = createQuizQuestion(quiz2, 0, externalCourseExecution)
        def quizQuestion3 = createQuizQuestion(quiz3, 0, externalCourseExecution)
        def quizAnswer2 = createQuizAnswer(quiz2, student2)
        def quizAnswer3 = createQuizAnswer(quiz3, student2)
        def questionAnswer2 = createQuestionAnswer(quizAnswer2, quizQuestion2, true)
        def questionAnswer3 = createQuestionAnswer(quizAnswer3, quizQuestion3, true)

        and: "a teacher dashboard with a student that answers correctly the only question of a quiz"
        def dashboard2 = teacherDashboardService.createTeacherDashboard(externalCourseExecution3.getId(), teacher.getId())
        def quiz1 = createQuiz(externalCourseExecution3)    
        def student1 = createStudent("student1", externalCourseExecution3)
        def quizQuestion1 = createQuizQuestion(quiz1, 0, externalCourseExecution3)
        def quizAnswer1 = createQuizAnswer(quiz1, student1)
        def questionAnswer1 = createQuestionAnswer(quizAnswer1, quizQuestion1, true)

        when: "update dashboards"
        teacherDashboardService.updateAllTeacherDashboards()

        then: "dashboard1 is updated"
        def result1 = teacherDashboardRepository.findById(dashboard1.getId()).get()

        result1.getQuizStats().size() == 3
        result1.getQuizStats().get(0).getNumQuizzes() == 2
        result1.getQuizStats().get(0).getNumUniqueAnsweredQuizzes() == 2
        result1.getQuizStats().get(0).getAverageQuizzesSolved() == 2f

        result1.getStudentStats().size() == 3
        result1.getStudentStats().get(0).getNumStudents() == 1
        result1.getStudentStats().get(0).getNumMore75CorrectQuestions() == 1
        result1.getStudentStats().get(0).getNumAtLeast3Quizzes() == 0

        result1.getQuestionStats().size() == 3
        result1.getQuestionStats().get(0).getNumAvailable() == 0
        result1.getQuestionStats().get(0).getAnsweredQuestionsUnique() == 2
        result1.getQuestionStats().get(0).getAverageQuestionsAnswered() == 2f

        then: "dashboard2 is updated with the right data"
        def result2 = teacherDashboardRepository.findById(dashboard2.getId()).get()

        result2.getQuizStats().size() == 3
        result2.getQuizStats().get(0).getNumQuizzes() == 1
        result2.getQuizStats().get(0).getNumUniqueAnsweredQuizzes() == 1
        result2.getQuizStats().get(0).getAverageQuizzesSolved() == 1

        result2.getStudentStats().size() == 3
        result2.getStudentStats().get(0).getNumStudents() == 1
        result2.getStudentStats().get(0).getNumMore75CorrectQuestions() == 1
        result2.getStudentStats().get(0).getNumAtLeast3Quizzes() == 0

        result2.getQuestionStats().size() == 3
        result2.getQuestionStats().get(0).getNumAvailable() == 0
        result2.getQuestionStats().get(0).getAnsweredQuestionsUnique() == 1
        result2.getQuestionStats().get(0).getAverageQuestionsAnswered() == 1f
    }

    def "given 1 course executions update 2 dashboards"() {
        given:
        def teacher1 = createTeacher(USER_1_NAME, externalCourseExecution)
        def dashboard = teacherDashboardService.createTeacherDashboard(externalCourseExecution.getId(), teacher1.getId())
        
        def teacher2 = createTeacher(USER_2_NAME, externalCourseExecution)        
        teacherDashboardService.createTeacherDashboard(externalCourseExecution.getId(), teacher2.getId())
        
        def quiz = createQuiz(externalCourseExecution)
        def question = createQuestion()
        def student = createStudent("student", externalCourseExecution)
        def quizQuestion = createQuizQuestion(quiz, 0, externalCourseExecution)
        def quizAnswer = createQuizAnswer(quiz, student)
        def questionAnswer = createQuestionAnswer(quizAnswer, quizQuestion, true)

        when: "update dashboards"
        teacherDashboardService.updateAllTeacherDashboards()

        then: "the dashboard is updated with the right data"
        def result = teacherDashboardRepository.findById(dashboard.getId()).get()

        result.getQuizStats().size() == 1
        result.getQuizStats().get(0).getNumQuizzes() == 1
        result.getQuizStats().get(0).getNumUniqueAnsweredQuizzes() == 1
        result.getQuizStats().get(0).getAverageQuizzesSolved() == 1

        result.getStudentStats().size() == 1
        result.getStudentStats().get(0).getNumStudents() == 1
        result.getStudentStats().get(0).getNumMore75CorrectQuestions() == 1
        result.getStudentStats().get(0).getNumAtLeast3Quizzes() == 0

        result.getQuestionStats().size() == 1
        result.getQuestionStats().get(0).getNumAvailable() == 0
        result.getQuestionStats().get(0).getAnsweredQuestionsUnique() == 1
        result.getQuestionStats().get(0).getAverageQuestionsAnswered() == 1
    }

    def "5 students answer quizzes and 2 dashboards are updated"() {
        given: "a teacher, his dashboard and stats"
        def teacher1 = createTeacher(USER_1_NAME, externalCourseExecution)
        def dashboard1 = teacherDashboardService.createTeacherDashboard(externalCourseExecution.getId(), teacher1.getId())
        
        def student1 = createStudent("student1", externalCourseExecution)
        def student2 = createStudent("student2", externalCourseExecution)
        def student3 = createStudent("student3", externalCourseExecution)

        def quiz1 = createQuiz(externalCourseExecution)
        def quizQuestion1 = createQuizQuestion(quiz1, 0, externalCourseExecution)
        def quiz2 = createQuiz(externalCourseExecution)
        def quizQuestion2 = createQuizQuestion(quiz2, 0, externalCourseExecution)
        def quiz3 = createQuiz(externalCourseExecution)
        def quizQuestion3 = createQuizQuestion(quiz3, 0, externalCourseExecution)

        and: "Another teacher, his dashboard and stats"
        def externalCourseExecution2 = createCourseExecution(COURSE_1_ACRONYM, COURSE_2_ACADEMIC_TERM, LOCAL_DATE_YESTERDAY)

        def teacher2 = createTeacher(USER_1_NAME, externalCourseExecution2)
        def dashboard2 = teacherDashboardService.createTeacherDashboard(externalCourseExecution2.getId(), teacher2.getId())

        def student4 = createStudent("student4", externalCourseExecution2)
        def student5 = createStudent("student5", externalCourseExecution2)

        def quiz4 = createQuiz(externalCourseExecution2)
        def quizQuestion4 = createQuizQuestion(quiz4, 0, externalCourseExecution2)
        def quiz5 = createQuiz(externalCourseExecution2)
        def quizQuestion5 = createQuizQuestion(quiz5, 0, externalCourseExecution2)

        and: "1 student submits 3 quizzes with correct answers for dashboard1"
        def quizAns1 = createQuizAnswer(quiz1, student1)
        createQuestionAnswer(quizAns1, quizQuestion1, true)
        def quizAns2 = createQuizAnswer(quiz2, student1)
        createQuestionAnswer(quizAns2, quizQuestion2, true)
        def quizAns3 = createQuizAnswer(quiz3, student1)
        createQuestionAnswer(quizAns3, quizQuestion3, true)

        and: "1 student submits 1 quiz, with 100% correct answers for dashboard1"
        def quizAns4 = createQuizAnswer(quiz1, student2)
        createQuestionAnswer(quizAns4, quizQuestion1, true)

        and: "1 student submits 3 quizzes, with two correct answers and a wrong answer for dashboard1"
        def quizAns5 = createQuizAnswer(quiz1, student3)
        createQuestionAnswer(quizAns5, quizQuestion1, true)
        def quizAns6 = createQuizAnswer(quiz2, student3)
        createQuestionAnswer(quizAns6, quizQuestion2, true)
        def quizAns7 = createQuizAnswer(quiz3, student3)
        createQuestionAnswer(quizAns7, quizQuestion3, false)

        and: "1 student answers 2 quizzes, with 100% correct answers, but does not submit them for dashboard2"
        def quizAns8 = createQuizAnswer(quiz4, student4, DateHandler.now(), false)
        createQuestionAnswer(quizAns8, quizQuestion4, true)
        def quizAns9 = createQuizAnswer(quiz5, student4, DateHandler.now(), false)
        createQuestionAnswer(quizAns9, quizQuestion5, true)

        and: "1 student submits 1 quiz with a correct answer for dashboard2"
        def quizAns10 = createQuizAnswer(quiz4, student5)
        createQuestionAnswer(quizAns10, quizQuestion4, true)

        when: "update TeacherDashboard"
        teacherDashboardService.updateAllTeacherDashboards()

        then: "dashboard1 is updated with the right data"
        def result1 = teacherDashboardRepository.findById(dashboard1.getId()).get()

        result1.getId() == dashboard1.getId()
        result1.getCourseExecution().getId() == externalCourseExecution.getId()
        result1.getTeacher().getId() == teacher1.getId()

        result1.getQuizStats().size() == 1
        result1.getQuestionStats().size() == 1
        result1.getStudentStats().size() == 1

        result1.getStudentStats().get(0).getNumStudents() == 3
        result1.getStudentStats().get(0).getNumMore75CorrectQuestions() == 2
        result1.getStudentStats().get(0).getNumAtLeast3Quizzes() == 2

        result1.getQuizStats().get(0).getNumQuizzes() == 3
        result1.getQuizStats().get(0).getNumUniqueAnsweredQuizzes() == 3
        result1.getQuizStats().get(0).getAverageQuizzesSolved() == 2.3333333f

        result1.getQuestionStats().get(0).getNumAvailable() == 0
        result1.getQuestionStats().get(0).getAnsweredQuestionsUnique() == 3
        result1.getQuestionStats().get(0).getAverageQuestionsAnswered() == 2.3333333f

        and: "dashboard2 is updated with the right data"
        def result2 = teacherDashboardRepository.findById(dashboard2.getId()).get()

        result2.getId() == dashboard2.getId()
        result2.getCourseExecution().getId() == externalCourseExecution2.getId()
        result2.getTeacher().getId() == teacher2.getId()

        result2.getQuizStats().size() == 1
        result2.getQuestionStats().size() == 1
        result2.getStudentStats().size() == 1

        result2.getStudentStats().get(0).getNumStudents() == 2
        result2.getStudentStats().get(0).getNumMore75CorrectQuestions() == 1
        result2.getStudentStats().get(0).getNumAtLeast3Quizzes() == 0

        result2.getQuizStats().get(0).getNumQuizzes() == 2
        result2.getQuizStats().get(0).getNumUniqueAnsweredQuizzes() == 1
        result2.getQuizStats().get(0).getAverageQuizzesSolved() == 0.5f

        result2.getQuestionStats().get(0).getNumAvailable() == 0
        result2.getQuestionStats().get(0).getAnsweredQuestionsUnique() == 2
        result2.getQuestionStats().get(0).getAverageQuestionsAnswered() == 1.5f
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
