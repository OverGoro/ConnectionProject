package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import java.util.UUID

class AuthServiceRecoverySimulation extends Simulation { 

  val targetRps = System.getProperty("targetRps", "100").toInt
  val targetHost = System.getProperty("targetHost", "localhost")
  val targetPort = System.getProperty("targetPort", "8081")
  val durationSec = System.getProperty("durationSec", "80").toInt // 20 + 60 = 80 секунд
  val serviceName = System.getProperty("serviceName", "auth-service")

  val httpProtocol = http
    .baseUrl(s"http://${targetHost}:${targetPort}")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .userAgentHeader("Gatling Benchmark")
    .disableCaching

  // Генерация уникальных тестовых данных
  def randomEmail(): String = s"test${System.currentTimeMillis()}${scala.util.Random.nextInt(10000)}@example.com"
  def randomUsername(): String = s"user${System.currentTimeMillis()}${scala.util.Random.nextInt(10000)}"
  val testPassword = "TestPassword123!"
  val testBirthDate = "1990-01-01T00:00:00.000Z"

  val authFlowScenario = scenario("Complete Auth Flow")
    .exec(session => {
      // Генерация уникальных данных для каждого пользователя
      val email = randomEmail()
      val username = randomUsername()
      val uid = UUID.randomUUID().toString
      session
        .set("email", email)
        .set("username", username)
        .set("password", testPassword)
        .set("birthDate", testBirthDate)
        .set("uid", uid)
    })
    // 1. Регистрация нового пользователя
    .exec(
      http("register")
        .post("/api/v1/auth/register")
        .body(StringBody(
          """{
            |  "uid": "${uid}",
            |  "birthDate": "${birthDate}",
            |  "email": "${email}",
            |  "password": "${password}",
            |  "username": "${username}"
            |}""".stripMargin
        )).asJson
        .check(status.in(200, 201, 409)) // Добавлен 409 для случая, когда пользователь уже существует
        .check(jsonPath("$.message").optional.saveAs("registerMessage"))
    )
    // 2. Логин для получения токенов
    .exec(
      http("login")
        .post("/api/v1/auth/login")
        .body(StringBody(
          """{
            |  "email": "${email}",
            |  "password": "${password}"
            |}""".stripMargin
        )).asJson
        .check(status.in(200, 201))
        .check(
          jsonPath("$.accessToken").saveAs("accessToken"),
          jsonPath("$.refreshToken").saveAs("refreshToken"),
          jsonPath("$.clientUid").saveAs("clientUid")
        )
    )
    // 3. Валидация access token
    .exec(
      http("validate_access")
        .post("/api/v1/auth/validate/access")
        .queryParam("accessToken", "${accessToken}")
        .check(status.in(200, 201))
    )
    // 4. Валидация refresh token
    .exec(
      http("validate_refresh")
        .post("/api/v1/auth/validate/refresh")
        .queryParam("refreshToken", "${refreshToken}")
        .check(status.in(200, 201))
    )
    // 5. Очистка данных после теста
    .exec(
      http("delete_user_data")
        .delete("/api/v1/auth/user/${clientUid}")
        .check(status.in(200, 404, 400)) 
    )

  // Расчет нагрузки
  val requestsPerUser = 5
  
  // Для достижения targetRps нужно: targetRps / requestsPerUser пользователей в секунду
  val usersPerSec = (targetRps.toDouble / requestsPerUser).max(1.0)

  // Время подъема нагрузки и стабильной фазы
  val rampUpTimeSeconds = (targetRps.toInt / 25)
  val steadyStateDurationSeconds = 60
  val totalDurationSeconds = rampUpTimeSeconds + steadyStateDurationSeconds

  println(s"=== Gatling Configuration ===")
  println(s"Target RPS: $targetRps")
  println(s"Ramp-up time: $rampUpTimeSeconds seconds")
  println(s"Steady-state duration: $steadyStateDurationSeconds seconds") 
  println(s"Total test duration: $totalDurationSeconds seconds")
  println(s"Users per second: $usersPerSec")
  println(s"Requests per user: $requestsPerUser")
  println(s"Expected total requests: ${targetRps * totalDurationSeconds}")
  println(s"Service name: $serviceName")
  println(s"Response time threshold: 500 ms")

  // Настройка инъекции с плавным подъемом и стабильной нагрузкой
  val injectionSteps = Seq(
    // Для инициализации всего
    constantUsersPerSec(10).during(10.second),
    // Плавный подъем до целевой нагрузки за 20 секунд
    rampUsersPerSec(10).to(usersPerSec).during(rampUpTimeSeconds.seconds),
    constantUsersPerSec(10).during(1.second),
    // Стабильная нагрузка в течение 60 секунд
    constantUsersPerSec(usersPerSec).during(steadyStateDurationSeconds.seconds),

    // маленькая нагрузка
    constantUsersPerSec(100).during(steadyStateDurationSeconds.seconds)
  )

  // Настройка теста
  setUp(
    authFlowScenario.inject(injectionSteps).protocols(httpProtocol)
  )
  .maxDuration((totalDurationSeconds + 10).seconds) // +10 секунд буфер
  .assertions(
    global.responseTime.percentile(95).lt(500),
    global.failedRequests.percent.lt(5.0)
  )

  def simulationName: String = s"GatewaySimulation-${serviceName}-${targetRps}rps"
}