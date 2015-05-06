package com.example.myservice.services;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.serviceproxy.ProxyHelper;

public class ProductServiceVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(ProductServiceVerticle.class);

  private ProductService service;

  @Override
  public void start() {
    service = ProductService.create(vertx, config());

    String address = config().getString("address");
    if (address == null) {
      throw new IllegalStateException("address field must be specified in config for service verticle");
    }

    ProxyHelper.registerService(ProductService.class, vertx, service, address);

    service.start();
  }
}
