# Smart Clinic Management System – Schema Design

The system uses two databases:

- **MySQL** holds the clinic's core operational data: patients, doctors, admins, appointments and availability. This data is structured and closely linked, and it needs referential integrity and transactions, for example so that a time slot can't be booked twice.
- **MongoDB** holds data whose shape varies or changes over time, such as prescriptions. Medication lists, notes and pharmacy details differ from one prescription to the next and are easier to model as documents.

## MySQL Database Design

> Conventions: primary keys are `BIGINT AUTO_INCREMENT` to match the `Long` ids in the JPA models. Passwords are always stored as hashes (e.g. BCrypt), never in plain text. Email and phone formats are checked in application code (Bean Validation `@Email`, `@Pattern`), and the database enforces `NOT NULL` and `UNIQUE`.

### Table: admin
- id: BIGINT, Primary Key, Auto Increment
- username: VARCHAR(50), Not Null, Unique
- password: VARCHAR(255), Not Null (hashed)

### Table: doctors
- id: BIGINT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- specialty: VARCHAR(50), Not Null
- email: VARCHAR(100), Not Null, Unique (format checked in code)
- password: VARCHAR(255), Not Null (hashed)
- phone: VARCHAR(10), Not Null (10 digits, checked in code)
- created_at: TIMESTAMP, Not Null, Default CURRENT_TIMESTAMP

### Table: doctor_available_times
- doctor_id: BIGINT, Foreign Key → doctors(id), ON DELETE CASCADE
- available_time: VARCHAR(20), Not Null (a slot such as `"09:00-10:00"`)
- Primary Key: (doctor_id, available_time)

> Each doctor has their own available slots. This table backs the `availableTimes` list in the `Doctor` entity (`@ElementCollection`). When a doctor marks a slot as unavailable, the row is deleted, and patients only see the slots that remain when they book.

### Table: patients
- id: BIGINT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- email: VARCHAR(100), Not Null, Unique (format checked in code)
- password: VARCHAR(255), Not Null (hashed)
- phone: VARCHAR(10), Not Null (10 digits, checked in code)
- address: VARCHAR(255), Not Null
- created_at: TIMESTAMP, Not Null, Default CURRENT_TIMESTAMP

### Table: appointments
- id: BIGINT, Primary Key, Auto Increment
- doctor_id: BIGINT, Not Null, Foreign Key → doctors(id), ON DELETE RESTRICT
- patient_id: BIGINT, Not Null, Foreign Key → patients(id), ON DELETE RESTRICT
- appointment_time: DATETIME, Not Null (start time; every appointment lasts 1 hour, so the end time is `appointment_time + 1 hour`)
- status: INT, Not Null, Default 0 (0 = Scheduled, 1 = Completed, 2 = Cancelled)
- created_at: TIMESTAMP, Not Null, Default CURRENT_TIMESTAMP
- Unique: (doctor_id, appointment_time)
- Index: (patient_id, appointment_time)

> **No overlapping appointments.** Because every appointment is one hour long and starts on a slot boundary, the `UNIQUE (doctor_id, appointment_time)` constraint stops a doctor from being double-booked, even if two patients try to book the same slot at the same moment. The service layer also checks the slot against `doctor_available_times` before saving.
>
> **Deleting a patient or doctor.** Appointments are medical history, so they are **not** cascade-deleted. `ON DELETE RESTRICT` blocks hard deletes while appointments exist. To remove a doctor, the system cancels their future appointments (status = 2) first. Patient accounts are deactivated rather than deleted, so the history stays intact.
>
> **History retention.** Past appointments are kept indefinitely for medical and legal reasons. Completed and cancelled appointments are kept through `status` and never deleted. The monthly-statistics stored procedure also depends on this data.

### Table: payments (optional, future)
- id: BIGINT, Primary Key, Auto Increment
- appointment_id: BIGINT, Not Null, Foreign Key → appointments(id), ON DELETE RESTRICT
- amount: DECIMAL(10,2), Not Null
- method: VARCHAR(20), Not Null (e.g. `CARD`, `CASH`, `INSURANCE`)
- status: INT, Not Null, Default 0 (0 = Pending, 1 = Paid, 2 = Refunded)
- paid_at: DATETIME, Null

> Payments need exact amounts and transactional consistency, so they belong in MySQL rather than MongoDB.

### Relationships summary
- A **doctor** has many **appointments**, and an **appointment** belongs to one doctor.
- A **patient** has many **appointments**, and an **appointment** belongs to one patient.
- A **doctor** has many **available time slots**.
- An **appointment** has zero or one **payment**.
- An **appointment** has zero or more **prescriptions**, which are stored in MongoDB and linked by `appointmentId`.

## MongoDB Collection Design

### Collection: prescriptions

> A prescription is **tied to a specific appointment**: it is written during a consultation, and the link lets a doctor see what was prescribed at each visit. Linking to MySQL is done **by ID** (`appointmentId`, `patientId`, `doctorId`) rather than by embedding the full patient or doctor objects. MySQL remains the single source of truth, so a phone number or address change doesn't leave stale copies in old prescriptions. `patientName` is stored as a small **snapshot** for convenient display, and it intentionally records the name as it was when the prescription was written.

```json
{
  "_id": "ObjectId('64abc123456def7890123456')",
  "appointmentId": 51,
  "patientId": 12,
  "doctorId": 3,
  "patientName": "John Smith",
  "issuedAt": "2026-11-02T10:30:00Z",
  "medications": [
    {
      "name": "Paracetamol",
      "dosage": "500mg",
      "frequency": "Every 6 hours",
      "durationDays": 5,
      "instructions": "Take after meals."
    },
    {
      "name": "Ibuprofen",
      "dosage": "200mg",
      "frequency": "Twice a day",
      "durationDays": 3
    }
  ],
  "doctorNotes": "Patient reports mild fever and headache. Follow up if symptoms persist beyond 5 days.",
  "refillCount": 2,
  "pharmacy": {
    "name": "Walgreens SF",
    "location": "Market Street"
  },
  "tags": ["fever", "pain-relief"],
  "attachments": [
    {
      "fileName": "blood-test-results.pdf",
      "url": "https://storage.example.com/prescriptions/51/blood-test-results.pdf",
      "uploadedAt": "2026-11-02T10:35:00Z"
    }
  ],
  "metadata": {
    "schemaVersion": 2,
    "createdBy": "doctor:3",
    "updatedAt": "2026-11-02T10:40:00Z"
  }
}
```

> **Why MongoDB:** one prescription can list several medications, each with optional fields (`instructions` appears on only one of them above). Pharmacy details, tags and attachments are optional as well. In SQL this would need several join tables and many nullable columns. A single document keeps everything together and reads it in one query.
>
> **Schema evolution:** new optional fields can be added without migrating existing documents. `metadata.schemaVersion` lets the application recognise older document shapes and handle them. In this example, version 1 documents had a single `medication`/`dosage` pair, and version 2 replaced it with the `medications` array.
>
> **Indexes:** `{ appointmentId: 1 }` and `{ patientId: 1, issuedAt: -1 }` support looking up the prescriptions for a visit and a patient's prescription history.

### Collection: messages (optional, future)

> An example of how a doctor–patient chat message could look. Each message is its own document, grouped by `conversationId`, so a conversation can grow without hitting the 16 MB document limit.

```json
{
  "_id": "ObjectId('64abd987654fed3210987654')",
  "conversationId": "doctor:3-patient:12",
  "appointmentId": 51,
  "sender": { "role": "patient", "id": 12 },
  "recipient": { "role": "doctor", "id": 3 },
  "content": "Should I keep taking ibuprofen if the fever is gone?",
  "sentAt": "2026-11-03T08:15:00Z",
  "readAt": null,
  "attachments": []
}
```
