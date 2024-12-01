package test.service

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import service.{RestServer, LLMService}
import models.{GenerateRequest, GenerateResponse, WelcomeResponse}
import spray.json._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import models.JsonProtocol._

class RestServerSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  val testKit = ActorTestKit()
  implicit val typedSystem: ActorSystem[Nothing] = testKit.system
//  implicit val ec: ExecutionContext = system.dispatcher

  "RestServer" should {
    val llmService = new LLMService(typedSystem)
    val server = new RestServer(llmService)
    val routes = server.route

    "respond to health check endpoint" in {
      Get("/") ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType.mediaType shouldBe MediaTypes.`application/json`

        val response = responseAs[String].parseJson.convertTo[WelcomeResponse]
        response.message should include("Server is running")
      }
    }
  }

  override def afterAll(): Unit = {
    testKit.shutdownTestKit()
  }
}