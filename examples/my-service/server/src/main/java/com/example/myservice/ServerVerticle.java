package com.example.myservice;

import com.example.myservice.services.ProductService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.BodyHandler;

public class ServerVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(ServerVerticle.class);

  @Override
  public void start() throws Exception {
    JsonObject config = new JsonObject().put("url", "jdbc:mysql://127.0.0.1:3306/vertx_example?user=root&password=root");
    DeploymentOptions options = new DeploymentOptions().setConfig(config);

    vertx.deployVerticle("service:com.example.myservice.product-service", options, res -> {
      if (res.succeeded()) {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.route().handler(routingContext -> {
          routingContext.response().putHeader("Content-Type", "application/json");
          routingContext.next();
        });

        router.get("/products").handler(this::handleListProduct);

        vertx.createHttpServer().requestHandler(router::accept).listen(8081);
      } else {
        throw new RuntimeException(res.cause());
      }
    });
  }

  private void handleListProduct(RoutingContext routingContext) {
    ProductService service = ProductService.createProxy(vertx, "myservice.product");

    service.list(res -> {
      if (res.succeeded()) {
        routingContext.response().end(res.result().encode());
      } else {
        routingContext.fail(res.cause());
      }
    });
  }
}
