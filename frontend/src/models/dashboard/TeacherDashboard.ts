import StudentStatsTeacher from './StudentStatsTeacher';
import QuizStatsTeacher from './QuizStatsTeacher';
import QuestionStatsTeacher from './QuestionStatsTeacher';

export default class TeacherDashboard {
  id!: number;
  numberOfStudents!: number;
  studentStats: StudentStatsTeacher[] = [];
  quizStats: QuizStatsTeacher[] = [];
  questionStats: QuestionStatsTeacher[] = [];

  constructor(jsonObj?: TeacherDashboard) {

    if (jsonObj) {

      this.id = jsonObj.id;
      this.numberOfStudents = jsonObj.numberOfStudents;
      
      if (jsonObj.studentStats)
        this.studentStats = jsonObj.studentStats.map((studentStatsTeacher: StudentStatsTeacher) =>
          new StudentStatsTeacher(studentStatsTeacher)).sort((a, b) => b.year - a.year);

      if (jsonObj.quizStats)
        this.quizStats = jsonObj.quizStats.map((quizStatsTeacher: QuizStatsTeacher) =>
          new QuizStatsTeacher(quizStatsTeacher)).sort((a, b) => b.year - a.year);

      if (jsonObj.questionStats)
        this.questionStats = jsonObj.questionStats.map((questionStatsTeacher: QuestionStatsTeacher) =>
          new QuestionStatsTeacher(questionStatsTeacher)).sort((a, b) => b.year - a.year);

    }
  }

  getLabels() {
    const labels: string[] = [];
    const len: number = this.studentStats.length;

    for (let i = 0; i < len; i++) {
      labels[len-i-1] = this.studentStats[i].year.toString();
    }

    labels[len-1] = labels[len-1] + ' (current)';
    return labels;
  }
  
  getStudentData() {
    const studentData: number[][] = [[]];
    const len: number = this.studentStats.length;

    studentData[0] = [];
    studentData[1] = [];
    studentData[2] = [];

    for (let i = 0; i < len; i++) {
      studentData[0][len-i-1] = this.studentStats[i].numStudents;
      studentData[1][len-i-1] = this.studentStats[i].numMore75CorrectQuestions;
      studentData[2][len-i-1] = this.studentStats[i].numAtLeast3Quizzes;
    }
    
    return [
      {
        label: 'Total Number of Students',
        backgroundColor: 'rgba(255, 0, 115, 0.3)',
        data: studentData[0],
      },

      {
        label: 'Students who Solved >= 75% of Questions',
        backgroundColor: 'rgba(54, 16, 235, 0.3)',
        data: studentData[1],
      },

      {
        label: 'Students who Solved >= 3 Quizzes',
        backgroundColor: 'rgba(54, 162, 200, 0.3)',
        data: studentData[2],
      }
    ]
  }

  getQuizData() {
    const quizData: number[][] = [[]];
    const len: number = this.quizStats.length;

    quizData[0] = [];
    quizData[1] = [];
    quizData[2] = [];

    for (let i = 0; i < len; i++) {
      quizData[0][len-i-1] = this.quizStats[i].numQuizzes;
      quizData[1][len-i-1] = this.quizStats[i].numUniqueAnsweredQuizzes;
      quizData[2][len-i-1] = this.quizStats[i].averageQuizzesSolved;
    }

    return [
      {
        label: 'Quizzes: Total Available',
        backgroundColor: 'rgba(255, 0, 115, 0.3)',
        data: quizData[0],
      },

      {
        label: 'Quizzes: Solved (Unique)',
        backgroundColor: 'rgba(54, 16, 235, 0.3)',
        data: quizData[1],
      },

      {
        label: 'Quizzes: Solved (Unique, Average per Student)',
        backgroundColor: 'rgba(54, 162, 200, 0.3)',
        data: quizData[2],
      }
    ]


  }

  getQuestionData() {
    const questionData: number[][] = [[]];
    const len: number = this.questionStats.length;

    questionData[1] = [];
    questionData[2] = [];
    questionData[0] = [];

    for (let i = 0; i < len; i++) {
      questionData[0][len-i-1] = this.questionStats[i].numAvailable;
      questionData[1][len-i-1] = this.questionStats[i].answeredQuestionsUnique;
      questionData[2][len-i-1] = this.questionStats[i].averageQuestionsAnswered;
    }

    return [
      {
        label: 'Questions: Total Available',
        backgroundColor: 'rgba(255, 0, 115, 0.3)',
        data: questionData[0],
      },

      {
        label: 'Questions: Total Solved (Unique)',
        backgroundColor: 'rgba(54, 16, 235, 0.3)',
        data: questionData[1],
      },

      {
        label: 'Questions: Correctly Solved (Unique, Average per Student)',
        backgroundColor: 'rgba(54, 162, 200, 0.3)',
        data: questionData[2],
      }
    ]
  }
}
