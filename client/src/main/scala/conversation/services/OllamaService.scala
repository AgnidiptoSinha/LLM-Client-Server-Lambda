package conversation.services

import conversation.config.OllamaConfig
import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.models.OllamaResult
import io.github.ollama4j.utils.Options
//import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._

class OllamaService(config: OllamaConfig) {
//  private val logger = LoggerFactory.getLogger(classOf[OllamaService])
  private val ollamaAPI = new OllamaAPI(config.host)
  ollamaAPI.setRequestTimeoutSeconds(config.requestTimeoutSeconds)

  def generateNextPrompt(previousResponse: String): String = {
    try {
      print(s"Generating next prompt based on response: $previousResponse")

//      // Create a more contextual prompt template
//      val prompt = s"""As an AI assistant engaging in conversation, analyze this response:
//                      |'$previousResponse'
//                      |
//                      |Generate a natural, contextually relevant follow-up question that:
//                      |1. Directly relates to specific details mentioned in the response
//                      |2. Encourages elaboration on an interesting point
//                      |3. Maintains conversational flow
//                      |
//                      |Keep the question concise and natural, as if in a real conversation.""".stripMargin

      val prompt = previousResponse.stripMargin

      print(s"Sending prompt to Ollama: $prompt")

      // Create options map with temperature
      val optionsMap: java.util.Map[String, AnyRef] = Map[String, AnyRef](
        "temperature" -> java.lang.Double.valueOf(config.temperature),
        "num_predict" -> java.lang.Integer.valueOf(config.num_predict)
      ).asJava

      val options = new Options(optionsMap)
      val result: OllamaResult = ollamaAPI.generate(config.model, prompt, false, options)
      val response = result.getResponse.trim

      print(s"Generated follow-up question: $response")

      // Validate the response
      if (response.isEmpty || response == "Can you elaborate on that?") {
        print("Received default or empty response, generating fallback question")
        generateFallbackQuestion(previousResponse)
      } else {
        response
      }
    } catch {
      case e: Exception =>
        print(s"Error generating next prompt: ${e.getMessage}", e)
        generateFallbackQuestion(previousResponse)
    }
  }

  private def generateFallbackQuestion(previousResponse: String): String = {
    // Extract key terms from the previous response
    val keywords = previousResponse.split("\\W+")
      .filter(_.length > 4)
      .take(2)

    if (keywords.isEmpty) {
      "Could you tell me more about what you mean?"
    } else {
      s"Could you tell me more about ${keywords.mkString(" and ")}?"
    }
  }
}