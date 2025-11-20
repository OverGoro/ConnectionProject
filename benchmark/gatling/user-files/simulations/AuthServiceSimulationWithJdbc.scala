package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import java.util.UUID

class AuthServiceSimulationWithJdbc extends Simulation {

  val targetRps = System.getProperty("targetRps", "100").toInt
  val targetHost = System.getProperty("targetHost", "localhost")
  val targetPort = System.getProperty("targetPort", "8081")
  val durationSec = System.getProperty("durationSec", "60").toInt
  val serviceName = System.getProperty("serviceName", "auth-service")

  // Генерация уникальных тестовых данных
  def randomEmail(): String = s"test${System.currentTimeMillis()}${scala.util.Random.nextInt(10000)}@example.com"
  def randomUsername(): String = s"user${System.currentTimeMillis()}${scala.util.Random.nextInt(10000)}"
  val testPassword = "TestPassword123!"
  val testBirthDate = "1990-01-01T00:00:00.000Z"

  val httpProtocol = http
    .baseUrl(s"http://${targetHost}:${targetPort}")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .userAgentHeader("Gatling Benchmark")
    .disableCaching


  // Основной сценарий
  val authFlowScenario = scenario("Complete Auth Flow")
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
        .check(status.in(200, 201))
        .check(responseTimeInMillis.lt(500))
    )
    // 2. Логин
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
          jsonPath("$.refreshToken").saveAs("refreshToken")
        )
        .check(responseTimeInMillis.lt(500))
    )
    // 3. Валидация access token
    .exec(
      http("validate_access")
        .post("/api/v1/auth/validate/access")
        .queryParam("accessToken", "${accessToken}")
        .check(status.in(200, 201))
        .check(responseTimeInMillis.lt(500))
    )
    // 4. Валидация refresh token
    .exec(
      http("validate_refresh")
        .post("/api/v1/auth/validate/refresh")
        .queryParam("refreshToken", "${refreshToken}")
        .check(status.in(200, 201))
        .check(responseTimeInMillis.lt(500))
    )
    // 5. очистка добавленного, чтобы диск не забивался
    .exec(
      http("delete_user_data")
        .delete("/api/v1/auth/user/${uid}")
        .check(status.in(200, 404, 400)) 
        .check(responseTimeInMillis.lt(500)) 
    )

  val requestsPerUser = 5
  val usersPerSec = (targetRps.toDouble / requestsPerUser).max(1.0)

  val injectionProfile = constantUsersPerSec(usersPerSec).during(durationSec.seconds)

  setUp(
    authFlowScenario.inject(injectionProfile)
  ).protocols(httpProtocol)
    .maxDuration(durationSec.seconds + 30.seconds)
    .assertions(
      global.responseTime.percentile(95).lt(500)
    )
}