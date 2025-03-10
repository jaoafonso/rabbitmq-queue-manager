# Queue Manager

This is a library to automate the creation of queues and exchanges in RabbitMQ.

## Installation

The following dependency should exist in the `pom.xml` file:

```xml
<dependency>
    <groupId>br.com.jaoafonso</groupId>
    <artifactId>queue-manager</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

After adding it to `pom.xml` and updating the project (`mvn clean install`), queue creation will depend on the existence of properties in the `application.yml` file.

Pay attention, as `YAML` files require correct indentation!

The following conventions are used by default:

### Exchanges

By default, exchanges have the following properties:

- `durable` - true
- `autoDelete` - false

Example:

```yml
queue-manager:
  rabbitmq:
    fanouts:
      -
        name: some-exchange.fanout // The name of your Fanout exchange
        // durable: true - Default value, no need to specify 
        // auto-delete: false - Default value, no need to specify
    directs:
      -
        name: some-exchange.direct // The name of your Direct exchange
        // durable: true - Default value, no need to specify 
        // auto-delete: false - Default value, no need to specify
    topics:
      -
        name: some-exchange.topic // The name of your Topic exchange
        // durable: true - Default value, no need to specify 
        // auto-delete: false - Default value, no need to specify
```

### Queues

By default, queues have the following properties:

- `durable` - false
- `exclusive` - false
- `auto-delete` - false
- `dead-letter-pattern` - true*
- `dead-letter-durable` - true*
- `arguments` - empty

The `dead-letter-pattern` property indicates whether a dead-letter queue should be created for handling failed messages.
When enabled, this property implies the following:

- Automatic creation of an error queue, following the naming convention: `<original_queue_name>-failure.queue`
- The generated queue has the `durable` property enabled but can be disabled by setting `dead-letter-durable` to false.
- The following properties are added to the original queue:
    - `x-dead-letter-exchange: ''`
    - `x-dead-letter-routing-key: <generated_error_queue_name>`

#### Binding a Queue to an Exchange

When declaring a queue, it is necessary to specify the related exchange, as shown in the example below:

```yml
queue-manager:
  rabbitmq:
    fanouts:
      -
        name: test.fanout

    queues:
      -
        name: test.queue
        exchange: test.fanout
        durable: true
```

## Reference

[AMQP Protocol - Queues, Exchanges, etc.](https://www.rabbitmq.com/tutorials/amqp-concepts.html)