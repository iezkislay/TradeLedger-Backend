# TradeLedger Backend

TradeLedger is a comprehensive billing, inventory, customer ledger, sales return, refund, and accounting system designed for hardware, sanitary, plumbing, electrical, and retail businesses.

This repository contains the Spring Boot backend responsible for business logic, authentication, inventory management, customer accounting, billing, returns, refunds, reporting, and audit tracking.

---

## Features

### Billing Management

* Generate bills
* Cash, UPI and Credit sales
* Bill adjustments
* Discounts and overrides
* Bill lifecycle management

### Inventory Management

* Item master management
* Category management
* Brand management
* Stock tracking
* Low stock monitoring
* Stock transaction audit trail

### Customer Management

* Customer master
* Customer ledger
* Outstanding balance tracking
* Customer statements
* Payment history

### Sales Returns

* Return Notes
* Partial returns
* Full returns
* Delivered returns
* Pending returns
* Return reconciliation
* Finalized return workflow

### Refund Management

* Refund processing
* Return-linked refunds
* Refund audit trail
* Settlement validation

### Financial Controls

* Customer ledger accounting
* Due calculations
* Payment tracking
* Adjustments
* Credit management

### Audit System

* User activity logs
* Business operation tracking
* Change history

### Security

* Session-based authentication
* Role-based authorization
* Owner permissions
* Billing user permissions

---

## Technology Stack

### Backend

* Java 21
* Spring Boot
* Spring Data JPA
* Hibernate
* Maven

### Database

* PostgreSQL

### Security

* Session Authentication
* Role-Based Access Control

### Infrastructure

* Docker
* Kubernetes
* Nginx

---

## Project Structure

```text
src/main/java/com/store/app

├── controller/
├── service/
├── repository/
├── entity/
├── dto/
├── enums/
├── config/
├── security/
├── util/
└── exception/
```

---

## Core Modules

### Billing

Handles:

* Bill creation
* Bill updates
* Bill printing
* Customer invoices
* Estimates

---

### Inventory

Handles:

* Item management
* Stock movement
* Stock transactions
* Inventory reconciliation

---

### Customer Ledger

Handles:

* Outstanding balance
* Payment posting
* Ledger entries
* Customer statements

---

### Return Management

Handles:

* Return note creation
* Return validation
* Return finalization
* Stock reversal
* Refund eligibility

---

### Refund Management

Handles:

* Refund creation
* Refund validation
* Refund history
* Settlement controls

---

### Audit Management

Tracks:

* Bill creation
* Customer changes
* Inventory changes
* Refund processing
* Return processing

---

## Database

### Primary Tables

```text
users
customers
items
stock
stock_transactions

bills
bill_items

customer_ledger

return_notes
return_items

refunds

audit_logs
```

---

## Setup

### Clone Repository

```bash
git clone <repository-url>
cd app-backend
```

---

### Configure PostgreSQL

Create database:

```sql
CREATE DATABASE tradeledger;
```

---

### Configure application.properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/tradeledger
spring.datasource.username=postgres
spring.datasource.password=password

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

---

## Run Application

### Using Maven

```bash
mvn clean install
mvn spring-boot:run
```

Application runs on:

```text
http://localhost:8080
```

---

## Build

```bash
mvn clean package
```

Generated artifact:

```text
target/app.jar
```

Run:

```bash
java -jar target/app.jar
```

---

## API Structure

```text
/api/auth

/api/customers
/api/items
/api/categories
/api/brands

/api/bills
/api/payments

/api/returns
/api/refunds

/api/reports
/api/dashboard
```

---

## Roles

### OWNER

Full access to:

* Users
* Inventory
* Customers
* Billing
* Returns
* Refunds
* Reports
* Settings

### BILLING

Access to:

* Billing
* Customers
* Payments
* Return Notes

Restricted from:

* User management
* System settings

---

## Accounting Rules

### Ledger Logic

```text
DEBIT          -> Customer owes store
CREDIT         -> Customer payment
RETURN_CREDIT  -> Sales return adjustment
ADJUSTMENT     -> Waiver / correction
```

### Balance Formula

```text
DEBIT
- CREDIT
- RETURN_CREDIT
- ADJUSTMENT
```

Result:

```text
Positive  -> Customer owes store
Negative  -> Store owes customer
```

---

## Return Workflow

```text
Create Return Note
        ↓
Validate Quantities
        ↓
Stock Reconciliation
        ↓
Finalize Return
        ↓
Ledger Settlement
        ↓
Refund Processing
```

---

## Deployment

TradeLedger is designed for:

* Local deployment
* VPS deployment
* Docker deployment
* Kubernetes deployment
* Cloud deployment

Supported environments:

* AWS
* Azure
* GCP
* On-Premise

---

## Future Enhancements

* GST Support
* Multi-store Support
* Barcode Scanning
* WhatsApp Integration
* Purchase Management
* Supplier Ledger
* Expense Tracking
* Mobile Application

---

## License

Private Proprietary Software

© TradeLedger. All Rights Reserved.
