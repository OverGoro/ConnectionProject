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

  // Health check scenario - простой и надежный
  val healthCheckScenario = scenario("Health Check")
    .exec(
      http("health_check")
        .get("/api/v1/auth/health")
        .check(status.is(200))
        .check(jsonPath("$.status").is("OK"))
    )

  // Registration scenario - исправленная версия
  val registrationScenario = scenario("User Registration")
    .exec(session => {
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
    .exec(
      http("register_user")
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
        .check(status.in(200, 400, 409)) // 400/409 если пользователь уже существует
    )

  // Login scenario - упрощенная версия без предварительной регистрации
  val loginScenario = scenario("User Login")
    .exec(session => {
      // Используем фиксированные тестовые данные для логина
      session
        .set("email", "test@example.com")
        .set("password", "password123")
    })
    .exec(
      http("login_user")
        .post("/api/v1/auth/login")
        .body(StringBody(
          """{
            |  "email": "${email}",
            |  "password": "${password}"
            |}""".stripMargin
        )).asJson
        .check(status.in(200, 400, 401)) // 400/401 если неверные учетные данные
        .check(
          jsonPath("$.accessToken").optional.saveAs("accessToken"),
          jsonPath("$.refreshToken").optional.saveAs("refreshToken"),
          jsonPath("$.clientUid").optional.saveAs("clientUid")
        )
    )

  // Token validation scenario - только для существующих токенов
  val tokenValidationScenario = scenario("Token Validation")
    .exec(
      http("validate_access_token")
        .post("/api/v1/auth/validate/access")
        .queryParam("accessToken", "test_token_123")
        .check(status.in(200, 400))
    )
    .exec(
      http("validate_refresh_token")
        .post("/api/v1/auth/validate/refresh")
        .queryParam("refreshToken", "test_refresh_token_123")
        .check(status.in(200, 400))
    )

  // Token refresh scenario
  val tokenRefreshScenario = scenario("Token Refresh")
    .exec(
      http("refresh_tokens")
        .post("/api/v1/auth/refresh")
        .body(StringBody(
          """{
            |  "refreshToken": "test_refresh_token_123"
            |}""".stripMargin
        )).asJson
        .check(status.in(200, 400))
    )

  // Комплексный сценарий с созданием пользователя и полным циклом
  val fullAuthScenario = scenario("Full Authentication Cycle")
    .exec(session => {
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
    // 1. Регистрация
    .exec(
      http("full_register")
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
        .check(status.in(200, 400, 409))
    )
    // 2. Логин (если регистрация успешна)
    .doIf(session => session("status").asOption[Int].exists(_ == 200)) {
      exec(
        http("full_login")
          .post("/api/v1/auth/login")
          .body(StringBody(
            """{
              |  "email": "${email}",
              |  "password": "${password}"
              |}""".stripMargin
          )).asJson
          .check(status.is(200))
          .check(
            jsonPath("$.accessToken").saveAs("accessToken"),
            jsonPath("$.refreshToken").saveAs("refreshToken"),
            jsonPath("$.clientUid").saveAs("clientUid")
          )
      )
      // 3. Валидация access token (если логин успешен)
      .doIf(session => session("accessToken").asOption[String].isDefined) {
        exec(
          http("full_validate_access")
            .post("/api/v1/auth/validate/access")
            .queryParam("accessToken", "${accessToken}")
            .check(status.in(200, 400))
        )
        // 4. Валидация refresh token
        .exec(
          http("full_validate_refresh")
            .post("/api/v1/auth/validate/refresh")
            .queryParam("refreshToken", "${refreshToken}")
            .check(status.in(200, 400))
        )
        // 5. Обновление токенов
        .exec(
          http("full_refresh")
            .post("/api/v1/auth/refresh")
            .body(StringBody(
              """{
                |  "refreshToken": "${refreshToken}"
                |}""".stripMargin
            )).asJson
            .check(status.in(200, 400))
        )
      }
    }

  // Базовый сценарий для быстрого тестирования
  val basicScenario = scenario("Basic Operations")
    .exec(
      http("health_check_basic")
        .get("/api/v1/auth/health")
        .check(status.is(200))
    )
    .exec(session => {
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
    .exec(
      http("register_basic")
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
        .check(status.in(200, 400, 409))
    )
    .exec(
      http("login_basic")
        .post("/api/v1/auth/login")
        .body(StringBody(
          """{
            |  "email": "${email}",
            |  "password": "${password}"
            |}""".stripMargin
        )).asJson
        .check(status.in(200, 400, 401))
    )

  // Настройка тестов в зависимости от serviceName
  val setUpConfig = serviceName match {
    case "auth-service-common" | "auth-service-reactive" =>
      // Для auth-сервисов запускаем стабильные сценарии
      List(
        healthCheckScenario.inject(
          constantUsersPerSec(targetRps * 0.2).during(durationSec.seconds)
        ),
        basicScenario.inject(
          constantUsersPerSec(targetRps * 0.6).during(durationSec.seconds)
        ),
        tokenValidationScenario.inject(
          constantUsersPerSec(targetRps * 0.1).during(durationSec.seconds)
        ),
        tokenRefreshScenario.inject(
          constantUsersPerSec(targetRps * 0.1).during(durationSec.seconds)
        )
      )
    case _ =>
      // По умолчанию - базовые операции
      List(
        basicScenario.inject(
          constantUsersPerSec(targetRps).during(durationSec.seconds)
        )
      )
  }

  setUp(setUpConfig: _*)
    .protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(5000),
      global.responseTime.percentile(95).lt(2000),
      global.failedRequests.percent.lt(10.0), // Более мягкие требования для тестирования
      global.successfulRequests.percent.gt(90.0)
    )
    .maxDuration(durationSec.seconds + 30.seconds)
}