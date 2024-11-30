package conversation.models

import spray.json._
import spray.json.DefaultJsonProtocol._

case class ConversationTurn(
                             prompt: String,
                             response: String,
                             timestamp: Long = System.currentTimeMillis()
                           )

case class Conversation(
                         id: String,
                         turns: List[ConversationTurn]
                       )

object ConversationTurn {
  implicit val format: RootJsonFormat[ConversationTurn] = jsonFormat3(ConversationTurn.apply)
}

object Conversation {
  import ConversationTurn._  // Import the ConversationTurn format
  implicit val format: RootJsonFormat[Conversation] = jsonFormat2(Conversation.apply)
}

// API models
case class EC2Request(query: String)
case class EC2Response(text: String)

object ApiFormats {
  implicit val ec2RequestFormat: RootJsonFormat[EC2Request] = jsonFormat1(EC2Request.apply)
  implicit val ec2ResponseFormat: RootJsonFormat[EC2Response] = jsonFormat1(EC2Response.apply)
}