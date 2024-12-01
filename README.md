# CS441 Homework 3 - LLM Conversational System

This repository is a homework submission for CS441 (Engineering Distributed Objects for Cloud Computing) at the University of Illinois at Chicago, Fall 2024.

## Project Demo
[Watch the demo video on YouTube](https://youtu.be/rgrdB9rGIms)

## Overview

This project implements a comprehensive Large Language Model (LLM) conversation system built with Scala, featuring a microservice architecture with automated conversational capabilities. The system integrates cloud-hosted LLMs through AWS Bedrock with local Ollama models to create an intelligent conversational experience.

The implementation fulfills the requirements of CS441's Homework 3, which focuses on creating an LLM-based generative system using Amazon Bedrock or a custom-trained LLM to respond to clients' requests using cloud-deployed lambda functions.

## System Architecture

The project consists of three main components:

1. **Client**: A conversational agent that orchestrates dialogue between cloud LLM and local Ollama
2. **Server**: A microservice for text generation using AWS Bedrock, hosted on EC2
3. **Lambda**: AWS Lambda function handling Bedrock interactions

```
root/
├── client/          # Conversational agent implementation
├── server/          # Text generation microservice
└── lambda/          # AWS Lambda Bedrock handler
```

## Features

- **Automated Conversation Generation**: Combines cloud LLM responses with local model processing
- **Microservice Architecture**: Scalable and maintainable service design
- **AWS Integration**: Leverages AWS Bedrock and Lambda for robust cloud processing
- **Docker Support**: Containerized deployment for all components
- **Comprehensive Logging**: Detailed logging and conversation tracking
- **Configuration Management**: Flexible configuration using TypeSafe Config
- **Error Handling**: Robust error handling and retry mechanisms

## Prerequisites

- Java 11 or higher
- Scala 2.13.12
- SBT 1.9.x
- Docker and Docker Compose
- AWS Account with configured credentials
- Ollama installed locally (for development)

## Quick Start

1. Clone the repository:
```bash
git clone <repository-url>
cd <repository-name>
```

2. Configure AWS credentials:
```bash
export AWS_ACCESS_KEY_ID="your-access-key"
export AWS_SECRET_ACCESS_KEY="your-secret-key"
```

3. Start the system using Docker Compose:
```bash
docker-compose up
```

## Component Details

### Client
- Manages conversation flow between cloud LLM and local Ollama
- Handles automated question generation
- Implements conversation logging and management
- [More details](client/README.md)

### Server
- Provides RESTful API for text generation
- Integrates with AWS Lambda using gRPC
- Implements request routing and error handling
- [More details](server/README.md)

### Lambda
- Processes text generation requests using Amazon Bedrock
- Handles AWS service integration
- Manages error handling and response formatting
- [More details](lambda/README.md)

## Docker Deployment

The system uses Docker Compose for orchestration:

```yaml
services:
  llm-server:
    build: ./server
    ports:
      - "8000:8000"
    networks:
      - llm-network

  llm-client:
    build: ./client
    volumes:
      - ./logs:/app/logs:rw
      - ./conversations:/app/conversations:rw
    environment:
      - EC2_URL=http://llm-server:8000
      - OLLAMA_HOST=http://host.docker.internal:11434
    networks:
      - llm-network
    depends_on:
      - llm-server
```

## Configuration

Each component has its own configuration files:

1. Client: `client/src/main/resources/application.conf`
2. Server: `server/src/main/resources/application.conf`
3. Lambda: Environment variables in AWS Console

Environment variables needed:
- `EC2_URL`: URL of the server instance
- `OLLAMA_HOST`: URL of local Ollama instance
- AWS credentials for Lambda deployment

## Building

Build individual components:

```bash
# Build client
cd client && sbt clean compile

# Build server
cd server && sbt clean compile

# Build lambda
cd lambda && sbt assembly
```

## Testing

Run tests for each component:

```bash
# Test client
cd client && sbt test

# Test server
cd server && sbt test

# Test lambda
cd lambda && sbt test
```

## Project Structure

```
root/
├── client/
│   ├── src/
│   │   └── main/
│   │       ├── scala/
│   │       └── resources/
│   ├── Dockerfile
│   └── README.md
├── server/
│   ├── src/
│   │   └── main/
│   │       ├── scala/
│   │       └── resources/
│   ├── Dockerfile
│   └── README.md
├── lambda/
│   ├── src/
│   │   └── main/
│   │       └── scala/
│   └── README.md
├── docker-compose.yml
└── README.md
```

## Monitoring and Logging

- Client: Logs conversations and interaction metrics
- Server: HTTP request/response logging
- Lambda: CloudWatch logs and metrics

## Course Information

- **Course**: CS441 - Engineering Distributed Objects for Cloud Computing
- **University**: University of Illinois at Chicago
- **Semester**: Fall 2024
- **Student**: Agnidipto Sinha
- **Instructor**: Dr. Mark Grechanik
- **TA**: Vasu Garg

## Homework Requirements Met

This implementation satisfies the key requirements of Homework 3:

1. Created a RESTful/gRPC service in Scala
2. Implemented AWS Lambda functions for LLM interaction
3. Deployed the system on AWS EC2
4. Created an automated conversational client using Ollama
5. Implemented comprehensive logging and configuration management
6. Containerized the application using Docker
7. Created thorough documentation and testing
