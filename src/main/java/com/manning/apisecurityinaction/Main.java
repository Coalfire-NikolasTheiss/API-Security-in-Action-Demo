package com.manning.apisecurityinaction;
import com.manning.apisecurityinaction.controller.*;
import org.dalesbred.Database;
import org.h2.jdbcx.JdbcConnectionPool;
import org.json.*;

import java.nio.file.*;
import static spark.Spark.*;
public class Main {


  public static void main(String... args) throws Exception {
  port(8080);               // Use port 8080 (or another open port)
  ipAddress("0.0.0.0");     // Bind to all network interfaces (for external access)
  var datasource = JdbcConnectionPool.create(
"jdbc:h2:mem:natter", "natter", "password"); var database = Database.forDataSource(datasource); createTables(database);
var spaceController =
    new SpaceController(database);
post("/spaces",
    spaceController::createSpace);
get("/spaces/:spaceId",
    spaceController::getSpace);
after((request, response) -> {
  response.type("application/json");
});
internalServerError(new JSONObject()
      .put("error", "internal server error").toString());
    notFound(new JSONObject()
      .put("error", "not found").toString());
}

  private static void createTables(Database database)
      throws Exception {
        var path = Paths.get(
            Main.class.getResource("/schema.sql").toURI());
        database.update(Files.readString(path));
      }
}
