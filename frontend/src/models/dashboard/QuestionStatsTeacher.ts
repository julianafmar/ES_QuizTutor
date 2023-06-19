export default class QuestionStatsTeacher {
    id!: number;
    year!: number;
    numAvailable!: number;
    answeredQuestionsUnique!: number;
    averageQuestionsAnswered!: number;

    constructor(jsonObj?: QuestionStatsTeacher) {
        if (jsonObj) {
            this.id = jsonObj.id;
            this.year = jsonObj.year;

            this.numAvailable = jsonObj.numAvailable;
            this.answeredQuestionsUnique = jsonObj.answeredQuestionsUnique;
            this.averageQuestionsAnswered = jsonObj.averageQuestionsAnswered;
        }
    }
}