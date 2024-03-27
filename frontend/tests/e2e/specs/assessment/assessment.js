describe('Assessment', () => {
  beforeEach(() => {
    cy.deleteAllButArs();
    cy.populateAssessmentEntities();
  });

  afterEach(() => {
    cy.deleteAllButArs();
  });

  it('create assessment', () => {
    const REVIEW = "Valid review with more than 10 characters"

    cy.demoVolunteerLogin();
    // intercept get activities request
    cy.intercept('GET', '/activities').as('getActivities');
    // intercept get assessment request
    cy.intercept('GET', '/user/assessments').as('getAssessment');
    // intercept get participations request
    cy.intercept('GET', '/user/participations').as('getParticipations');
    // intercept post assessment request
    cy.intercept('POST', '/institutions/*/assessments').as('postAssessment');

    // go to volunteer activities view
    cy.get('[data-cy="volunteerActivities"]').click();

    // wait for the requests to be done
    cy.wait('@getActivities');
    cy.wait('@getAssessment');
    cy.wait('@getParticipations');

    // verify that the table has 6 activities
    cy.get('[data-cy="volunteerActivitiesTable"] tbody tr')
      .should('have.length', 6);

    // verify that the first activity has name A1
    cy.get('[data-cy="volunteerActivitiesTable"] tbody tr')
      .children()
      .eq(0)
      .should('contain', 'A1');

    // assess the first activity
    cy.get('[data-cy="volunteerActivitiesTable"] tbody tr') //open assessment modal
      .eq(0)
      .find('[data-cy="assessmentButton"]').click()

    cy.get('[data-cy="reviewInput"]').type(REVIEW); //fill review

    cy.get('[data-cy="createAssessment"]').click(); //save assessment
    cy.wait('@postAssessment'); //wait for the request to be done
    cy.logout();

    cy.demoMemberLogin()
    // intercept get institution
    cy.intercept('GET', '/users/*/getInstitution').as('getInstitution')
    // intercept get assessments
    cy.intercept('GET', '/institutions/*/assessments').as('getAssessment')

    cy.get('[data-cy="institution"]').click();
    cy.get('[data-cy="assessments"]').click();

    // wait for the requests to be done
    cy.wait('@getInstitution')
    cy.wait('@getAssessment')

    // verify table content
    cy.get('[data-cy="institutionAssessmentsTable"] tbody tr')
      .should('have.length', 1)
      .eq(0)
      .children(0)
      .eq(0)
      .should("contain", REVIEW);

    cy.logout();
  });
});
