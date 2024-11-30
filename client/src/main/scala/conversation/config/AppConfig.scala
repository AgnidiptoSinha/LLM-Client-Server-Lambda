package conversation.config

import com.typesafe.config.{Config, ConfigFactory}

case class AppConfig(
                      ec2Config: EC2Config,
                      ollamaConfig: OllamaConfig,
                      maxTurns: Int
                    )

case class EC2Config(
                      url: String,
                      endpoint: String
                    )

case class OllamaConfig(
                         host: String,
                         model: String,
                         requestTimeoutSeconds: Int,
                         temperature: Double,
                         num_predict: Int
                       )

object AppConfig {
  def load(): AppConfig = {
    val config = ConfigFactory.load()

    AppConfig(
      ec2Config = EC2Config(
        url = config.getString("ec2.url"),
        endpoint = config.getString("ec2.endpoint")
      ),
      ollamaConfig = OllamaConfig(
        host = config.getString("ollama.host"),
        model = config.getString("ollama.model"),
        requestTimeoutSeconds = config.getInt("ollama.request-timeout-seconds"),
        temperature = config.getDouble("ollama.temperature"),
        num_predict = config.getInt("ollama.num_predict")
      ),
      maxTurns = config.getInt("conversation.max-turns")
    )
  }
}