var Router = require("vertx-apex-js/router");
var BodyHandler = require("vertx-apex-js/body_handler");
var ProductService = require("myservice-product-js/product_service");

var config = { url: "jdbc:mysql://127.0.0.1:3306/vertx_example?user=root&password=root" };

vertx.deployVerticle("service:com.example.myservice.product-service", { config: config }, function(res, err) {
    if (err) {
      throw err;
    }

    var router = Router.router(vertx);

    router.route().handler(BodyHandler.create().handle);

    router.get("/products").handler(handleListProduct);

    vertx.createHttpServer().requestHandler(router.accept).listen(8082);
});

function handleListProduct(routingContext) {
  var proxy = ProductService.createProxy(vertx, "myservice.product");
  proxy.list(function(res, err) {
    if (err) {
      routingContext.fail(err);
    } else {
      routingContext.response().putHeader("Content-Type", "application/json").end(JSON.stringify(res));
    }
  });
}