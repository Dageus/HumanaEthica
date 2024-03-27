const credentials = {
  user: Cypress.env('psql_db_username'),
  host: Cypress.env('psql_db_host'),
  database: Cypress.env('psql_db_name'),
  password: Cypress.env('psql_db_password'),
  port: Cypress.env('psql_db_port'),
};

const INSTITUTION_COLUMNS = "institutions (id, active, confirmation_token, creation_date, email, name, nif, token_generation_date)";
const USER_COLUMNS = "users (user_type, id, creation_date, name, role, state, institution_id)";
const AUTH_USERS_COLUMNS = "auth_users (auth_type, id, active, email, username, user_id)";

const now = new Date();
const tomorrow = new Date(now);
tomorrow.setDate(now.getDate() + 1);
const dayAfterTomorrow = new Date(now);
dayAfterTomorrow.setDate(now.getDate() + 2);
const yesterday = new Date(now);
yesterday.setDate(now.getDate() - 1);
const dayBeforeYesterday = new Date(now);
dayBeforeYesterday.setDate(now.getDate() - 2);

Cypress.Commands.add('deleteAllButArs', () => {
  cy.task('queryDatabase', {
    query: "DELETE FROM ENROLLMENT",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM PARTICIPATION",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM ACTIVITY",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM ASSESSMENT",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM AUTH_USERS WHERE NOT (username = 'ars')",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM USERS WHERE NOT (name = 'ars')",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM INSTITUTIONS",
    credentials: credentials,
  });
});

Cypress.Commands.add('createDemoEntities', () => {
  cy.task('queryDatabase', {
    query: "INSERT INTO " + INSTITUTION_COLUMNS + generateInstitutionTuple(1),
    credentials: credentials,
  })
  cy.task('queryDatabase', {
    query: "INSERT INTO " + USER_COLUMNS + generateUserTuple(2, "MEMBER", "DEMO-MEMBER", "MEMBER", 1),
    credentials: credentials,
  })
  cy.task('queryDatabase', {
    query: "INSERT INTO " + AUTH_USERS_COLUMNS + generateAuthUserTuple(2, "DEMO", "demo-member", 2),
    credentials: credentials,
  })
  cy.task('queryDatabase', {
    query: "INSERT INTO " + USER_COLUMNS + generateUserTuple(3, "VOLUNTEER", "DEMO-VOLUNTEER", "VOLUNTEER", "NULL"),
    credentials: credentials,
  })
  cy.task('queryDatabase', {
    query: "INSERT INTO " + AUTH_USERS_COLUMNS + generateAuthUserTuple(3, "DEMO", "demo-volunteer", 3),
    credentials: credentials,
  })
});

Cypress.Commands.add('populateAssessmentEntities', () => {
  cy.task('queryDatabase', {
    query: `
      INSERT INTO public.institutions (id, active, confirmation_token, creation_date, email, name, nif, token_generation_date)
      VALUES
      (1, true, 'abca428c09862e89', '2024-02-06 17:58:21.402146', 'demo_institution@mail.com', 'DEMO INSTITUTION', '000000000', '2024-02-06 17:58:21.402134'),
      (2, true, 'abca428c09862e89', '2024-02-06 17:58:21.402146', 'demo_institution@mail.com', 'DEMO INSTITUTION-2', '000000002', '2024-02-06 17:58:21.402134')
    `,
    credentials: credentials,
  });

  cy.task('queryDatabase', {
    query: `
      INSERT INTO public.users (user_type, id, creation_date, name, role, state, institution_id)
      VALUES
      ('MEMBER', 2, '2024-02-06 17:58:21.419878', 'DEMO-MEMBER', 'MEMBER', 'ACTIVE', 1),
      ('VOLUNTEER', 3, '2024-02-06 17:58:23.732513', 'DEMO-VOLUNTEER', 'VOLUNTEER', 'ACTIVE', NULL)
    `,
    credentials: credentials,
  });

  cy.task('queryDatabase', {
    query: `
      INSERT INTO public.auth_users (auth_type, id, active, email, last_access, password, username, confirmation_token, token_generation_date, user_id)
      VALUES
      ('DEMO', 2, true, 'demo_member@mail.com', NULL, NULL, 'demo-member', NULL, NULL, 2),
      ('DEMO', 3, true, 'demo_volunteer@mail.com', NULL, NULL, 'demo-volunteer', NULL, NULL, 3)
    `,
    credentials: credentials,
  });

  cy.task('queryDatabase', {
    query: `
      INSERT INTO public.activity (id, application_deadline, creation_date, description, ending_date, name, participants_number_limit, region, starting_date, state, institution_id)
      VALUES
      (1, '2024-02-06 17:58:21.402146', '2024-02-06 17:58:21.402146', 'Same institution is enrolled and participates', '2024-02-08 10:58:21.402146', 'A1', 1, 'Lisbon', '2024-02-07 17:58:21.402146', 'APPROVED', 1),
      (2, '2024-02-06 17:58:21.402146', '2024-02-06 17:58:21.402146', 'Same institution is enrolled and participates', '2024-02-08 10:58:21.402146', 'A2', 1, 'Lisbon', '2024-02-07 17:58:21.402146', 'APPROVED', 1),
      (3, '2024-02-06 17:58:21.402146', '2024-02-06 17:58:21.402146', 'Same institution is enrolled and does not participate', '2024-02-08 10:58:21.402146', 'A3', 2, 'Lisbon', '2024-02-07 17:58:21.402146', 'APPROVED', 1),
      (4, '2024-02-06 17:58:21.402146', '2024-02-06 17:58:21.402146', 'Same institution is not enrolled', '2024-02-08 10:58:21.402146', 'A4', 2, 'Lisbon', '2024-02-07 17:58:21.402146', 'APPROVED', 1),
      (5, '2024-02-06 17:58:21.402146', '2024-02-06 17:58:21.402146', 'Same institution before end date', '2024-08-08 10:58:21.402146', 'A5', 2, 'Lisbon', '2024-02-07 17:58:21.402146', 'APPROVED', 1),
      (6, '2024-02-06 17:58:21.402146', '2024-02-06 17:58:21.402146', 'Other institution is enrolled and participates', '2024-02-08 10:58:21.402146', 'A6', 3, 'Lisbon', '2024-02-07 17:58:21.402146', 'APPROVED', 2)
    `,
    credentials: credentials,
  });

  cy.task('queryDatabase', {
    query: `
      INSERT INTO public.enrollment (id, enrollment_date_time, motivation, activity_id, volunteer_id)
      VALUES
      (1, '2024-02-06 18:51:37.595713', 'sql-inserted-motivation', 1, 3),
      (2, '2024-02-06 18:51:37.595713', 'sql-inserted-motivation', 2, 3),
      (3, '2024-02-06 18:51:37.595713', 'sql-inserted-motivation', 3, 3),
      (4, '2024-02-06 18:51:37.595713', 'sql-inserted-motivation', 6, 3)
    `,
    credentials: credentials,
  });

  cy.task('queryDatabase', {
    query: `
      INSERT INTO public.participation (id, acceptance_date, rating, activity_id, volunteer_id)
      VALUES
      (1, '2024-02-06 18:51:37.595713', 5, 1, 3),
      (2, '2024-02-06 18:51:37.595713', 5, 2, 3),
      (3, '2024-02-06 18:51:37.595713', 5, 6, 3)
    `,
    credentials: credentials,
  });
});



function generateAuthUserTuple(id, authType, username, userId) {
  return "VALUES ('"
    + authType + "', '"
    + id + "', 't', 'demo_member@mail.com','"
    + username + "', '"
    + userId + "')"
}

function generateUserTuple(id, userType, name, role, institutionId) {
  return "VALUES ('"
    + userType + "', '"
    + id + "', '2022-02-06 17:58:21.419878', '"
    + name + "', '"
    + role + "', 'ACTIVE', "
    + institutionId + ")";
}

function generateInstitutionTuple(id) {
  return "VALUES ('"
    + id + "', 't', 'abca428c09862e89', '2022-08-06 17:58:21.402146','demo_institution@mail.com', 'DEMO INSTITUTION', '000000000', '2024-02-06 17:58:21.402134')";
}