# Client: LLM Conversational Agent

A Scala-based conversational agent that enables interaction between cloud-hosted LLMs and local Ollama models, creating an automated conversation system.

## Overview

This project implements a conversational agent that:
1. Sends queries to a cloud-hosted LLM via EC2
2. Processes responses using a local Ollama model
3. Automatically generates follow-up questions to maintain conversation flow
4. Logs and manages conversation history

## Architecture

The system consists of several key components:

- **ConversationalAgent**: Orchestrates the conversation flow between EC2 and Ollama services
- **EC2Service**: Handles communication with cloud-hosted LLM via RESTful API
- **OllamaService**: Manages local Ollama model interactions for generating follow-up questions
- **Configuration Management**: Uses Typesafe Config for flexible configuration
- **Logging**: Implements comprehensive logging using Logback

## Prerequisites

- Java 11 or higher
- SBT 1.9.x
- Docker and Docker Compose (for containerized deployment)
- Ollama installed locally (for development)
- AWS account with EC2 instance configured

## Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd client
```

2. Configure environment variables:
```bash
export EC2_URL=<your-ec2-instance-url>
export OLLAMA_HOST=http://localhost:11434
```

3. Build the project:
```bash
sbt clean compile
```

## Configuration

The application uses TypeSafe Config for configuration management. Key configuration files:

- `application.conf`: Main configuration file
- `logback.xml`: Logging configuration

### Important Configuration Parameters:

```hocon
ollama {
    host = ${?OLLAMA_HOST}
    model = "llama3.2"
    request-timeout-seconds = 120
    temperature = 0.4
    num_predict = 50
}

ec2 {
    url = ${?EC2_URL}
    endpoint = "/generate"
}

conversation {
    max-turns = 5
}
```

## Running the Application

### Local Development

1. Start the application:
```bash
sbt run "Tell me about artificial intelligence"
```

### Docker Deployment

1. Build the Docker image:
```bash
docker build -t llm-conversational-agent .
```

2. Run using Docker Compose:
```bash
docker-compose up
```

## Project Structure

```
├── src/main/scala/conversation/
│   ├── config/
│   │   └── AppConfig.scala           # Configuration management
│   ├── models/
│   │   └── Conversation.scala        # Data models
│   ├── services/
│   │   ├── EC2Service.scala         # Cloud LLM interaction
│   │   └── OllamaService.scala      # Local model interaction
│   └── ConversationalAgent.scala     # Main conversation logic
├── Dockerfile                        # Container configuration
├── docker-compose.yml               # Container orchestration
└── build.sbt                        # Project dependencies
```

## Key Features

- Automatic conversation generation using cloud and local LLMs
- Configurable conversation parameters (turns, timeout, temperature)
- Robust error handling and retry mechanisms
- Comprehensive logging
- Docker support for easy deployment
- Type-safe configuration management

## Dependencies

Main dependencies include:
- Akka HTTP for REST communication
- Ollama4j for local model interaction
- Typesafe Config for configuration
- Logback for logging
- ScalaTest for testing

## Error Handling

The system implements several error handling mechanisms:
- Automatic retries for failed EC2 requests
- Fallback question generation when Ollama fails
- Comprehensive logging of errors and system state
- Graceful degradation of service

## Testing

Run the test suite:
```bash
sbt test
```


## Docker Support

The project includes multi-stage Docker builds for optimized container size and efficient dependency caching. The `docker-compose.yml` file provides:
- Volume mounting for logs and conversations
- Environment variable configuration
- Network setup for service communication

## Known Limitations

- Requires stable internet connection for EC2 communication
- Local Ollama model must be installed separately
- Limited to configured maximum conversation turns
- Response generation time may vary based on model complexity
