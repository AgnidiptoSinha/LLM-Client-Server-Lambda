package lambda

import aws.lambda.grpc.llm.{GenerateTextRequest, GenerateTextResponse, Status}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.typesafe.scalalogging.LazyLogging
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import spray.json._
import DefaultJsonProtocol._

import scala.util.{Failure, Success, Try}
import scala.collection.JavaConverters._
import java.util.Base64

class BedrockLambdaHandler extends RequestHandler[java.util.Map[String, Object], java.util.Map[String, Object]] with LazyLogging {
  // Case classes for JSON handling of protobuf messages
  private case class ProtoRequest(prompt: String, maxTokens: Int)
  private case class ProtoResponse(text: String, status: Option[StatusInfo])
  private case class StatusInfo(code: Int, message: String)

  private implicit val statusInfoFormat: RootJsonFormat[StatusInfo] = jsonFormat2(StatusInfo)
  private implicit val protoRequestFormat: RootJsonFormat[ProtoRequest] = jsonFormat2(ProtoRequest)
  private implicit val protoResponseFormat: RootJsonFormat[ProtoResponse] = jsonFormat2(ProtoResponse)

  // Case classes for Bedrock API
  private case class BedrockConfig(maxTokenCount: Int, temperature: Double, topP: Double)
  private case class BedrockRequest(inputText: String, textGenerationConfig: BedrockConfig)
  private case class BedrockResponse(results: List[BedrockResult])
  private case class BedrockResult(outputText: String)

  private implicit val bedrockConfigFormat: RootJsonFormat[BedrockConfig] = jsonFormat3(BedrockConfig)
  private implicit val bedrockRequestFormat: RootJsonFormat[BedrockRequest] = jsonFormat2(BedrockRequest)
  private implicit val bedrockResultFormat: RootJsonFormat[BedrockResult] = jsonFormat1(BedrockResult)
  private implicit val bedrockResponseFormat: RootJsonFormat[BedrockResponse] = jsonFormat1(BedrockResponse)

  private lazy val httpClient = ApacheHttpClient.builder()
    .maxConnections(50)
    .build()

  private lazy val bedrockClient = BedrockRuntimeClient.builder()
    .region(Region.US_EAST_1)
    .credentialsProvider(DefaultCredentialsProvider.create())
    .httpClient(httpClient)
    .build()

  override def handleRequest(input: java.util.Map[String, Object], context: Context): java.util.Map[String, Object] = {
    val logger = context.getLogger
    logger.log("Received request from API Gateway")

    try {
      // Get the request body
      val requestBody = Option(input.get("body")) match {
        case Some(b) =>
          val isBase64 = Option(input.get("isBase64Encoded")).exists(_.toString.toBoolean)
          if (isBase64) {
            new String(Base64.getDecoder.decode(b.toString), "UTF-8")
          } else {
            b.toString
          }
        case None => throw new RuntimeException("No body in request")
      }

      logger.log(s"Decoded request body: $requestBody")

      // Parse into JSON-friendly case class
      val jsonRequest = requestBody.parseJson.convertTo[ProtoRequest]

      // Convert to protobuf request (maintaining proto implementation)
      val protoRequest = GenerateTextRequest(
        prompt = jsonRequest.prompt,
        maxTokens = jsonRequest.maxTokens
      )

      logger.log(s"Processing request - prompt: ${protoRequest.prompt}, maxTokens: ${protoRequest.maxTokens}")

      // Create Bedrock request
      val bedrockRequest = BedrockRequest(
        inputText = protoRequest.prompt,
        textGenerationConfig = BedrockConfig(
          maxTokenCount = protoRequest.maxTokens,
          temperature = 0.7,
          topP = 0.9
        )
      )

      val modelRequest = InvokeModelRequest.builder()
        .modelId("amazon.titan-text-lite-v1")
        .contentType("application/json")
        .accept("application/json")
        .body(SdkBytes.fromUtf8String(bedrockRequest.toJson.compactPrint))
        .build()

      Try(bedrockClient.invokeModel(modelRequest)) match {
        case Success(response) =>
          val responseJson = response.body().asUtf8String().parseJson
          val bedrockResponse: BedrockResponse = responseJson.convertTo[BedrockResponse]

          val generatedText = bedrockResponse.results.headOption match {
            case Some(result) => result.outputText
            case None => throw new RuntimeException("No output text in Bedrock response")
          }

          // Create protobuf response (maintaining proto implementation)
          val protoResponse = GenerateTextResponse(
            text = generatedText,
            status = Some(Status(code = 200, message = "Success"))
          )

          // Convert to JSON-friendly response
          val jsonResponse = ProtoResponse(
            text = protoResponse.text,
            status = Some(StatusInfo(code = 200, message = "Success"))
          )

          val responseString = jsonResponse.toJson.compactPrint
          logger.log(s"Response JSON: $responseString")

          Map[String, Object](
            "statusCode" -> Integer.valueOf(200),
            "headers" -> Map(
              "Content-Type" -> "application/json",
              "Access-Control-Allow-Origin" -> "*"
            ).asJava,
            "body" -> responseString
          ).asJava

        case Failure(e) =>
          logger.log(s"Bedrock invocation failed: ${e.getMessage}")
          createErrorResponse(500, e.getMessage)
      }
    } catch {
      case e: Exception =>
        logger.log(s"Error processing request: ${e.getMessage}")
        createErrorResponse(500, e.getMessage)
    }
  }

  private def createErrorResponse(statusCode: Int, message: String): java.util.Map[String, Object] = {
    val errorResponse = ProtoResponse(
      text = "",
      status = Some(StatusInfo(code = statusCode, message = message))
    )

    Map[String, Object](
      "statusCode" -> Integer.valueOf(statusCode),
      "headers" -> Map(
        "Content-Type" -> "application/json",
        "Access-Control-Allow-Origin" -> "*"
      ).asJava,
      "body" -> errorResponse.toJson.compactPrint
    ).asJava
  }
}