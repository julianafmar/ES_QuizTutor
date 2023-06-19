export default class StudentStatsTeacher {
    id!: number;
    numStudents!: number;
    numMore75CorrectQuestions!: number;
    numAtLeast3Quizzes!: number;
    year!: number;

    constructor(jsonObj?: StudentStatsTeacher) {
        if (jsonObj) {
            this.id = jsonObj.id;
            
            this.numStudents = jsonObj.numStudents;
            this.numMore75CorrectQuestions = jsonObj.numMore75CorrectQuestions;
            this.numAtLeast3Quizzes = jsonObj.numAtLeast3Quizzes;
            this.year = jsonObj.year;
        }
    }
    
}
