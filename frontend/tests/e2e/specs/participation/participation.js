describe('Participation', () => {
    beforeEach(() => {
      cy.deleteAllButArs();
      cy.populateParticipationDatabase();
    });
  
    afterEach(() => {
      // cy.deleteAllButArs();
    });
  
    it('create participations', () => {
      const RATING = '5';
      const FALSE = 'false';
      const TRUE = 'true';
      const ONE = '1';
      const TWO = '2';
  
      cy.demoMemberLogin();
  
      cy.intercept('POST', '/activities/*/participations').as(
        'createParticipation',
      );
  
      // intercept get institutions
      cy.intercept('GET', '/users/*/getInstitution').as('getInstitutions');
      // go to create activity form
      cy.get('[data-cy="institution"]').click();
  
      cy.get('[data-cy="activities"]').click();
      cy.wait('@getInstitutions');
  
      // check if the activity only has one participant
      cy.get('[data-cy="memberActivitiesTable"] tbody tr')
        .should('have.length', 2)
        .eq(0)
        .children()
        .should('have.length', 13)
        .eq(3)
        .should('contain', ONE);
  
      // go to the first activity
      cy.get('[data-cy="memberActivitiesTable"] tbody tr')
        .first()
        .find('[data-cy="showEnrollments"]')
        .click();
  
      // check if the enrollments table has two instances
      cy.get('[data-cy="activityEnrollmentsTable"] tbody tr')
        .should('have.length', 2)
        .eq(0)
        .children()
        .should('have.length', 5);
  
      // check the first enrollment has Participating has false
      cy.get('[data-cy="activityEnrollmentsTable"] tbody tr')
        .eq(0)
        .children()
        .eq(3)
        .should('contain', FALSE);
  
      // create a participation for said enrollment
      cy.get('[data-cy="activityEnrollmentsTable"] tbody tr')
        .eq(0)
        .find('[data-cy="createParticipation"]')
        .click();
  
      // fill form
      cy.get('[data-cy="ratingInput"]').type(RATING);
  
      // save form
      cy.get('[data-cy="saveParticipation"]').click();
      // check request was done
      cy.wait('@createParticipation');
  
      cy.get('[data-cy="activityEnrollmentsTable"] tbody tr')
        .eq(0)
        .children()
        .eq(3)
        .invoke('text')
        .then((text) => {
          console.log(text);
        });
  
      // check the first enrollment has Participating has true
      cy.get('[data-cy="activityEnrollmentsTable"] tbody tr')
        .eq(0)
        .children()
        .eq(3)
        .should('contain', TRUE);
  
      // travel back to the activities view
      cy.get('[data-cy="activityEnrollmentsTable"]')
        .find('[data-cy="getActivities"]')
        .click();
      cy.wait('@getInstitutions');
  
      // check the first activity now has 2 participants
      cy.get('[data-cy="memberActivitiesTable"] tbody tr')
        .should('have.length', 2)
        .eq(0)
        .children()
        .should('have.length', 12)
        .eq(3)
        .should('contain', TWO);
  
      cy.logout();
    });
  });
  