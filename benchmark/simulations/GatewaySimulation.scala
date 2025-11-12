// simulations/GatewaySimulation.scala
class GatewaySimulation extends Simulation {
  
  val targetHost = System.getProperty("targetHost", "localhost")
  val targetPort = System.getProperty("targetPort", "8080")
  val rps = System.getProperty("targetRps", "100").toDouble
  
  val httpProtocol = http
    .baseUrl(s"http://$targetHost:$targetPort")
    .acceptHeader("application/json")
    .userAgentHeader("Gatling Benchmark")
  
  // Тестируем критические эндпоинты вашего gateway
  val scn = scenario("Gateway Service Load Test")
    .exec(http("Health Check")
      .get("/actuator/health")
      .check(status.is(200)))
    // .exec(http("Swagger UI") 
    //   .get("/swagger-ui.html")
    //   .check(status.is(200)))
    // .exec(http("API Docs")
    //   .get("/v3/api-docs")
    //   .check(status.is(200)))
    // Добавьте здесь ваши бизнес-эндпоинты
    // .exec(http("Some Business Endpoint")
    //   .post("/api/some-endpoint")
    //   .body(StringBody("""{"data": "test"}""")).asJson
    //   .check(status.is(200)))
  
  setUp(
    scn.inject(
      constantUsersPerSec(rps) during (Duration(System.getProperty("durationSec", "120").toInt, "seconds"))
    ).protocols(httpProtocol)
  )
}
