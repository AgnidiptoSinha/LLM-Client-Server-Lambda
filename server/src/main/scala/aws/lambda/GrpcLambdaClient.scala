package aws.lambda

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import aws.lambda.proto.llm.{GenerateTextRequest, GenerateTextResponse, Status}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import spray.json._
import DefaultJsonProtocol._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class GrpcLambdaClient(config: Config, system: ActorSystem[Nothing])(implicit ec: ExecutionContext) extends LazyLogging {
  private implicit val classicSystem = system.toClassic
  private val apiGatewayUrl = config.getString("llm.aws.lambda.api-gateway-url")

  // JSON formats for protobuf messages
  private case class ProtoRequest(prompt: String, maxTokens: Int)
  private case class ProtoResponse(text: String, status: Option[StatusInfo])
  private case class StatusInfo(code: Int, message: String)

  private implicit val statusInfoFormat: RootJsonFormat[StatusInfo] = jsonFormat2(StatusInfo)
  private implicit val protoRequestFormat: RootJsonFormat[ProtoRequest] = jsonFormat2(ProtoRequest)
  private implicit val protoResponseFormat: RootJsonFormat[ProtoResponse] = jsonFormat2(ProtoResponse)

  def invokeLambda(prompt: String, maxTokens: Int): Future[String] = {
    logger.info(s"Invoking Lambda via API Gateway with prompt: $prompt")
    val startTime = System.currentTimeMillis()

    // Create the protobuf request
    val protoRequest = GenerateTextRequest(
      prompt = prompt,
      maxTokens = maxTokens
    )

    // Convert to JSON-friendly case class
    val jsonRequest = ProtoRequest(
      prompt = protoRequest.prompt,
      maxTokens = protoRequest.maxTokens
    )

    // Create HTTP request with JSON
    val httpRequest = HttpRequest(
      method = HttpMethods.POST,
      uri = apiGatewayUrl,
      entity = HttpEntity(
        ContentTypes.`application/json`,
        jsonRequest.toJson.compactPrint
      )
    )

    Http()
      .singleRequest(httpRequest)
      .flatMap { response =>
        val processingTime = System.currentTimeMillis() - startTime
        logger.debug(s"API Gateway response received in ${processingTime}ms")

        response.status match {
          case StatusCodes.OK =>
            Unmarshal(response.entity).to[String].map { responseBody =>
              try {
                // Parse JSON response
                val jsonResponse = responseBody.parseJson.convertTo[ProtoResponse]

                // Convert back to protobuf response
                val protoResponse = GenerateTextResponse(
                  text = jsonResponse.text,
                  status = jsonResponse.status.map(s => Status(code = s.code, message = s.message))
                )

                protoResponse.status match {
                  case Some(status) if status.code == 200 =>
                    logger.info(s"Successfully generated text, length: ${protoResponse.text.length}")
                    protoResponse.text

                  case Some(status) =>
                    val errorMsg = s"Lambda invocation failed with status ${status.code}: ${status.message}"
                    logger.error(errorMsg)
                    throw new RuntimeException(errorMsg)

                  case None =>
                    val errorMsg = "Invalid response: Missing status field"
                    logger.error(errorMsg)
                    throw new RuntimeException(errorMsg)
                }
              } catch {
                case e: Exception =>
                  logger.error(s"Failed to parse response: ${e.getMessage}")
                  logger.debug(s"Raw response: $responseBody")
                  throw new RuntimeException(s"Failed to parse response: ${e.getMessage}")
              }
            }

          case _ =>
            Unmarshal(response.entity).to[String].flatMap { errorBody =>
              logger.error(s"API Gateway request failed with status ${response.status}, body: $errorBody")
              Future.failed(new RuntimeException(s"API Gateway request failed: ${response.status} - $errorBody"))
            }
        }
      }
      .recoverWith { case e: Exception =>
        logger.error(s"Error in API Gateway request: ${e.getMessage}", e)
        Future.failed(e)
      }
  }

  def shutdown(): Unit = {
    logger.info("Shutting down gRPC Lambda client")
  }
}