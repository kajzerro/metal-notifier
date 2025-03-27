# 🔔 Metal Notifier

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.0-brightgreen)
![Lit](https://img.shields.io/badge/Lit-2.7.4-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

A modern notification system for precious metal price changes. Configure personalized templates, set precise price conditions, and receive targeted email notifications when market prices match your defined criteria.



## 🛠️ Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.1.0**: Core application framework
- **Spring Data JPA**: Database connectivity and ORM
- **Jakarta Validation**: Input validation
- **Lombok**: Boilerplate code reduction
- **JUnit 5 & Mockito**: Testing framework
- **H2 Database**: Data storage

### Frontend
- **Lit**: Web components framework
- **JavaScript**: Frontend logic
- **CSS3**: Styling
- **HTTP Server**: Static file serving

## 🏗️ Architecture
![Demo Gif](docs/demo_create_template_rule.gif)

The application follows a classic three-tier architecture:

```
┌────────────┐     ┌────────────┐     ┌────────────┐
│  Frontend  │◄────┤   Backend  │◄────┤  Database  │
│    (Lit)   │     │(Spring Boot│     │    (H2)    │
└────────────┘     └────────────┘     └────────────┘
```

- **Frontend**: User interface built with Lit web components
- **Backend**: RESTful API powered by Spring Boot
- **Database**: H2 database for persistent storage

## 📦 Installation & Setup

### Prerequisites
- Java 17 or higher
- Maven 3.6
- Node.js & npm (for frontend development)

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/metal-notifier.git
   cd metal-notifier
   ```

2. **Build and run the backend**
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```
   The backend will start on http://localhost:8080

### Frontend Setup

1. **Install dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Run the frontend**
   ```bash
   npm start
   ```
   The frontend will be available at http://localhost:8000



## 🚀 Usage

### Creating a Template

1. Click "Add New" in the template list
2. Fill in the template title and content
3. Add recipients (email addresses)
4. Configure rules:
   - Select metal type (gold, silver, platinum)
   - Set price conditions (greater than, less than, equal to, etc.)
      5. Save the template

### Testing Price Notifications

Send a POST request to `/api/new-price` with the following JSON payload:

```json
{
  "itemType": "gold",
  "price": "1850.50"
   }
   ```

The system will check all templates and send notifications if the criteria match.

## 📚 API Documentation

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/templates` | Retrieve all templates |
| GET | `/api/templates/{id}` | Get a single template by ID |
| POST | `/api/templates` | Create a new template |
| PUT | `/api/templates/{id}` | Update an existing template |
| DELETE | `/api/templates/{id}` | Delete a template |
| POST | `/api/new-price` | Process a new price signal |

### Price Signal Format

```json
{
  "itemType": "gold|silver|platinum",
  "price": "1234.56"
   }
   ```

### Template Format

```json
{
  "title": "Template Title",
  "content": "Notification content text",
  "recipients": [
    {"email": "user1@example.com"},
    {"email": "user2@example.com"}
  ],
  "rules": [
    {"operator": "ITEM_IS", "operand": "gold"},
    {"operator": "PRICE_IS_GREATER_THAN", "operand": "1800.00"}
  ]
   }
   ```


## 🧪 Testing


### Test Structure

Tests are organized by type:

- **Unit Tests**: Naming convention in `*Test.java`
- **Integration Tests**: Located in `*IT.java`


---

Made with ❤️ for ING Interview
