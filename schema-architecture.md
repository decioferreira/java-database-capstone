# Smart Clinic Management System – Architecture Design

## Section 1: Architecture Summary

The Smart Clinic Management System is a Spring Boot application built on a three-tier architecture made up of a presentation tier, an application tier and a data tier. It uses two kinds of controllers. Spring MVC controllers with Thymeleaf templates render server-side HTML for the Admin and Doctor dashboards. REST controllers expose JSON APIs for the other modules, such as Appointments, the Patient Dashboard and Patient Records. Because those modules use REST, mobile apps, future frontends and third-party integrations can also call the same backend over HTTP.

The application uses two databases. MySQL, accessed through Spring Data JPA, stores structured relational data: patients, doctors, appointments and admins. MongoDB, accessed through Spring Data MongoDB, stores prescriptions, which are flexible documents. Every controller sends its requests through a common service layer. The service layer applies business rules and validation, coordinates work that involves several entities, and calls the matching repositories. MySQL data is mapped to JPA entities (`@Entity`) and MongoDB data is mapped to document models (`@Document`). This keeps the layers separate, so the application is easier to maintain, test and scale.

## Section 2: Numbered Flow of Data and Control

1. **User Interface Layer:** A user opens a Thymeleaf-based dashboard (AdminDashboard or DoctorDashboard), or a REST client (such as the Appointments, PatientDashboard or PatientRecord modules, or a mobile app) sends an HTTP request to the backend.
2. **Controller Layer:** Spring routes the request to a controller based on its URL path and HTTP method. Requests for server-rendered pages go to Thymeleaf (MVC) controllers. API requests go to REST controllers. The controllers validate the input and manage the request/response flow.
3. **Service Layer:** The controller passes the work to the service layer. The service layer applies business rules and validation and coordinates workflows across entities, for example checking that a doctor is available before booking an appointment.
4. **Repository Layer:** The service layer calls the matching repository. MySQL repositories (Spring Data JPA) handle patients, doctors, appointments and admins. The MongoDB repository (Spring Data MongoDB) handles prescriptions.
5. **Database Access:** Each repository talks to its database. MySQL holds the core entities in a normalized relational schema with constraints. MongoDB holds prescriptions as flexible, nested documents whose schema can change over time.
6. **Model Binding:** The data that comes back is mapped to Java model classes. MySQL rows become JPA entities annotated with `@Entity`, and MongoDB documents become objects annotated with `@Document`. Both give the rest of the application the same object-oriented view of the data.
7. **Application Models in Use:** The models go back to the response layer. In MVC flows, the controller passes them to Thymeleaf templates, which render dynamic HTML for the browser. In REST flows, the models (or DTOs built from them) are serialized to JSON and returned in the HTTP response. This ends the request-response cycle.
