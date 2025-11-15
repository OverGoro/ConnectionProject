package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import java.util.UUID

class AuthServiceSimulation extends Simulation {

  val targetRps = System.getProperty("targetRps", "100").toInt
  val targetHost = System.getProperty("targetHost", "localhost")
  val targetPort = System.getProperty("targetPort", "8081")
  val durationSec = System.getProperty("durationSec", "60").toInt
  val serviceName = System.getProperty("serviceName", "auth-service")
  val refreshDelayMs = System.getProperty("refreshDelayMs", "1000").toInt // Задержка перед refresh

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

  // Основной сценарий: полный цикл аутентификации для каждого пользователя
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
        .check(status.in(200, 201)) // 200 - успех, 400 - ошибка, 409 - уже существует
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
        .check(status.in(200, 201)) // Ожидаем 201 для успешного логина
        .check(
          jsonPath("$.accessToken").saveAs("accessToken"),
          jsonPath("$.refreshToken").saveAs("refreshToken"),
          jsonPath("$.clientUid").saveAs("clientUid")
        )
    )
    // // 3. Обновление токенов
    // .exec(
    //   http("refresh")
    //     .post("/api/v1/auth/refresh")
    //     .body(StringBody(
    //       """{
    //         |  "refreshToken": "${refreshToken}"
    //         |}""".stripMargin
    //     )).asJson
    //     .check(status.is(200)) // Ожидаем 201 для успешного обновления
    //     .check(
    //       jsonPath("$.accessToken").saveAs("newAccessToken"),
    //       jsonPath("$.refreshToken").saveAs("newRefreshToken")
    //     )
    // )
    // 4. Валидация access token (используем новый токен после refresh)
    .exec(
      http("validate_access")
        .post("/api/v1/auth/validate/access")
        .queryParam("accessToken", "${accessToken}")
        .check(status.in(200, 201))
    )
    // 5. Валидация refresh token (используем новый токен после refresh)
    .exec(
      http("validate_refresh")
        .post("/api/v1/auth/validate/refresh")
        .queryParam("refreshToken", "${refreshToken}")
        .check(status.in(200, 201))
    )

  // Health check scenario (отдельный сценарий для проверки здоровья)
  val healthCheckScenario = scenario("Health Check")
    .exec(
      http("health_check")
        .get("/api/v1/auth/health")
        .check(status.in(200, 201))
        .check(jsonPath("$.status").is("OK"))
    )

  // Расчет нагрузки
  // Каждый пользователь делает 5 запросов: register + login + refresh + validate_access + validate_refresh
  val requestsPerUser = 4
  
  // Для достижения targetRps нужно: targetRps / requestsPerUser пользователей в секунду
  val usersPerSec = (targetRps.toDouble / requestsPerUser).max(1.0)

  println(s"=== Gatling Configuration ===")
  println(s"Target RPS: $targetRps")
  println(s"Duration: $durationSec seconds") 
  println(s"Users per second: $usersPerSec")
  println(s"Requests per user: $requestsPerUser")
  println(s"Expected total requests: ${targetRps * durationSec}")
  println(s"Service name: $serviceName")

  // Настройка тестов в зависимости от serviceName
  val setUpConfig = serviceName match {
    case "auth-service-common" | "auth-service-reactive" =>
      // Распределяем нагрузку: 80% на auth flow, 20% на health checks
      val authFlowUsers = (usersPerSec * 1).max(1.0)
      // val healthCheckUsers = (targetRps * 0.2).max(1.0)
      
      List(
        authFlowScenario.inject(
          constantUsersPerSec(authFlowUsers).during(durationSec.seconds)
        )
        // ,
        // healthCheckScenario.inject(
        //   constantUsersPerSec(healthCheckUsers).during(durationSec.seconds)
        // )
      )
    case _ =>
      // По умолчанию - только основной сценарий аутентификации
      List(
        authFlowScenario.inject(
          constantUsersPerSec(usersPerSec).during(durationSec.seconds)
        )
      )
  }

  setUp(setUpConfig: _*)
    .protocols(httpProtocol)
    .maxDuration(durationSec.seconds + 30.seconds) // Добавляем запас времени
}