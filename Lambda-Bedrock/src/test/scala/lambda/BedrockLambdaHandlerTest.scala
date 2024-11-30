package lambda

import com.amazonaws.services.lambda.runtime.{Context, LambdaLogger}
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalamock.scalatest.MockFactory
import io.circe.syntax._
import BedrockLambdaHandler._

class BedrockLambdaHandlerTest extends AnyFlatSpec with Matchers with MockFactory {

  "BedrockLambdaHandler" should "handle valid requests" in {
    val handler = new BedrockLambdaHandler()
    val mockContext = mock[Context]
    val mockLogger = mock[LambdaLogger]

    (mockContext.getLogger _).expects().returning(mockLogger)
    (mockLogger.log(_: String)).expects(*).anyNumberOfTimes()

    val request = LambdaRequest("Tell me about cats", 100)
    val event = new APIGatewayProxyRequestEvent()
      .withBody(request.asJson.noSpaces)

    val result = handler.handleRequest(event, mockContext)

    result.getStatusCode shouldBe 200
    result.getHeaders.get("Content-Type") shouldBe "application/json"
  }

  it should "handle invalid requests gracefully" in {
    val handler = new BedrockLambdaHandler()
    val mockContext = mock[Context]
    val mockLogger = mock[LambdaLogger]

    (mockContext.getLogger _).expects().returning(mockLogger)
    (mockLogger.log(_: String)).expects(*).anyNumberOfTimes()

    val event = new APIGatewayProxyRequestEvent()
      .withBody("invalid json")

    val result = handler.handleRequest(event, mockContext)

    result.getStatusCode shouldBe 500
    result.getBody should include("Error")
  }
}