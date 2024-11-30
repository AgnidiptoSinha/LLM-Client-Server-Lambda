# AWS Lambda Bedrock Handler

A Scala-based AWS Lambda function that processes text generation requests using Amazon Bedrock. This Lambda function serves as an intermediary between the main Akka server and Amazon Bedrock service.

## Project Structure

```
├── src/main/scala
│   └── lambda
│       └── BedrockLambdaHandler.scala  # Main Lambda handler
└── build.sbt                           # Project build configuration
```

## Prerequisites

- AWS Account with Bedrock access
- AWS Lambda basic execution role with Bedrock permissions
- Java 11+ runtime environment
- sbt 1.x

## Building

1. Clone the repository:
```bash
git clone <repository-url>
```

2. Build the JAR:
```bash
sbt clean assembly
```

This creates a fat JAR in `target/scala-2.12/bedrock-lambda-assembly-0.1.0.jar`

## Lambda Function Setup

1. Create Lambda function:
```bash
aws lambda create-function \
  --function-name Bedrock-LLM \
  --runtime java11 \
  --handler lambda.BedrockLambdaHandler \
  --memory-size 512 \
  --timeout 300 \
  --role arn:aws:iam::<account-id>:role/lambda-bedrock-role \
  --zip-file fileb://target/scala-2.12/bedrock-lambda-assembly-0.1.0.jar
```

2. Update Lambda function:
```bash
aws lambda update-function-code \
  --function-name Bedrock-LLM \
  --zip-file fileb://target/scala-2.12/bedrock-lambda-assembly-0.1.0.jar
```

## Function Input/Output

### Input Format
```json
{
    "body": {
        "prompt": "Your text prompt",
        "maxTokens": 100
    }
}
```

### Output Format
```json
{
    "statusCode": 200,
    "headers": {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*"
    },
    "body": {
        "text": "Generated text response",
        "status": {
            "code": 200,
            "message": "Success"
        }
    }
}
```

## IAM Role Configuration

Required permissions for Lambda execution role:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "bedrock:InvokeModel"
            ],
            "Resource": "*"
        }
    ]
}
```

## Error Handling

The Lambda function handles various error scenarios:

1. Invalid input format
2. Bedrock service errors
3. Timeouts
4. Missing parameters

Error response format:
```json
{
    "statusCode": 500,
    "body": {
        "text": "",
        "status": {
            "code": 500,
            "message": "Error description"
        }
    }
}
```

## Performance Tuning

Lambda configuration options:
- Memory: 512MB (adjustable based on needs)
- Timeout: 5 minutes
- Concurrent executions: Configurable via AWS console

