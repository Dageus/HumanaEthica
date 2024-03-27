describe('Enrollment e2e Test', () => {
  beforeEach(() => {
    cy.deleteAllButArs();
    cy.createDemoEntities();
    cy.populateDatabaseFromDumpEnrollment();
  });

  it('verify, as member, if activity table has 3 instances and the first one has 0 Applications', () => {
    const ZERO = '0';
    const MOTIVATION = 'I love working with children';
    const ONE = '1';

    cy.demoMemberLogin();

    cy.intercept('GET', '/users/*/getInstitution').as('getInstitutions');

    cy.get('[data-cy="institution"]').click();
    cy.get('[data-cy="activities"]').click();
    cy.wait('@getInstitutions');

    // Check that the activities table has 3 instances
    cy.get('[data-cy="memberActivitiesTable"] tbody tr').should(
      'have.length',
      3,
    );

    // Check if the first activity has 0 Applications
    cy.get('[data-cy="memberActivitiesTable"] tbody tr')
      .eq(0)
      .children()
      .eq(9)
      .should('contain', ZERO);
      
    // Change for volunteer
    cy.logout();
    cy.demoVolunteerLogin();
    
    // Apply to the first activity
    cy.intercept('POST', '/activities/*/enrollments').as('createEnrollment');

    cy.intercept('GET', '/activities').as('getActivities');
    cy.get('[data-cy="volunteerActivities"]').click();
    cy.wait('@getActivities');
    
    cy.get('[data-cy="volunteerActivitiesTable"] tbody tr')
      .eq(0)
      .children()
      .find('[data-cy="applyButton"]')
      .click();
    cy.get('[data-cy="motivation"]').type(MOTIVATION);

    cy.get('[data-cy="saveEnrollment"]').click();
    cy.wait('@createEnrollment');
    
    // Change for member
    cy.logout();
    cy.demoMemberLogin();
    
    cy.intercept('GET', '/users/*/getInstitution').as('getInstitutions');

    cy.get('[data-cy="institution"]').click();
    cy.get('[data-cy="activities"]').click();
    cy.wait('@getInstitutions');

    // Check that the first activity's Applications is now 1
    cy.get('[data-cy="memberActivitiesTable"] tbody tr')
      .eq(0)
      .children()
      .eq(9)
      .should('contain', ONE);
    
    // Check if the first activity's Applications has the correct Motivation
    cy.get('[data-cy="memberActivitiesTable"] tbody tr')
      .eq(0)
      .find('[data-cy="showEnrollments"]')
      .click();

    cy.get('[data-cy="activityEnrollmentsTable"] tbody tr').should(
      'have.length',
      1,
    );
    
    cy.get('[data-cy="activityEnrollmentsTable"] tbody tr')
      .eq(0)
      .children()
      .eq(0)
      .should('contain', MOTIVATION);
  });
});
