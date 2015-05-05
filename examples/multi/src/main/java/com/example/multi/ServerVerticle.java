package com.example.multi;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.BodyHandler;


/**
 * Created by tajimaj on 2015/04/20.
 */
public class ServerVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(ServerVerticle.class);

  @Override
  public void start(Future<Void> startFuture) {
    DeploymentOptions options = new DeploymentOptions().setWorker(true);
    vertx.deployVerticle("com.example.multi.ProductProcessorVerticle", options, res -> {
      if (res.succeeded()) {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.route().handler(routingContext -> {
          routingContext.response().putHeader("Content-Type", "application/json");
          routingContext.next();
        });

        router.get("/products").handler(this::handleListProduct);

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
        startFuture.complete();
      } else {
        startFuture.fail(res.cause());
      }
    });
  }

  private void handleListProduct(RoutingContext routingContext) {
    EventBus eventBus = vertx.eventBus();

    eventBus.send("products.list", null, (AsyncResult<Message<JsonArray>> res) -> {
      if (res.failed()) {
        routingContext.fail(res.cause());
      } else {
        routingContext.response().end(res.result().body().encode());
      }
    });
  }
}