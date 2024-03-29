<template>
  <div class="container">
    <h2>Statistics for this course execution</h2>
    <div v-if="teacherDashboard != null" class="stats-container">

      <div class="items">
        <div ref="totalStudents" class="icon-wrapper">
          <animated-number :number="numStudents" data-cy="numStudents"/>
        </div>
        <div class="project-name">
          <p>Number of Students</p>
        </div>
      </div>

      <div class="items">
        <div ref="more75CorrectQuestions" class="icon-wrapper">
          <animated-number :number="numMore75CorrectQuestions" data-cy="numMore75CorrectQuestions"/>
        </div>
        <div class="project-name">
          <p>Number of Students who Solved >= 75% Questions</p>
        </div>
      </div>
      
      <div class="items">
        <div ref="atLeast3Quizzes" class="icon-wrapper">
          <animated-number :number="numAtLeast3Quizzes" data-cy="numAtLeast3Quizzes"/>
        </div>
        <div class="project-name">
          <p>Number of Students who Solved >= 3 Quizzes</p>
        </div>
      </div>

      <div class="items">
        <div ref="numQuizzes" class="icon-wrapper">
          <animated-number :number="numQuizzes" data-cy="numQuizzes"/>
        </div>
        <div class="project-name">
          <p>Number of Quizzes</p>
        </div>
      </div>

      <div class="items">
        <div ref="uniqueAnsweredQuizzes" class="icon-wrapper">
          <animated-number :number="numUniqueAnsweredQuizzes" data-cy="numUniqueAnsweredQuizzes"/>
        </div>
        <div class="project-name">
          <p>Number of Quizzes Solved (Unique)</p>
        </div>
      </div>

      <div class="items">
        <div ref="averageQuestionsSolved" class="icon-wrapper">
          <animated-number :number="averageQuizzesSolved" data-cy="averageQuizzesSolved"/>
        </div>
        <div class="project-name">
          <p>Number of Quizzes Solved (Unique, Average Per Student)</p>
        </div>
      </div>

      <div class="items">
        <div ref="numQuestions" class="icon-wrapper">
          <animated-number :number="numAvailableQuestions" data-cy="numAvailableQuestions"/>
        </div>
        <div class="project-name">
          <p>Number of Questions</p>
        </div>
      </div>

      <div class="items">
        <div ref="numAnsweredQuestionsUnique" class="icon-wrapper">
          <animated-number :number="answeredQuestionsUnique" data-cy="answeredQuestionsUnique"/>
        </div>
        <div class="project-name">
          <p>Number of Questions Solved (Unique)</p>
        </div>
      </div>

      <div class="items">
        <div ref="numAverageQuestionsAnswered" class="icon-wrapper">
          <animated-number :number="averageQuestionsAnswered" data-cy="averageQuestionsAnswered"/>
        </div>
        <div class="project-name">
          <p>Number of Questions Correctly Solved (Unique, Average Per Student)</p>
        </div>
      </div>
    </div>

    <div v-if="teacherDashboard != null && teacherDashboard.studentStats.length != 1">
      <h2>Comparison with previous course executions</h2>    
      <p></p>
      <div v-if="teacherDashboard != null" class="stats-container">
        <div>
          <Bar :chartData="chartStudentData" :chartOptions="chartOptions" class="bar-chart" :style="{ marginRight: '20px' }" id="studentGraph"/>
        </div>
        <div > 
          <Bar :chartData="chartQuizData" :chartOptions="chartOptions" class="bar-chart" :style="{ marginLeft: '20px' }" id="quizGraph"/>
        </div>
        <div >
          <Bar :chartData="chartQuestionData" :chartOptions="chartOptions" class="bar-chart" :style="{ marginLeft: '40px' }" id="questionGraph"/>
        </div>
      </div>
    </div>
    
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import { Bar } from 'vue-chartjs/legacy';

import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale } from 'chart.js'

import RemoteServices from '@/services/RemoteServices';
import AnimatedNumber from '@/components/AnimatedNumber.vue';
import TeacherDashboard from '@/models/dashboard/TeacherDashboard';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

@Component({
  components: { AnimatedNumber, Bar },
})

export default class TeacherStatsView extends Vue {
  @Prop() readonly dashboardId!: number;
  teacherDashboard: TeacherDashboard | null = null;

  numStudents: number | undefined;
  numMore75CorrectQuestions: number | undefined;
  numAtLeast3Quizzes: number | undefined;

  numQuizzes : number | undefined;
  numUniqueAnsweredQuizzes : number | undefined;
  averageQuizzesSolved : number | undefined;

  numAvailableQuestions : number | undefined;
  answeredQuestionsUnique : number | undefined;
  averageQuestionsAnswered : number | undefined;
  
  chartStudentData : any = null;
  chartQuizData :any = null;
  chartQuestionData : any = null;
  chartOptions : any = null;

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.teacherDashboard = await RemoteServices.getTeacherDashboard();

      this.numStudents = this.teacherDashboard.studentStats[0].numStudents;
      this.numMore75CorrectQuestions = this.teacherDashboard.studentStats[0].numMore75CorrectQuestions;
      this.numAtLeast3Quizzes = this.teacherDashboard.studentStats[0].numAtLeast3Quizzes;

      this.numQuizzes = this.teacherDashboard.quizStats[0].numQuizzes;
      this.numUniqueAnsweredQuizzes = this.teacherDashboard.quizStats[0].numUniqueAnsweredQuizzes;
      this.averageQuizzesSolved = this.teacherDashboard.quizStats[0].averageQuizzesSolved;

      this.numAvailableQuestions = this.teacherDashboard.questionStats[0].numAvailable;
      this.answeredQuestionsUnique = this.teacherDashboard.questionStats[0].answeredQuestionsUnique;
      this.averageQuestionsAnswered = this.teacherDashboard.questionStats[0].averageQuestionsAnswered;
  
      this.chartStudentData = {
        labels : this.teacherDashboard.getLabels(),
        datasets: this.teacherDashboard.getStudentData()
      }

      this.chartQuizData = {
        labels : this.teacherDashboard.getLabels(),
        datasets: this.teacherDashboard.getQuizData()
      }

      this.chartQuestionData = {
        labels : this.teacherDashboard.getLabels(),
        datasets: this.teacherDashboard.getQuestionData()
      }

      this.chartOptions = {
        responsive: true,
        maintainAspectRatio: false
      }
      
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

}

</script>

<style lang="scss" scoped>
.stats-container {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: center;
  align-items: stretch;
  align-content: center;
  height: 100%;

  .items {
    background-color: rgba(255, 255, 255, 0.75);
    color: #1976d2;
    border-radius: 5px;
    flex-basis: 25%;
    margin: 20px;
    cursor: pointer;
    transition: all 0.6s;
  }

  .bar-chart {
    background-color: rgba(255, 255, 255);
    height: 400px;
  }
}

.icon-wrapper,
.project-name {
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-wrapper {
  font-size: 100px;
  transform: translateY(0px);
  transition: all 0.6s;
}

.icon-wrapper {
  align-self: end;
}

.project-name {
  align-self: start;
}

.project-name p {
  font-size: 24px;
  font-weight: bold;
  letter-spacing: 2px;
  transform: translateY(0px);
  transition: all 0.5s;
}

.items:hover {
  border: 3px solid black;

  & .project-name p {
    transform: translateY(-10px);
  }

  & .icon-wrapper i {
    transform: translateY(5px);
  }
}
</style>