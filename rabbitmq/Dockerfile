FROM rabbitmq:4.1.0-management

COPY rabbitmq/rabbitmq_delayed_message_exchange-4.1.0.ez /opt/rabbitmq/plugins/rabbitmq_delayed_message_exchange.ez

RUN rabbitmq-plugins enable rabbitmq_delayed_message_exchange
