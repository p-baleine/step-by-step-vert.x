# coding: utf-8
require 'vertx-apex/router'
require 'vertx-apex/body_handler'
require 'myservice-product/product_service'

config = {url: 'jdbc:mysql://127.0.0.1:3306/vertx_example?user=root&password=root'}

$vertx.deploy_verticle 'service:com.example.myservice.product-service', config: config do |res_err, res|
  raise res_err unless res_err.nil?

  router = VertxApex::Router.router($vertx)

  router.route.handler(&VertxApex::BodyHandler.create.method(:handle))

  router.get("/products").handler(&self.method(:handle_list_product))

  $vertx.create_http_server.request_handler(&router.method(:accept)).listen 8083
end

def handle_list_product(routing_context)
  proxy = MyserviceProduct::ProductService.create_proxy $vertx, 'myservice.product'
  proxy.list do |res_err, res|
    if res_err
      routing_context.fail res_err
    else
      routing_context.response
        .put_header("Content-Type", "application/json")
        .end(res.to_s)
    end
  end
end
