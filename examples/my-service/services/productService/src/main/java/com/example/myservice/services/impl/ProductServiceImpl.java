package com.example.myservice.services.impl;

import com.example.myservice.services.ProductService;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.DefaultMapper;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.ResultIterator;

import java.util.Map;

public class ProductServiceImpl implements ProductService {

  private static final Logger log = LoggerFactory.getLogger(ProductService.class);

  private final Vertx vertx;
  private final JsonObject config;

  private ComboPooledDataSource dataSource = null;
  private DBI dbi = null;

  public ProductServiceImpl(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.config = config;
  }

  @Override
  public void start() {
    String url = config.getString("url");

    if (url == null) {
      throw new NullPointerException("url cannot be null");
    }

    ComboPooledDataSource cpds = new ComboPooledDataSource();
    cpds.setJdbcUrl(url);
    dataSource = cpds;
    dbi = new DBI(dataSource);
  }

  @Override
  public void stop() {
    dataSource.close();
  }

  @Override
  public void list(Handler<AsyncResult<JsonArray>> resultHandler) {
    Handle handle = dbi.open();
    ResultIterator<Map<String, Object>> iter = handle.createQuery("select * from products")
        .map(new DefaultMapper())
        .iterator();
    JsonArray results = new JsonArray();
    while (iter.hasNext()) {
      results.add(iter.next());
    }
    resultHandler.handle(Future.succeededFuture(results));
  }
}