package conversation.services

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.scalalogging.Logger
import conversation.config.EC2Config
import conversation.models.ApiFormats._
import conversation.models.{EC2Request, EC2Response}
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class EC2Service(config: EC2Config)(implicit system: ActorSystem[_], ec: ExecutionContext) {
  private val logger = Logger(getClass.getName)

  private def queryWithRetry(prompt: String, retries: Int = 3, delay: FiniteDuration = 5.seconds): Future[String] = {
    logger.debug(s"Processing query with prompt: $prompt (Retries left: $retries)")

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = s"${config.url}${config.endpoint}",
      headers = List(headers.`Content-Type`(ContentTypes.`application/json`)),
      entity = HttpEntity(
        ContentTypes.`application/json`,
        EC2Request(prompt).toJson.toString()
      )
    )

    logger.info(s"Sending request to EC2: ${request.uri}")

    Http().singleRequest(request).flatMap { response =>
      response.status match {
        case StatusCodes.OK =>
          logger.debug("Received OK response from EC2")
          Unmarshal(response.entity).to[String].map { jsonStr =>
            try {
              val ec2Response = jsonStr.parseJson.convertTo[EC2Response]
              val responseText = ec2Response.text.trim
              logger.info(s"Successfully processed EC2 response: $responseText")
              responseText
            } catch {
              case e: Exception =>
                logger.error(s"Failed to parse EC2 response: $jsonStr", e)
                throw new RuntimeException("Failed to parse EC2 response", e)
            }
          }
        case status =>
          logger.error(s"Unexpected status code from EC2: $status")
          Future.failed(new RuntimeException(s"Unexpected status code: $status"))
      }
    }.recoverWith {
      case e: Exception if retries > 0 =>
        logger.warn(s"Request failed: ${e.getMessage}. Retrying in ${delay.toSeconds} seconds...")
        akka.pattern.after(delay) {
          queryWithRetry(prompt, retries - 1, delay * 2)
        }
      case e: Exception =>
        logger.error(s"All retry attempts failed: ${e.getMessage}", e)
        Future.failed(e)
    }
  }

  def query(prompt: String): Future[String] = queryWithRetry(prompt)
}