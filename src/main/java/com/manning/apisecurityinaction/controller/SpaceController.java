package com.manning.apisecurityinaction.controller;
import org.dalesbred.Database;
import org.dalesbred.result.RowMapper;
import org.json.*;
import spark.*;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;
public class SpaceController {
  private final Database database;
  public SpaceController(Database database) {
    this.database = database;
  }

      // DTO class to map the Space data
    public static class Space {
        private long spaceId;
        private String name;
        private String owner;

        // Constructor
        public Space(long spaceId, String name, String owner) {
            this.spaceId = spaceId;
            this.name = name;
            this.owner = owner;
        }

        // Getters
        public long getSpaceId() {
            return spaceId;
        }

        public String getName() {
            return name;
        }

        public String getOwner() {
            return owner;
        }
    }
    
public JSONObject createSpace(Request request, Response response)
      throws SQLException {
    var json = new JSONObject(request.body());
    var spaceName = json.getString("name");
    var owner = json.getString("owner");
    return database.withTransaction(tx -> {
      var spaceId = database.findUniqueLong(
          "SELECT NEXT VALUE FOR space_id_seq;");
      // WARNING: this next line of code contains a
      // security vulnerability!
      database.updateUnique(
          "INSERT INTO spaces(space_id, name, owner) " +
              "VALUES(" + spaceId + ", '" + spaceName +
              "', '" + owner + "');");
response.status(201);
response.header("Location", "/spaces/" + spaceId);
      return new JSONObject()
          .put("name", spaceName)
          .put("uri", "/spaces/" + spaceId);
}); }
// Method to get a space by its ID
public JSONObject getSpace(Request request, Response response) throws SQLException {
    String spaceIdStr = request.params(":spaceId");

    try {
        long spaceId = Long.parseLong(spaceIdStr); // Convert spaceId to long

        // Define the query to fetch space details
        String query = "SELECT space_id, name, owner FROM spaces WHERE space_id = ?";

        // Execute the query and retrieve the result as a Space object
        Space result = database.findUnique(Space.class, query, spaceId);

        if (result == null) {
            response.status(404); // Space not found
            return new JSONObject().put("error", "Space not found");
        }

        // Create the response JSON
        JSONObject jsonResult = new JSONObject();
        jsonResult.put("space_id", result.getSpaceId());
        jsonResult.put("name", result.getName());
        jsonResult.put("owner", result.getOwner());

        response.status(200); // OK status
        response.type("application/json");
        return jsonResult;

    } catch (NumberFormatException e) {
        response.status(400); // Bad request if spaceId is invalid
        return new JSONObject().put("error", "Invalid space ID format");
    }
}
}
