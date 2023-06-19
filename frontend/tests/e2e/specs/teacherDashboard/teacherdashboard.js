describe('TeacherDashboard', () => {

    before(() => {
        cy.request('http://localhost:8080/auth/demo/teacher')
            .as('loginResponse')
            .then((response) => {
                Cypress.env('token', response.body.token);
                return response;
            });

        cy.intercept('GET', '**/teachers/dashboards/executions/*').as(
            'getDashboard'
        );

        cy.createDemoCourse2();

        cy.demoTeacherLogin();

        cy.removeDemoCourseExecution();

        cy.createCourseExecutionOnDemoCourse2('1st Semester 2019/2020', '2019-12-31 10:15:30');
        cy.addDemoTeacherCourseExecution('1st Semester 2019/2020');

        cy.createCourseExecutionOnDemoCourse2('1st Semester 2022/2023', '2022-12-31 10:15:30');
        cy.addDemoTeacherCourseExecution('1st Semester 2022/2023');

        cy.createCourseExecutionOnDemoCourse2('1st Semester 2023/2024', '2023-12-31 10:15:30');
        cy.addDemoTeacherCourseExecution('1st Semester 2023/2024');

        cy.contains('Logout').click();
        cy.demoTeacherLogin();

        cy.changeCoursetoCourseExecution(2);
        cy.changeCoursetoCourseExecution(1);
        cy.changeCoursetoCourseExecution(0);

        cy.addStudentStats('1st Semester 2019/2020', 34, 21, 18);
        cy.addQuizStats('1st Semester 2019/2020', 28, 17, 6);
        cy.addQuestionStats('1st Semester 2019/2020', 49, 35, 19);

        cy.addStudentStats('1st Semester 2022/2023', 20, 13, 17);
        cy.addQuizStats('1st Semester 2022/2023', 22, 12, 12);
        cy.addQuestionStats('1st Semester 2022/2023', 30, 21, 17);

        cy.addStudentStats('1st Semester 2023/2024', 10, 5, 5);
        cy.addQuizStats('1st Semester 2023/2024', 22, 7, 5);
        cy.addQuestionStats('1st Semester 2023/2024', 14, 11, 3);

        cy.contains('Logout').click();

    });

    beforeEach(() => {
        cy.demoTeacherLogin();
    });

    afterEach(() => {
        cy.contains('Logout').click();
    });

    after(() => {
        cy.addDemoCourseExecution();
        cy.deleteCourseExecutions();
    });

    it('verify if stats are correct for 2023/2024', () => {
        cy.intercept('GET', '**/teachers/dashboards/executions/*').as(
            'getDashboard'
        );

        cy.changeCoursetoCourseExecution(0);

        cy.get('[data-cy="numStudents"]').invoke('text').should('eq', '10');
        cy.get('[data-cy="numMore75CorrectQuestions"]').invoke('text').should('eq', '5');
        cy.get('[data-cy="numAtLeast3Quizzes"]').invoke('text').should('eq', '5');

        cy.get('[data-cy="numQuizzes"]').invoke('text').should('eq', '22');
        cy.get('[data-cy="numUniqueAnsweredQuizzes"]').invoke('text').should('eq', '7');
        cy.get('[data-cy="averageQuizzesSolved"]').invoke('text').should('eq', '5');

        cy.get('[data-cy="numAvailableQuestions"]').invoke('text').should('eq', '14');
        cy.get('[data-cy="answeredQuestionsUnique"]').invoke('text').should('eq', '11');
        cy.get('[data-cy="averageQuestionsAnswered"]').invoke('text').should('eq', '3');

        cy.get('#studentGraph', { timeout: 10000 })
            .scrollIntoView()
            .wait(5000)
            .screenshot('studentGraph_2023')
            .should('be.visible')
            .then(chart => {
                    expect(chart.height()).to.be.greaterThan(350);
            });

        cy.get('#quizGraph', { timeout: 10000 })
            .scrollIntoView()
            .wait(5000)
            .screenshot('quizGraph_2023')
            .should('be.visible')
            .then(chart => {
                    expect(chart.height()).to.be.greaterThan(350);
            });

        cy.get('#questionGraph', { timeout: 10000 })
            .scrollIntoView()
            .wait(5000)
            .screenshot('questionGraph_2023')
            .should('be.visible')
            .then(chart => {
                    expect(chart.height()).to.be.greaterThan(350);
            });


        const PNG = require('pngjs').PNG;
        const pixelmatch = require('pixelmatch');

        cy.readFile(
            './tests/e2e/specs/teacherDashboard/base-screenshots/expected/studentGraph_2023.png','base64'
        ).then(baseImage => {
            cy.readFile(
            './tests/e2e/screenshots/teacherDashboard/teacherdashboard.js/studentGraph_2023.png','base64'
            ).then(studentsGraphImage => {

                const img1 = PNG.sync.read(Buffer.from(baseImage, 'base64'));
                const img2 = PNG.sync.read(Buffer.from(studentsGraphImage, 'base64'));

                const { width, height } = img1;
                const diff = new PNG({ width, height });

                const numDiffPixels = pixelmatch(img1.data, img2.data, diff.data, width, height, { threshold: 0.1 });

                const diffPercent = (numDiffPixels / (width * height) * 100);

                cy.log(`Found a ${diffPercent.toFixed(2)}% pixel difference`);

                expect(diffPercent).to.be.below(10);
            });

        });

        cy.readFile(
            './tests/e2e/specs/teacherDashboard/base-screenshots/expected/quizGraph_2023.png','base64'
        ).then(baseImage => {
            cy.readFile(
            './tests/e2e/screenshots/teacherDashboard/teacherdashboard.js/quizGraph_2023.png','base64'
            ).then(studentsGraphImage => {

                const img1 = PNG.sync.read(Buffer.from(baseImage, 'base64'));
                const img2 = PNG.sync.read(Buffer.from(studentsGraphImage, 'base64'));

                const { width, height } = img1;
                const diff = new PNG({ width, height });

                const numDiffPixels = pixelmatch(img1.data, img2.data, diff.data, width, height, { threshold: 0.1 });

                const diffPercent = (numDiffPixels / (width * height) * 100);

                cy.log(`Found a ${diffPercent.toFixed(2)}% pixel difference`);

                expect(diffPercent).to.be.below(10);
            });

        });

        cy.readFile(
            './tests/e2e/specs/teacherDashboard/base-screenshots/expected/questionGraph_2023.png','base64'
        ).then(baseImage => {
            cy.readFile(
            './tests/e2e/screenshots/teacherDashboard/teacherdashboard.js/questionGraph_2023.png','base64'
            ).then(studentsGraphImage => {

                const img1 = PNG.sync.read(Buffer.from(baseImage, 'base64'));
                const img2 = PNG.sync.read(Buffer.from(studentsGraphImage, 'base64'));

                const { width, height } = img1;
                const diff = new PNG({ width, height });

                const numDiffPixels = pixelmatch(img1.data, img2.data, diff.data, width, height, { threshold: 0.1 });

                const diffPercent = (numDiffPixels / (width * height) * 100);

                cy.log(`Found a ${diffPercent.toFixed(2)}% pixel difference`);

                expect(diffPercent).to.be.below(10);
            });

        });

    });

    it('verify if stats are correct for 2022/2023', () => {
        cy.intercept('GET', '**/teachers/dashboards/executions/*').as(
            'getDashboard'
        );

        cy.changeCoursetoCourseExecution(1);

        cy.get('[data-cy="numStudents"]').invoke('text').should('eq', '20');
        cy.get('[data-cy="numMore75CorrectQuestions"]').invoke('text').should('eq', '13');
        cy.get('[data-cy="numAtLeast3Quizzes"]').invoke('text').should('eq', '17');

        cy.get('[data-cy="numQuizzes"]').invoke('text').should('eq', '22');
        cy.get('[data-cy="numUniqueAnsweredQuizzes"]').invoke('text').should('eq', '12');
        cy.get('[data-cy="averageQuizzesSolved"]').invoke('text').should('eq', '12');

        cy.get('[data-cy="numAvailableQuestions"]').invoke('text').should('eq', '30');
        cy.get('[data-cy="answeredQuestionsUnique"]').invoke('text').should('eq', '21');
        cy.get('[data-cy="averageQuestionsAnswered"]').invoke('text').should('eq', '17');

        cy.get('#studentGraph', { timeout: 10000 })
            .scrollIntoView()
            .wait(5000)
            .screenshot('studentGraph_2022')
            .should('be.visible')
            .then(chart => {
                    expect(chart.height()).to.be.greaterThan(350);
            });

        cy.get('#quizGraph', { timeout: 10000 })
            .scrollIntoView()
            .wait(5000)
            .screenshot('quizGraph_2022')
            .should('be.visible')
            .then(chart => {
                    expect(chart.height()).to.be.greaterThan(350);
            });

        cy.get('#questionGraph', { timeout: 10000 })
            .scrollIntoView()
            .wait(5000)
            .screenshot('questionGraph_2022')
            .should('be.visible')
            .then(chart => {
                    expect(chart.height()).to.be.greaterThan(350);
            });

        const PNG = require('pngjs').PNG;
        const pixelmatch = require('pixelmatch');

        cy.readFile(
            './tests/e2e/specs/teacherDashboard/base-screenshots/expected/studentGraph_2022.png','base64'
        ).then(baseImage => {
            cy.readFile(
            './tests/e2e/screenshots/teacherDashboard/teacherdashboard.js/studentGraph_2022.png','base64'
            ).then(studentsGraphImage => {

                const img1 = PNG.sync.read(Buffer.from(baseImage, 'base64'));
                const img2 = PNG.sync.read(Buffer.from(studentsGraphImage, 'base64'));

                const { width, height } = img1;
                const diff = new PNG({ width, height });

                const numDiffPixels = pixelmatch(img1.data, img2.data, diff.data, width, height, { threshold: 0.1 });

                const diffPercent = (numDiffPixels / (width * height) * 100);

                cy.log(`Found a ${diffPercent.toFixed(2)}% pixel difference`);

                expect(diffPercent).to.be.below(10);
            });

        });

        cy.readFile(
            './tests/e2e/specs/teacherDashboard/base-screenshots/expected/quizGraph_2022.png','base64'
        ).then(baseImage => {
            cy.readFile(
            './tests/e2e/screenshots/teacherDashboard/teacherdashboard.js/quizGraph_2022.png','base64'
            ).then(studentsGraphImage => {

                const img1 = PNG.sync.read(Buffer.from(baseImage, 'base64'));
                const img2 = PNG.sync.read(Buffer.from(studentsGraphImage, 'base64'));

                const { width, height } = img1;
                const diff = new PNG({ width, height });

                const numDiffPixels = pixelmatch(img1.data, img2.data, diff.data, width, height, { threshold: 0.1 });

                const diffPercent = (numDiffPixels / (width * height) * 100);

                cy.log(`Found a ${diffPercent.toFixed(2)}% pixel difference`);

                expect(diffPercent).to.be.below(10);
            });

        });

        cy.readFile(
            './tests/e2e/specs/teacherDashboard/base-screenshots/expected/questionGraph_2022.png','base64'
        ).then(baseImage => {
            cy.readFile(
            './tests/e2e/screenshots/teacherDashboard/teacherdashboard.js/questionGraph_2022.png','base64'
            ).then(studentsGraphImage => {

                const img1 = PNG.sync.read(Buffer.from(baseImage, 'base64'));
                const img2 = PNG.sync.read(Buffer.from(studentsGraphImage, 'base64'));

                const { width, height } = img1;
                const diff = new PNG({ width, height });

                const numDiffPixels = pixelmatch(img1.data, img2.data, diff.data, width, height, { threshold: 0.1 });

                const diffPercent = (numDiffPixels / (width * height) * 100);

                cy.log(`Found a ${diffPercent.toFixed(2)}% pixel difference`);

                expect(diffPercent).to.be.below(10);
            });

        });
    });

    it('verify if stats are correct for 2019/2020', () => {
        cy.intercept('GET', '**/teachers/dashboards/executions/*').as(
            'getDashboard'
        );

        cy.changeCoursetoCourseExecution(2);

        cy.get('[data-cy="numStudents"]').invoke('text').should('eq', '34');
        cy.get('[data-cy="numMore75CorrectQuestions"]').invoke('text').should('eq', '21');
        cy.get('[data-cy="numAtLeast3Quizzes"]').invoke('text').should('eq', '18');

        cy.get('[data-cy="numQuizzes"]').invoke('text').should('eq', '28');
        cy.get('[data-cy="numUniqueAnsweredQuizzes"]').invoke('text').should('eq', '17');
        cy.get('[data-cy="averageQuizzesSolved"]').invoke('text').should('eq', '6');

        cy.get('[data-cy="numAvailableQuestions"]').invoke('text').should('eq', '49');
        cy.get('[data-cy="answeredQuestionsUnique"]').invoke('text').should('eq', '35');
        cy.get('[data-cy="averageQuestionsAnswered"]').invoke('text').should('eq', '19');
        
        cy.get('.scrollbar').scrollTo('bottom') 
        cy.screenshot('Graph_2019');
        
        const PNG = require('pngjs').PNG;
        const pixelmatch = require('pixelmatch');
    
        cy.readFile(
            './tests/e2e/specs/teacherDashboard/base-screenshots/expected/Graph_2019.png','base64'
        ).then(baseImage => {
            cy.readFile(
            './tests/e2e/screenshots/teacherDashboard/teacherdashboard.js/Graph_2019.png','base64'
            ).then(GraphImage => {
                const img1 = PNG.sync.read(Buffer.from(baseImage, 'base64'));
                const img2 = PNG.sync.read(Buffer.from(GraphImage, 'base64'));

                const { width, height } = img1;
                const diff = new PNG({ width, height });

                const numDiffPixels = pixelmatch(img1.data, img2.data, diff.data, width, height, { threshold: 0.1 });
                const diffPercent = (numDiffPixels / (width * height) * 100);

                cy.log(`Found a ${diffPercent.toFixed(2)}% pixel difference`);

                expect(diffPercent).to.be.below(10);
            });

        });
    });

});