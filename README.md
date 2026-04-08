# Reservation System

A full-featured restaurant reservation management system for **Romantic Baboy**, consisting of a Spring Boot backend API with web registration portal and a JavaFX desktop application for staff operations.

## Project Structure

```
ReservationSystem/
├── server/       Spring Boot backend API + web registration portal
└── staff-app/    JavaFX desktop application for staff management
```

## Features

### Backend API (Server)
- REST API for reservation management
- Web registration portal for customers (Thymeleaf templates)
- WebSocket server for real-time updates
- SMS notification integration
- Email notification support
- Activity logging and reporting
- Spring Security authentication
- PostgreSQL database integration

### Staff Desktop App
- Dashboard with reservation and operational monitoring
- Reservation and table management workflows
- Messaging template management
- Permission-based staff access control
- Activity logs and reporting views
- Settings configuration (system, messaging, database)
- Real-time updates via WebSocket
- QR code generation for reservations
- Serial port hardware integration support

## Technology Stack

### Server
- Java 21
- Spring Boot 3.2.1
- Spring Data JPA
- Spring Security
- Spring WebSocket
- Thymeleaf (web UI)
- PostgreSQL
- ZXing (QR codes)
- Maven

### Staff App
- Java 21
- JavaFX 21
- Spring Boot 3.2.1
- MaterialFX
- BootstrapFX
- FontAwesome5 Icons
- Apache HTTP Client
- jSerialComm (hardware)
- ZXing (QR codes)
- Maven

## Requirements

- JDK 21
- Maven 3.8+
- PostgreSQL database

## Configuration

### Server

1. Configure database in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/reservation_system
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password

# SMS configuration
philsms.api.token=your_philsms_token
philsms.sender.id=your_sender_id

# Website URL for web portal
website.url=https://your-web-app.example.com

# Email configuration
spring.mail.host=smtp.example.com
spring.mail.username=your_email
spring.mail.password=your_password
```

### Staff App

Configure in `config/application.properties`:
```properties
# Database connection
spring.datasource.url=jdbc:postgresql://localhost:5432/reservation_system
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password

# Website URL for customer portal
website.url=https://your-web-app.example.com

# WebSocket server URL
server.url=ws://localhost:8080/ws
```

## Running

### Server

```bash
cd server
mvn spring-boot:run
```

The web registration portal will be available at `http://localhost:8080`

### Staff App

```bash
cd staff-app
mvn clean javafx:run
```

### Windows Packaging (Staff App)

```bash
cd staff-app
mvn clean verify -Pjpackage
```

Output: `target/jpackage/ReservationSystem/`

With WiX installed:
```bash
mvn clean verify -Pjpackage,jpackage-exe
```

Output: `target/jpackage/ReservationSystem-1.0.exe`

## Security Notes

- Do not commit real API tokens, database passwords, or production endpoints to source control
- Use environment variables or externalized configuration for secrets
- If WebSocket endpoint uses HTTPS/TLS, use `wss://` instead of `ws://`

## License

Proprietary - Romantic Baboy