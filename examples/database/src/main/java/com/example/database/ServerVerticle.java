package com.example.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.BodyHandler;
import io.vertx.ext.jdbc.JdbcService;
import io.vertx.ext.sql.SqlConnection;

import java.util.List;

public class ServerVerticle extends AbstractVerticle {

  private final static String CONNECTION_KEY = "connection";
  private final static Logger log = LoggerFactory.getLogger(ServerVerticle.class);

  @Override
  public void start() throws Exception {
    JsonObject config = new JsonObject().put("url", "jdbc:mysql://127.0.0.1:3306/vertx_example?user=root&password=root");
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
        router.put("/products/:productID").handler(this::handleUpdateProduct);
        router.post("/products").handler(this::handleCreateProduct);

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
              log.error(c.cause());
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

  private void handleUpdateProduct(RoutingContext routingContext) {
    String productID = routingContext.request().getParam("productID");

    if (productID == null) {
      routingContext.fail(404);
    } else {
      SqlConnection connection = routingContext.get(CONNECTION_KEY);
      JsonObject product = routingContext.getBodyAsJson();
      String sql = "update products set name = ?, price = ?, weight = ? where id = ?";
      JsonArray params = new JsonArray()
          .add(product.getValue("name"))
          .add(product.getValue("price"))
          .add(product.getValue("weight"))
          .add(productID);

      connection.updateWithParams(sql, params, res -> {
        if (res.succeeded()) {
          if (res.result().getUpdated() == 0) {
            routingContext.fail(404);
          } else {
            routingContext.response().end();
          }
        } else {
          routingContext.fail(res.cause());
        }
      });
    }
  }

  private void handleCreateProduct(RoutingContext routingContext) {
    JsonObject product = routingContext.getBodyAsJson();
    String sql = "insert into products (name, price, weight) values (?, ?, ?)";
    JsonArray params = new JsonArray()
        .add(product.getValue("name"))
        .add(product.getValue("price"))
        .add(product.getValue("weight"));
    SqlConnection connection = routingContext.get(CONNECTION_KEY);

    connection.updateWithParams(sql, params, res -> {
      if (res.succeeded()) {
        routingContext.response().end();
      } else {
        routingContext.fail(res.cause());
      }
    });
  }
}
