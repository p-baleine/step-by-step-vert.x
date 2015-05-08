require 'vertx/util/utils.rb'
# Generated from com.example.myservice.services.ProductService
module MyserviceProduct
  class ProductService
    # @private
    # @param j_del [::MyserviceProduct::ProductService] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::MyserviceProduct::ProductService] the underlying java delegate
    def j_del
      @j_del
    end
    # @param [::Vertx::Vertx] vertx
    # @param [Hash{String => Object}] config
    # @return [::MyserviceProduct::ProductService]
    def self.create(vertx=nil,config=nil)
      if vertx.class.method_defined?(:j_del) && config.class == Hash && !block_given?
        return ::MyserviceProduct::ProductService.new(Java::ComExampleMyserviceServices::ProductService.java_method(:create, [Java::IoVertxCore::Vertx.java_class,Java::IoVertxCoreJson::JsonObject.java_class]).call(vertx.j_del,::Vertx::Util::Utils.to_json_object(config)))
      end
      raise ArgumentError, "Invalid arguments when calling create(vertx,config)"
    end
    # @param [::Vertx::Vertx] vertx
    # @param [String] address
    # @return [::MyserviceProduct::ProductService]
    def self.create_proxy(vertx=nil,address=nil)
      if vertx.class.method_defined?(:j_del) && address.class == String && !block_given?
        return ::MyserviceProduct::ProductService.new(Java::ComExampleMyserviceServices::ProductService.java_method(:createProxy, [Java::IoVertxCore::Vertx.java_class,Java::java.lang.String.java_class]).call(vertx.j_del,address))
      end
      raise ArgumentError, "Invalid arguments when calling create_proxy(vertx,address)"
    end
    # @return [void]
    def start
      if !block_given?
        return @j_del.java_method(:start, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling start()"
    end
    # @return [void]
    def stop
      if !block_given?
        return @j_del.java_method(:stop, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling stop()"
    end
    # @yield 
    # @return [void]
    def list
      if block_given?
        return @j_del.java_method(:list, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling list()"
    end
  end
end
