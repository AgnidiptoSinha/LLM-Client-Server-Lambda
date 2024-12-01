package conversation.testutils

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import conversation.config.{AppConfig, EC2Config, OllamaConfig}
import scala.concurrent.Future

object TestConfig {
  // Mock configurations for testing
  val mockEC2Config = EC2Config(
    url = "http://localhost:8080",
    endpoint = "/test"
  )

  val mockOllamaConfig = OllamaConfig(
    host = "http://localhost:11434",
    model = "test-model",
    requestTimeoutSeconds = 30,
    temperature = 0.4,
    num_predict = 50
  )

  val mockAppConfig = AppConfig(
    ec2Config = mockEC2Config,
    ollamaConfig = mockOllamaConfig,
    maxTurns = 5
  )

  // Mock HTTP responses
  val successResponse = HttpResponse(
    status = StatusCodes.OK,
    entity = """{"text": "This is a test response"}"""
  )

  val errorResponse = HttpResponse(
    status = StatusCodes.InternalServerError,
    entity = "Internal Server Error"
  )

  // Helper methods for testing
  def mockHttpRequest(shouldSucceed: Boolean = true): Future[HttpResponse] = {
    Future.successful(if (shouldSucceed) successResponse else errorResponse)
  }

  def configWithMaxTurns(turns: Int): AppConfig = mockAppConfig.copy(maxTurns = turns)

  def configWithBadEC2(): AppConfig = mockAppConfig.copy(
    ec2Config = mockEC2Config.copy(url = "http://invalid-url")
  )

  def configWithBadOllama(): AppConfig = mockAppConfig.copy(
    ollamaConfig = mockOllamaConfig.copy(host = "http://invalid-host")
  )
}