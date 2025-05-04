package wichat;

import java.time.Duration;
import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import io.gatling.javaapi.jdbc.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import static io.gatling.javaapi.jdbc.JdbcDsl.*;

public class GlobalImageRanking extends Simulation {

  {
    HttpProtocolBuilder httpProtocol = http
      .baseUrl("https://wichat.uksouth.cloudapp.azure.com")
      .inferHtmlResources(AllowList(), DenyList(".*\\.js", ".*\\.css", ".*\\.gif", ".*\\.jpeg", ".*\\.jpg", ".*\\.ico", ".*\\.woff", ".*\\.woff2", ".*\\.(t|o)tf", ".*\\.png", ".*\\.svg", ".*detectportal\\.firefox\\.com.*"))
      .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
      .acceptEncodingHeader("gzip, deflate, br")
      .acceptLanguageHeader("en-US,en;q=0.5")
      .doNotTrackHeader("1")
      .upgradeInsecureRequestsHeader("1")
      .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:137.0) Gecko/20100101 Firefox/137.0");
    
    Map<CharSequence, String> headers_0 = new HashMap<>();
    headers_0.put("Priority", "u=0, i");
    headers_0.put("Sec-Fetch-Dest", "document");
    headers_0.put("Sec-Fetch-Mode", "navigate");
    headers_0.put("Sec-Fetch-Site", "none");
    headers_0.put("Sec-Fetch-User", "?1");
    headers_0.put("Sec-GPC", "1");


    ScenarioBuilder scn = scenario("GlobalImageRanking")
      .exec(
        http("GET /ranking/image/global")
          .get("/ranking/image/global")
          .headers(headers_0)
      );

	//setUp(scn.injectOpen(atOnceUsers(10))).protocols(httpProtocol);
	  //setUp(scn.injectOpen(atOnceUsers(1000))).protocols(httpProtocol);
	  
	  setUp(scn.injectOpen(rampUsers(200).during(20))).protocols(httpProtocol);
	  //setUp(scn.injectOpen(rampUsers(2000).during(60))).protocols(httpProtocol);
  }
}
