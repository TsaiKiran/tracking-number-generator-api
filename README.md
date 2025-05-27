# Tracking Number Generator API

A scalable REST API service that generates unique tracking numbers for shipments.

## Tracking Number Format

The tracking number follows this format: `CCDDDDDDNNNNNN`

Where:
- `CC`: Two letters representing the first letter of origin and destination country codes
  - Example: US to GB becomes "UG"
- `DDDDDD`: Date in YYMMDD format
  - Example: March 15, 2024 becomes "240315"
- `NNNNNN`: 8-digit sequence number (padded with zeros)
  - Example: "00012345"

Example tracking number: `UG24031500012345`

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Redis server
- Maven

### Installation

1. Clone the repository:

git clone https://github.com/TsaiKiran/tracking-number-generator-api.git
s
    cd tracking-number-generator-api


2. Configure Redis:
   - Ensure Redis is running locally on default port (6379) or configure redis cloud URL in REDIS_URL

3. Build the application:
```
./mvnw clean install
```

### Running the Application

1. Start the application:
```bash
./mvnw spring-boot:run
```

2. The application will start on `http://localhost:8080`

### Use Postman or Browser to Trigger the request

Execute the api using:
```
https://tracking-number-generator-api-ayt5.onrender.com/api/v1/next-tracking-number?originCountryId=MY&destinationCountryId=ID&weight=1.234&createdAt=2018-11-20T11:29:32Z&customerId=de619854-b59b-425e-9db4-943979e1bd49&customerName=RedBox%20Logistics&customerSlug=redbox-logistics
```
- Use custom values as per requirement in the request
## API Documentation

### Generate Tracking Number

```
POST /api/v1/next-tracking-number
```

Request Params:
```

originCountryId:MY
destinationCountryId:ID
weight:1.234
createdAt:2018-11-20T11:29:32Z
customerId:de619854-b59b-425e-9db4-943979e1bd49
customerName:RedBox Logistics
customerSlug:redbox-logistics

```

Response:
```json
{
  "trackingNumber": "MI18112000000011",
  "createdAt": "2025-05-26T19:33:57.193113157Z",
  "customerSlug": "redbox-logistics"
}
```

### Public API URL

Base URL: `https://tracking-number-generator-api-ayt5.onrender.com/`


⚠️ Note on Cold Start Delay

Important:
This application is deployed on Render’s free tier, which automatically puts services to sleep after 15 minutes of inactivity.
As a result, the first request after a period of inactivity may take 30-45 seconds to respond due to cold start latency.
Subsequent requests will respond normally with low latency.

## Features

- Generates unique tracking numbers with date-based prefixes
- Redis-based sequence number generation
- Input validation for all fields
- Error handling with detailed responses
- Daily sequence number reset
- Scalable design for high-volume usage

## Error Handling

The API provides detailed error responses for:
- Invalid input validation
- Missing required fields
- System errors
- Redis connection issues

## Health Status URL

- https://tracking-number-generator-api-ayt5.onrender.com/actuator/health

## Sample URL to hit public API hosted on Render:
https://tracking-number-generator-api-ayt5.onrender.com/api/v1/next-tracking-number?originCountryId=MY&destinationCountryId=ID&weight=1.234&createdAt=2018-11-20T11:29:32Z&customerId=de619854-b59b-425e-9db4-943979e1bd49&customerName=RedBox%20Logistics&customerSlug=redbox-logistics