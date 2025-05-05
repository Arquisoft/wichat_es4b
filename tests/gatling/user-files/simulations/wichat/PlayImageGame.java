package wichat;

import java.time.Duration;
import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import io.gatling.javaapi.jdbc.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import static io.gatling.javaapi.jdbc.JdbcDsl.*;

public class PlayImageGame extends Simulation {

  {
    HttpProtocolBuilder httpProtocol = http
      .baseUrl("https://wichat.uksouth.cloudapp.azure.com")
      .inferHtmlResources(AllowList(), DenyList(".*\\.css", ".*\\.gif", ".*\\.jpeg", ".*\\.jpg", ".*\\.JPG", ".*\\.ico", ".*\\.woff", ".*\\.woff2", ".*\\.(t|o)tf", ".*\\.png", ".*\\.svg", ".*detectportal\\.firefox\\.com.*", ".*\\.js", ".*\\.css", ".*\\.gif", ".*\\.jpeg", ".*\\.jpg", ".*\\.ico", ".*\\.woff", ".*\\.woff2", ".*\\.(t|o)tf", ".*\\.png", ".*\\.svg", ".*detectportal\\.firefox\\.com.*", ".*\\.js", ".*\\.css", ".*\\.gif", ".*\\.jpeg", ".*\\.jpg", ".*\\.ico", ".*\\.woff", ".*\\.woff2", ".*\\.(t|o)tf", ".*\\.png", ".*\\.svg", ".*detectportal\\.firefox\\.com.*", ".*\\.js", ".*\\.css", ".*\\.gif", ".*\\.jpeg", ".*\\.jpg", ".*\\.ico", ".*\\.woff", ".*\\.woff2", ".*\\.(t|o)tf", ".*\\.png", ".*\\.svg", ".*detectportal\\.firefox\\.com.*"))
      .acceptHeader("*/*")
      .acceptEncodingHeader("gzip, deflate, br")
      .acceptLanguageHeader("es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3")
      .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:138.0) Gecko/20100101 Firefox/138.0");
    
    Map<CharSequence, String> headers_0 = new HashMap<>();
    headers_0.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    headers_0.put("Priority", "u=0, i");
    headers_0.put("Sec-Fetch-Dest", "document");
    headers_0.put("Sec-Fetch-Mode", "navigate");
    headers_0.put("Sec-Fetch-Site", "same-origin");
    headers_0.put("Sec-Fetch-User", "?1");
    headers_0.put("Upgrade-Insecure-Requests", "1");
    
    Map<CharSequence, String> headers_1 = new HashMap<>();
    headers_1.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    headers_1.put("Origin", "https://wichat.uksouth.cloudapp.azure.com");
    headers_1.put("Priority", "u=0, i");
    headers_1.put("Sec-Fetch-Dest", "document");
    headers_1.put("Sec-Fetch-Mode", "navigate");
    headers_1.put("Sec-Fetch-Site", "same-origin");
    headers_1.put("Sec-Fetch-User", "?1");
    headers_1.put("Upgrade-Insecure-Requests", "1");
    
    Map<CharSequence, String> headers_3 = new HashMap<>();
    headers_3.put("Sec-Fetch-Dest", "empty");
    headers_3.put("Sec-Fetch-Mode", "cors");
    headers_3.put("Sec-Fetch-Site", "same-origin");
    headers_3.put("X-Requested-With", "XMLHttpRequest");
    
    Map<CharSequence, String> headers_5 = new HashMap<>();
    headers_5.put("Priority", "u=0");
    headers_5.put("Sec-Fetch-Dest", "empty");
    headers_5.put("Sec-Fetch-Mode", "cors");
    headers_5.put("Sec-Fetch-Site", "same-origin");
    headers_5.put("X-Requested-With", "XMLHttpRequest");
    
    Map<CharSequence, String> headers_6 = new HashMap<>();
    headers_6.put("Accept", "text/html, */*; q=0.01");
    headers_6.put("Priority", "u=0");
    headers_6.put("Sec-Fetch-Dest", "empty");
    headers_6.put("Sec-Fetch-Mode", "cors");
    headers_6.put("Sec-Fetch-Site", "same-origin");
    headers_6.put("X-Requested-With", "XMLHttpRequest");


    ScenarioBuilder scn = scenario("PlayImageGame")
      .exec(
		http("GET /login")
			.get("/login")
			.headers(headers_0)
			.check(css("input[name=_csrf]", "value").saveAs("csrfToken"))
      )
      .pause(1)
      .exec( 
        http("POST /login")
          .post("/login")
          .headers(headers_1)
          .formParam("username", "test")
          .formParam("password", "test")
          .formParam("_csrf", "#{csrfToken}")
      )
      .pause(1)
      .exec(
        http("GET /game/image")
          .get("/game/image")
          .headers(headers_0)
      )
      .pause(1)
      .exec(
        http("Play image game")
          .get("/game/image/currentQuestion")
          .headers(headers_3)
          .resources(
            http("request_4")
              .get("/game/image/points")
              .headers(headers_3),
            http("request_5")
              .get("/game/image/39/155")
              .headers(headers_5),
            http("request_6")
              .get("/game/image/update")
              .headers(headers_6),
            http("request_7")
              .get("/game/image/points")
              .headers(headers_3),
            http("request_8")
              .get("/game/image/currentQuestion")
              .headers(headers_3),
            http("request_9")
              .get("/game/image/36/144")
              .headers(headers_5),
            http("request_10")
              .get("/game/image/currentQuestion")
              .headers(headers_3),
            http("request_11")
              .get("/game/image/points")
              .headers(headers_3),
            http("request_12")
              .get("/game/image/update")
              .headers(headers_6),
            http("request_13")
              .get("/game/image/50/200")
              .headers(headers_5),
            http("request_14")
              .get("/game/image/points")
              .headers(headers_3),
            http("request_15")
              .get("/game/image/currentQuestion")
              .headers(headers_3),
            http("request_16")
              .get("/game/image/update")
              .headers(headers_6),
            http("request_17")
              .get("/game/image/5/17")
              .headers(headers_5),
            http("request_18")
              .get("/game/image/points")
              .headers(headers_3),
            http("request_19")
              .get("/game/image/currentQuestion")
              .headers(headers_3),
            http("request_20")
              .get("/game/image/update")
              .headers(headers_6),
            http("request_21")
              .get("/game/image/currentQuestion")
              .headers(headers_3),
            http("request_22")
              .get("/game/image/points")
              .headers(headers_3),
            http("request_23")
              .get("/game/image/points")
              .headers(headers_3),
            http("request_24")
              .get("/game/image/currentQuestion")
              .headers(headers_3),
            http("request_25")
              .get("/game/image/points")
              .headers(headers_3),
            http("request_26")
              .get("/game/image/currentQuestion")
              .headers(headers_3)
          )
      );

	  //setUp(scn.injectOpen(atOnceUsers(10))).protocols(httpProtocol);
	  //setUp(scn.injectOpen(atOnceUsers(1000))).protocols(httpProtocol);
	  
	  setUp(scn.injectOpen(rampUsers(200).during(20))).protocols(httpProtocol);
	  //setUp(scn.injectOpen(rampUsers(2000).during(60))).protocols(httpProtocol);
  }
}
