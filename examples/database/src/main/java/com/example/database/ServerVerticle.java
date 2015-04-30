package com.example.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.BodyHandler;
import io.vertx.ext.jdbc.JdbcService;
import io.vertx.ext.sql.SqlConnection;

import java.util.List;

public class ServerVerticle extends AbstractVerticle {

  private final static String CONNECTION_KEY = "connection";

  @Override
  public void start() throws Exception {
    JsonObject config = new JsonObject().put("url", "jdbc:mysql://127.0.0.1:3306/samplerest?user=root&password=root");
    DeploymentOptions options = new DeploymentOptions().setConfig(config);

    vertx.deployVerticle("service:io.vertx.jdbc-service", options, res -> {
      if (res.succeeded()) {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.route().handler(this::setupConnection);
        router.route().handler(routingContext -> {
          routingContext.response().putHeader("Content-Type", "application/json");
          routingContext.next();
        });

        router.get("/products").handler(this::handleListProduct);
        router.get("/products/:productID").handler(this::handleShowProduct);

        vertx.createHttpServer().requestHandler(router::accept).listen(8081);
      } else {
        throw new RuntimeException(res.cause());
      }
    });
  }

  private void setupConnection(RoutingContext routingContext) {
    JdbcService proxy = JdbcService.createEventBusProxy(vertx, "vertx.jdbc");
    proxy.getConnection(res -> {
      if (res.succeeded()) {
        routingContext.put(CONNECTION_KEY, res.result());
        routingContext.addHeadersEndHandler(v -> {
          SqlConnection connection = routingContext.get(CONNECTION_KEY);
          connection.close(c -> {
            if (!c.succeeded()) {
              routingContext.fail(c.cause());
            }
          });
        });
        routingContext.next();
      } else {
        routingContext.fail(res.cause());
      }
    });
  }

  private void handleListProduct(RoutingContext routingContext) {
    SqlConnection connection = routingContext.get(CONNECTION_KEY);

    connection.query("select * from products", res -> {
      if (res.succeeded()) {
        JsonArray arr = new JsonArray();
        List<JsonObject> rows = res.result().getRows();
        rows.forEach(arr::add);
        routingContext.response().end(arr.encode());
      } else {
        routingContext.fail(res.cause());
      }
    });
  }

  private void handleShowProduct(RoutingContext routingContext) {
    String productID = routingContext.request().getParam("productID");

    if (productID == null) {
      routingContext.fail(404);
    } else {
      SqlConnection connection = routingContext.get(CONNECTION_KEY);
      String sql = "select * from products where id = ?";
      JsonArray params = new JsonArray().add(productID);

      connection.queryWithParams(sql, params, res -> {
        if (res.succeeded()) {
          List<JsonObject> rows = res.result().getRows();
          if (rows.isEmpty()) {
            routingContext.fail(404);
          } else {
            routingContext.response().end(rows.get(0).encode());
          }
        } else {
          routingContext.fail(res.cause());
        }
      });
    }
  }
}
