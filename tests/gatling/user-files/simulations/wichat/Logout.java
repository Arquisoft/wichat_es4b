package wichat;

import java.time.Duration;
import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import io.gatling.javaapi.jdbc.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import static io.gatling.javaapi.jdbc.JdbcDsl.*;

public class Logout extends Simulation {

  {
    HttpProtocolBuilder httpProtocol = http
      .baseUrl("https://wichat.uksouth.cloudapp.azure.com")
      .inferHtmlResources(AllowList(), DenyList(".*\\.js", ".*\\.css", ".*\\.gif", ".*\\.jpeg", ".*\\.jpg", ".*\\.ico", ".*\\.woff", ".*\\.woff2", ".*\\.(t|o)tf", ".*\\.png", ".*\\.svg", ".*detectportal\\.firefox\\.com.*"))
      .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
      .acceptEncodingHeader("gzip, deflate, br")
      .acceptLanguageHeader("en-US,en;q=0.5")
      .doNotTrackHeader("1")
      .upgradeInsecureRequestsHeader("1")
      .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:134.0) Gecko/20100101 Firefox/134.0");
    
    Map<CharSequence, String> headers_0 = new HashMap<>();
    headers_0.put("Priority", "u=0, i");
    headers_0.put("Sec-Fetch-Dest", "document");
    headers_0.put("Sec-Fetch-Mode", "navigate");
    headers_0.put("Sec-Fetch-Site", "none");
    headers_0.put("Sec-Fetch-User", "?1");
    headers_0.put("Sec-GPC", "1");
    
    Map<CharSequence, String> headers_1 = new HashMap<>();
    headers_1.put("Origin", "https://wichat.uksouth.cloudapp.azure.com");
    headers_1.put("Priority", "u=0, i");
    headers_1.put("Sec-Fetch-Dest", "document");
    headers_1.put("Sec-Fetch-Mode", "navigate");
    headers_1.put("Sec-Fetch-Site", "same-origin");
    headers_1.put("Sec-Fetch-User", "?1");
    headers_1.put("Sec-GPC", "1");
    
    Map<CharSequence, String> headers_2 = new HashMap<>();
    headers_2.put("Priority", "u=0, i");
    headers_2.put("Sec-Fetch-Dest", "document");
    headers_2.put("Sec-Fetch-Mode", "navigate");
    headers_2.put("Sec-Fetch-Site", "same-origin");
    headers_2.put("Sec-Fetch-User", "?1");
    headers_2.put("Sec-GPC", "1");


    ScenarioBuilder scn = scenario("Logout")
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
      .pause(2)
      .exec(
        http("GET /logout")
          .get("/logout")
          .headers(headers_2)
      );

	//setUp(scn.injectOpen(atOnceUsers(10))).protocols(httpProtocol);
	  //setUp(scn.injectOpen(atOnceUsers(1000))).protocols(httpProtocol);
	  
	  setUp(scn.injectOpen(rampUsers(200).during(20))).protocols(httpProtocol);
	  //setUp(scn.injectOpen(rampUsers(2000).during(60))).protocols(httpProtocol);
  }
}
