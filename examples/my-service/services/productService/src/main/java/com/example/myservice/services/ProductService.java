package com.example.myservice.services;

import com.example.myservice.services.impl.ProductServiceImpl;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

@ProxyGen
@VertxGen
public interface ProductService {

  static ProductService create(Vertx vertx, JsonObject config) {
    return new ProductServiceImpl(vertx, config);
  }

  static ProductService createProxy(Vertx vertx, String address) {
    return ProxyHelper.createProxy(ProductService.class, vertx, address);
  }

  @ProxyIgnore
  public void start();

  @ProxyIgnore
  public void stop();

  void list(Handler<AsyncResult<JsonArray>> resultHandler);
}