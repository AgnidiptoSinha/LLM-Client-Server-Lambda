# Server: LLM Text Generation Service

A Scala-based microservice that provides text generation capabilities using AWS Bedrock through AWS Lambda. Built with Akka HTTP, this service offers a RESTful API for text generation requests.

## Project Structure

```
├── src/main/scala
│   ├── aws
│   │   ├── bedrock/
│   │   │   └── BedrockClient.scala    # AWS Bedrock client implementation
│   │   └── lambda/
│   │       └── GrpcLambdaClient.scala # gRPC client for Lambda
│   ├── service/
│   │   ├── LLMService.scala          # Core LLM service logic
│   │   └── RestServer.scala          # REST API server
│   ├── models/
│   │   └── Models.scala              # Data models and JSON formats
│   └── Main.scala                    # Application entry point
├── src/main/resources
│   ├── application.conf              # Application configuration
│   └── logback.xml                   # Logging configuration
└── src/main/protobuf
    └── llm.proto                     # Protocol buffer definitions
```

## Prerequisites

- JDK 8+
- Scala 2.12.18
- sbt 1.x
- AWS Account with configured credentials

## Configuration

### application.conf
```hocon
llm {
  model {
    maxTokens = 100
  }
  server {
    host = "0.0.0.0"
    port = 8000
  }
  aws {
    region = "us-east-1"
    lambda {
      function-name = "Bedrock-LLM"
    }
  }
}

akka {
  http {
    server {
      interface = "0.0.0.0"
      request-timeout = 120s
    }
  }
}
```

### AWS Credentials
Configure AWS credentials in `~/.aws/credentials` or through environment variables:

1. ~/.aws/credentials

```
[default]
aws_access_key_id="your-access-key"
aws_secret_access_key="your-secret-key"
```
2. Environment variables
```
aws.credentials {
  access-key = "your-access-key"
  secret-key = "your-secret-key"
}
```

## Building and Running

1. Clone the repository:
```bash
git clone <repository-url>
```

2. Build the project:
```bash
sbt clean compile
```

3. Run tests:
```bash
sbt test
```

4. Start the server:
```bash
sbt run
```

## REST API Endpoints

### 1. Health Check
```http
GET /
```
Response:
```json
{
    "message": "Server is running! Post to '/generate'"
}
```

### 2. Generate Text
```http
POST /generate
Content-Type: application/json

{
    "query": "Your prompt text here"
}
```
Response:
```json
{
    "text": "Generated response text"
}
```

Example curl commands:
```bash
# Health check
curl http://localhost:8000/

# Generate text
curl -X POST http://localhost:8000/generate \
  -H "Content-Type: application/json" \
  -d '{"query": "How do cats express love?"}'
```

## Architecture Flow

1. Client sends HTTP request to Akka server
2. Akka server processes request through RestServer
3. LLMService handles business logic
4. GrpcLambdaClient invokes AWS Lambda function using GRPC
5. Lambda processes request and calls Bedrock
6. Response flows back through the chain to client

## Error Handling

The service implements comprehensive error handling with appropriate HTTP status codes:

- 200: Successful response
- 400: Bad Request (invalid input)
- 500: Internal Server Error (server/AWS service errors)
- 504: Gateway Timeout (Lambda timeout)

Example error response:
```json
{
    "error": "Request failed: Invalid input format"
}
```

## Logging

Using Logback with SLF4J. Configure in `logback.xml`:
```xml
<root level="INFO">
    <appender-ref ref="STDOUT" />
</root>
```

## Local Development

For local testing, ensure:
1. AWS credentials are properly configured
2. Lambda function is deployed
3. Required IAM roles and permissions are set
4. Local environment variables are set if needed

## Testing

Run the test suite:
```bash
sbt test
```