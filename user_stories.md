# User Story Template

**Title:**
_As a [user role], I want [feature/goal], so that [reason]._

**Acceptance Criteria:**
1. [Criteria 1]
2. [Criteria 2]
3. [Criteria 3]

**Priority:** [High/Medium/Low]
**Story Points:** [Estimated Effort in Points]
**Notes:**
- [Additional information or edge cases]

## Admin User Stories

### Issue 1

**Issue title:** Admin: Log into the portal

**Description:**

**Title:**
_As an admin, I want to log into the portal with my username and password, so that I can manage the platform securely._

**Acceptance Criteria:**
1. The admin can enter a username and password on the login page.
2. Valid credentials take the admin to the Admin Dashboard.
3. Invalid credentials show an error message and do not grant access.

**Priority:** High
**Story Points:** 3
**Notes:**
- Passwords must be stored hashed, never in plain text.
- Admin-only pages must be protected so they can't be reached without logging in.

---

### Issue 2

**Issue title:** Admin: Log out of the portal

**Description:**

**Title:**
_As an admin, I want to log out of the portal, so that system access is protected._

**Acceptance Criteria:**
1. A logout option is available from every admin page.
2. Logging out ends the session or invalidates the token.
3. After logging out, the admin is sent to the login page and can't reach admin pages with the browser's back button.

**Priority:** High
**Story Points:** 1
**Notes:**
- The session should also expire automatically after a period of inactivity.

---

### Issue 3

**Issue title:** Admin: Add doctors to the portal

**Description:**

**Title:**
_As an admin, I want to add doctors to the portal, so that patients can find them and book appointments._

**Acceptance Criteria:**
1. The admin can open a form to enter a doctor's name, specialization, email, phone number and password.
2. Required fields are validated before the doctor is saved.
3. The new doctor is saved to the database and appears in the doctor list.

**Priority:** High
**Story Points:** 3
**Notes:**
- Adding a doctor whose email already exists must be rejected with a clear message.

---

### Issue 4

**Issue title:** Admin: Delete a doctor's profile

**Description:**

**Title:**
_As an admin, I want to delete a doctor's profile from the portal, so that only active doctors are listed._

**Acceptance Criteria:**
1. The admin can select a doctor from the list and choose to delete them.
2. The admin is asked to confirm before the deletion happens.
3. The doctor is removed from the database and no longer appears in the doctor list.

**Priority:** Medium
**Story Points:** 2
**Notes:**
- Decide how to handle the doctor's existing or future appointments (for example, cancel them and notify patients).

---

### Issue 5

**Issue title:** Admin: Run a stored procedure for monthly appointment statistics

**Description:**

**Title:**
_As an admin, I want to run a stored procedure in the MySQL CLI to get the number of appointments per month, so that I can track usage statistics._

**Acceptance Criteria:**
1. A stored procedure exists in the MySQL database that returns the number of appointments for each month.
2. The admin can run it from the MySQL CLI with a `CALL` statement.
3. The results show the month and the appointment count for that month.

**Priority:** Medium
**Story Points:** 2
**Notes:**
- Months with no appointments may be omitted or shown with a count of 0.
- The procedure could take an optional year parameter.

---

## Patient User Stories

### Issue 6

**Issue title:** Patient: View the list of doctors without logging in

**Description:**

**Title:**
_As a patient, I want to view a list of doctors without logging in, so that I can explore my options before registering._

**Acceptance Criteria:**
1. The doctor list is available on a public page that doesn't require login.
2. Each doctor shows their name, specialization and available times.
3. Patients who aren't logged in are asked to log in or sign up when they try to book.

**Priority:** High
**Story Points:** 2
**Notes:**
- Filtering by name, specialization or available time would be useful.

---

### Issue 7

**Issue title:** Patient: Sign up with email and password

**Description:**

**Title:**
_As a patient, I want to sign up using my email and password, so that I can book appointments._

**Acceptance Criteria:**
1. The sign-up form asks for name, email, password, phone number and address.
2. The email format and password strength are validated.
3. The account is created and the patient can log in straight away.

**Priority:** High
**Story Points:** 3
**Notes:**
- Signing up with an email that's already registered must show an error.

---

### Issue 8

**Issue title:** Patient: Log into the portal

**Description:**

**Title:**
_As a patient, I want to log into the portal, so that I can manage my bookings._

**Acceptance Criteria:**
1. The patient can log in with their email and password.
2. Valid credentials take the patient to their dashboard.
3. Invalid credentials show an error message and do not grant access.

**Priority:** High
**Story Points:** 2
**Notes:**
- Consider limiting repeated failed login attempts.

---

### Issue 9

**Issue title:** Patient: Log out of the portal

**Description:**

**Title:**
_As a patient, I want to log out of the portal, so that my account is secure._

**Acceptance Criteria:**
1. A logout option is available from the patient dashboard.
2. Logging out ends the session or invalidates the token.
3. After logging out, the patient can't reach protected pages until they log in again.

**Priority:** High
**Story Points:** 1
**Notes:**
- Any token stored on the client should be cleared on logout.

---

### Issue 10

**Issue title:** Patient: Book an hour-long appointment

**Description:**

**Title:**
_As a patient, I want to log in and book an hour-long appointment, so that I can consult with a doctor._

**Acceptance Criteria:**
1. A logged-in patient can choose a doctor and one of their available time slots.
2. Every appointment lasts one hour.
3. The booking is saved, and the slot is no longer available to other patients.

**Priority:** High
**Story Points:** 5
**Notes:**
- Two patients must not be able to book the same slot at the same time.
- Appointments can't be booked in the past.

---

### Issue 11

**Issue title:** Patient: View upcoming appointments

**Description:**

**Title:**
_As a patient, I want to view my upcoming appointments, so that I can prepare accordingly._

**Acceptance Criteria:**
1. A logged-in patient can see a list of their upcoming appointments.
2. Each appointment shows the doctor's name, the date and the time.
3. Appointments are sorted by date, with the soonest first.

**Priority:** Medium
**Story Points:** 2
**Notes:**
- Past appointments could be shown in a separate history view.

---

## Doctor User Stories

### Issue 12

**Issue title:** Doctor: Log into the portal

**Description:**

**Title:**
_As a doctor, I want to log into the portal, so that I can manage my appointments._

**Acceptance Criteria:**
1. The doctor can log in with their email and password.
2. Valid credentials take the doctor to the Doctor Dashboard.
3. Invalid credentials show an error message and do not grant access.

**Priority:** High
**Story Points:** 2
**Notes:**
- Doctor accounts are created by an admin. There's no self sign-up.

---

### Issue 13

**Issue title:** Doctor: Log out of the portal

**Description:**

**Title:**
_As a doctor, I want to log out of the portal, so that my data is protected._

**Acceptance Criteria:**
1. A logout option is available from the Doctor Dashboard.
2. Logging out ends the session or invalidates the token.
3. After logging out, the doctor can't reach protected pages until they log in again.

**Priority:** High
**Story Points:** 1
**Notes:**
- The session should also expire automatically after a period of inactivity.

---

### Issue 14

**Issue title:** Doctor: View appointment calendar

**Description:**

**Title:**
_As a doctor, I want to view my appointment calendar, so that I can stay organized._

**Acceptance Criteria:**
1. The doctor can see their appointments organized by date.
2. Each appointment shows the patient's name and the time.
3. The doctor can switch to a different date to see that day's appointments.

**Priority:** High
**Story Points:** 3
**Notes:**
- Show the current day's appointments by default.

---

### Issue 15

**Issue title:** Doctor: Mark unavailability

**Description:**

**Title:**
_As a doctor, I want to mark when I'm unavailable, so that patients only see the slots I'm available for._

**Acceptance Criteria:**
1. The doctor can mark specific dates or time slots as unavailable.
2. Slots marked as unavailable aren't shown to patients when they book.
3. The doctor can remove an unavailability so the slot becomes bookable again.

**Priority:** High
**Story Points:** 3
**Notes:**
- Decide what happens if the doctor marks a slot unavailable that already has an appointment (block it, or warn and require cancellation).

---

### Issue 16

**Issue title:** Doctor: Update profile with specialization and contact information

**Description:**

**Title:**
_As a doctor, I want to update my profile with my specialization and contact information, so that patients have up-to-date information._

**Acceptance Criteria:**
1. The doctor can edit their specialization, phone number and email.
2. Changes are validated before they're saved.
3. The changes are saved and appear in the public doctor list.

**Priority:** Medium
**Story Points:** 2
**Notes:**
- Changing the email must not clash with another account's email.

---

### Issue 17

**Issue title:** Doctor: View patient details for upcoming appointments

**Description:**

**Title:**
_As a doctor, I want to view patient details for my upcoming appointments, so that I can be prepared._

**Acceptance Criteria:**
1. The doctor can open an upcoming appointment to see the patient's name, contact information and relevant history.
2. The doctor can only see details for patients who have appointments with them.
3. Any previous prescriptions for the patient are shown where they exist.

**Priority:** Medium
**Story Points:** 3
**Notes:**
- Patient data is sensitive, so access must be restricted to the assigned doctor.