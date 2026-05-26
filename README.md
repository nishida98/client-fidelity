# Client Fidelity

Client Fidelity is a Spring Boot project for small businesses that want to track customer purchases and reward loyal customers after a defined number of purchases.

The main idea is simple: every time a customer buys something, the business records that purchase. When the customer reaches the required purchase count, they become eligible for a prize, discount, free product, or another loyalty reward.

## Project Goal

This application is intended to help small businesses manage a basic customer loyalty program without needing paper cards or manual tracking.

Possible use cases include:

- Coffee shops offering a free drink after 10 purchases
- Bakeries rewarding frequent customers
- Small stores tracking repeat purchases
- Service businesses giving discounts after a number of visits

## Planned Features

- Register customers
- Record customer purchases
- Track how many purchases each customer has made
- Configure the number of purchases required for a reward
- Mark customers as eligible for a prize
- Reset or continue the purchase count after a reward is redeemed
- Provide REST endpoints for managing customers, purchases, and rewards

## Tech Stack

- Java 26
- Spring Boot 4.0.6
- Spring Web MVC
- Maven

## Project Structure

```text
src/
  main/
    java/com/lhn/client_fidelity/
      ClientFidelityApplication.java
    resources/
      application.properties
  test/
    java/com/lhn/client_fidelity/
      ClientFidelityApplicationTests.java
```

## Requirements

- JDK 26
- Maven, or the included Maven Wrapper

## Running the Application

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On Linux or macOS:

```bash
./mvnw spring-boot:run
```

By default, the application starts on:

```text
http://localhost:8080
```

## Running Tests

On Windows:

```powershell
.\mvnw.cmd test
```

On Linux or macOS:

```bash
./mvnw test
```

## Current Status

The project currently contains the initial Spring Boot application setup. Domain models, REST controllers, persistence, and business rules for the loyalty program still need to be implemented.

## Future Domain Ideas

The application may include entities such as:

- `Customer`: stores customer information
- `Purchase`: records each customer purchase
- `RewardRule`: defines how many purchases are required for a prize
- `Reward`: tracks earned and redeemed prizes

## License

No license has been defined yet.
