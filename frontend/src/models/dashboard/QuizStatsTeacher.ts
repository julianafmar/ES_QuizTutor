export default class QuizStatsTeacher {
    id!: number;  
    year!: number;

    numQuizzes!: number;
    numUniqueAnsweredQuizzes!: number;
    averageQuizzesSolved!: number;

    constructor(jsonObj?: QuizStatsTeacher) {
        if (jsonObj) {
            this.id = jsonObj.id;
            this.year = jsonObj.year;
            
            this.numQuizzes = jsonObj.numQuizzes;
            this.numUniqueAnsweredQuizzes = jsonObj.numUniqueAnsweredQuizzes;
            this.averageQuizzesSolved = jsonObj.averageQuizzesSolved;
        }
    }
}