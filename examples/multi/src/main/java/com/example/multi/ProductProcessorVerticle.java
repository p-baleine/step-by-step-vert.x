package com.example.multi;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.DefaultMapper;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.ResultIterator;

import java.util.Map;

/**
 * Created by tajimaj on 2015/04/20.
 */
public class ProductProcessorVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(ProductProcessorVerticle.class);

  private ComboPooledDataSource dataSource = null;
  private DBI dbi = null;

  @Override
  public void start() throws Exception {
    dataSource = new ComboPooledDataSource();
    dataSource.setDriverClass("com.mysql.jdbc.Driver");
    dataSource.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/vertx_example?user=root&password=root");
    dbi = new DBI(dataSource);
    EventBus eventBus = vertx.eventBus();

    eventBus.consumer("products.list", this::listProduct);
  }

  @Override
  public void stop() throws Exception {
    dataSource.close();
  }

  private void listProduct(Message<Object> message) {
    Handle h = dbi.open();
    ResultIterator<Map<String, Object>> results = h.createQuery("select * from products")
        .map(new DefaultMapper())
        .iterator();
    JsonArray arr = new JsonArray();
    while (results.hasNext()) {
      arr.add(results.next());
    }
    message.reply(arr);
    h.close();
  }
}