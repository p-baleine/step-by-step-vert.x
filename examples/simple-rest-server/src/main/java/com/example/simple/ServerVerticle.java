package com.example.simple;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.BodyHandler;

import java.util.HashMap;
import java.util.Map;

public class ServerVerticle extends AbstractVerticle {

  private Map<String, JsonObject> store = new HashMap<String, JsonObject>() {
    {
      put("1", new JsonObject().put("id", "1").put("name", "Egg Whisk").put("price", 150));
      put("2", new JsonObject().put("id", "2").put("name", "Tea Cosy").put("price", 100));
      put("3", new JsonObject().put("id", "3").put("name", "Spatula").put("price", 30));
    }
  };

  @Override
  public void start() throws Exception {
    Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create());

    router.get("/products").handler(this::handleListProduct);
    router.get("/products/:productID").handler(this::handleShowProduct);
    router.post("/products").handler(this::handleCreateProduct);
    router.put("/products/:productID").handler(this::handleUpdateProduct);

    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }

  private void handleListProduct(RoutingContext routingContext) {
    JsonArray result = new JsonArray();
    store.values().forEach(result::add);
    routingContext.response()
        .putHeader("Content-Type", "application/json")
        .end(result.encode());
  }

  private void handleShowProduct(RoutingContext routingContext) {
    String productID = routingContext.request().getParam("productID");
    JsonObject product = store.get(productID);
    if (product == null) {
      routingContext.fail(404);
    } else {
      routingContext.response()
          .putHeader("Content-Type", "application/json")
          .end(product.encode());
    }
  }

  private void handleCreateProduct(RoutingContext routingContext) {
    JsonObject product = routingContext.getBodyAsJson();
    String productID = String.valueOf(store.size() + 1);
    product.put("id", productID);
    store.put(productID, product);
    routingContext.response()
        .putHeader("Content-Type", "application/json")
        .end();
  }

  private void handleUpdateProduct(RoutingContext routingContext) {
    String productID = routingContext.request().getParam("productID");
    JsonObject product = store.get(productID);
    if (product == null) {
      routingContext.fail(404);
    } else {
      JsonObject newProduct = routingContext.getBodyAsJson();
      newProduct.put("id", productID);
      store.put(productID, newProduct);
      routingContext.response()
          .putHeader("Content-Type", "application/json")
          .end();
    }
  }
}