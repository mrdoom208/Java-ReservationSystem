# Reservation System Monitoring

Reservation System Monitoring is a desktop administration application built with JavaFX and Spring Boot for managing restaurant reservations, messaging workflows, permissions, reporting, and live operational updates.

The application combines a JavaFX client UI with Spring-managed services, database persistence, SMS integration, and WebSocket-based real-time synchronization.

## Features

- Dashboard for reservation and operational monitoring
- Reservation and table management workflows
- Messaging template management and SMS delivery integration
- Permission-based staff access control
- Activity logs and reporting views
- Settings screen for system, messaging, database, and permission configuration
- Real-time updates through WebSocket/STOMP connectivity

## Technology Stack

- Java 21
- JavaFX 21
- Spring Boot 3.2
- Spring Data JPA
- PostgreSQL or MySQL
- MaterialFX
- BootstrapFX
- jSerialComm
- Java-WebSocket / STOMP WebSocket client
- Maven

## Project Structure

```text
src/main/java/com/mycompany/reservationsystem
|-- config/        Application configuration and local settings helpers
|-- controller/    JavaFX controllers for main views and popup dialogs
|-- dto/           Data transfer objects for reports and WebSocket payloads
|-- hardware/      Serial device detection and communication logic
|-- model/         JPA entities
|-- repository/    Spring Data repositories
|-- service/       Business logic and integrations
|-- transition/    UI animation helpers
|-- util/          Shared UI and application utilities
|-- websocket/     WebSocket client and live update handlers
```

## Requirements

- JDK 21
- Maven 3.8+
- PostgreSQL or MySQL database
- Network access to the configured website and WebSocket endpoints

## Configuration

The application reads its main configuration from [application.properties](/C:/Users/John%20Estorca/Desktop/Reservation/ReservationSystemMonitoring/src/main/resources/application.properties).

Recommended environment variables:

```properties
DATABASE_URL=jdbc:postgresql://localhost:5432/reservation_system
DB_USER=your_db_user
DB_PASSWORD=your_db_password
```

Important application properties:

```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}

philsms.api.token=your_token
philsms.sender.id=your_sender_id

website.url=https://your-web-app.example.com
server.url=ws://your-websocket-endpoint/ws
```

## Security Notes

- Do not commit real API tokens, database passwords, or production endpoints to source control.
- Prefer environment variables or externalized configuration for secrets.
- If the WebSocket endpoint is exposed behind HTTPS/TLS, use `wss://` instead of `ws://`.

## Running Locally

1. Install Java 21 and Maven.
2. Configure your database connection and external service properties.
3. From the project root, run:

```bash
mvn clean javafx:run
```

Alternatively:

```bash
mvn clean package
```

Then run the packaged application using your preferred Java command or IDE launch configuration.

## Windows Packaging

The project includes opt-in Maven profiles for Windows packaging through `jpackage`.

Requirements:

- Windows
- JDK 21
- `JAVA_HOME` set to the JDK that should be bundled into the installer

Command:

```bash
mvn clean verify -Pjpackage
```

Output:

- `target/jpackage/ReservationSystem/` for the app-image

To build the Windows installer too, install WiX Toolset and run:

```bash
mvn clean verify -Pjpackage,jpackage-exe
```

Additional output with WiX installed:

- `target/jpackage/ReservationSystem-1.0.exe` for the installer

Packaging metadata:

- Name: `ReservationSystem`
- Version: `1.0`
- Vendor: `Romantic Baboy`
- Description: `ReservationSystem for Romantic Baboy`
- Icon: `src/main/resources/Images/icon.ico`

The generated installer bundles a Java runtime, so other Windows computers do not need a separate Java installation to run the app. They will still need access to the configured database, WebSocket endpoint, and any external SMS or website services your `application.properties` points to.

## Database Notes

- JPA auto-update is enabled through:

```properties
spring.jpa.hibernate.ddl-auto=update
```

- The current configuration targets PostgreSQL by default.
- Commented properties in `application.properties` show an alternative MySQL setup.

## Real-Time Updates

The application opens a STOMP WebSocket connection on startup from the main administrator UI. Incoming messages are routed to update handlers for live reservation and monitoring behavior.

Related source files:

- [AdministratorUIController.java](/C:/Users/John%20Estorca/Desktop/Reservation/ReservationSystemMonitoring/src/main/java/com/mycompany/reservationsystem/controller/main/AdministratorUIController.java)
- [WebSocketClient.java](/C:/Users/John%20Estorca/Desktop/Reservation/ReservationSystemMonitoring/src/main/java/com/mycompany/reservationsystem/websocket/WebSocketClient.java)
- [WebUpdateHandlerImpl.java](/C:/Users/John%20Estorca/Desktop/Reservation/ReservationSystemMonitoring/src/main/java/com/mycompany/reservationsystem/websocket/WebUpdateHandlerImpl.java)

## SMS Integration

SMS sending is handled through the PhilSMS integration in:

- [SmsService.java](/C:/Users/John%20Estorca/Desktop/Reservation/ReservationSystemMonitoring/src/main/java/com/mycompany/reservationsystem/service/SmsService.java)

Ensure the sender ID and API token are configured correctly before enabling SMS features in the Settings screen.

## Development Notes

- Controllers are managed through Spring's controller factory.
- The JavaFX application bootstraps Spring Boot in [App.java](/C:/Users/John%20Estorca/Desktop/Reservation/ReservationSystemMonitoring/src/main/java/com/mycompany/reservationsystem/App.java).
- User interface files are stored in `src/main/resources/fxml`.
- Styling is defined in `src/main/resources/css/style.css`.

## Recommended Next Improvements

- Move secrets fully out of `application.properties`
- Add a proper logging framework configuration instead of console prints
- Add unit and integration tests for services and controllers
- Add a packaged release workflow for desktop distribution

## License

No license file is currently included in this repository. Add one before public distribution.
