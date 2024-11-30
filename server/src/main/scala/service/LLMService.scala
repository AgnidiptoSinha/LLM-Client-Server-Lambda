package service

import akka.actor.typed.ActorSystem
import aws.bedrock.BedrockClient
import aws.lambda.GrpcLambdaClient
import com.typesafe.scalalogging.LazyLogging
import models.{GenerateRequest, GenerateResponse}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class LLMService(system: ActorSystem[Nothing])(implicit ec: ExecutionContext) extends LazyLogging {
  private val config = ConfigFactory.load()
  private val modelPath = config.getString("llm.model.path")
  private val maxTokens = config.getInt("llm.model.maxTokens")

  // main function to use Lambda with grpc
  private val lambdaLLMService = new LambdaLLMService(system, config)
  def generateResponse(query: String): Future[GenerateResponse] = {
    lambdaLLMService.generateResponse(query)
  }

  def shutdown(): Unit = {
    lambdaLLMService.shutdown()
  }
}

class LambdaLLMService(system: ActorSystem[Nothing], config: Config)(implicit ec: ExecutionContext) extends LazyLogging {
  private val grpcClient = new GrpcLambdaClient(config, system)

  def generateResponse(query: String): Future[GenerateResponse] = {
    logger.info(s"Generating response for query: $query")

    grpcClient.invokeLambda(query, config.getInt("llm.model.maxTokens"))
      .map(text => GenerateResponse(text))
      .recover { case e: Exception =>
        logger.error(s"Text generation failed: ${e.getMessage}")
        throw e
      }
  }

  def shutdown(): Unit = {
    grpcClient.shutdown()
  }
}