package ru.rtuitlab.notify.redis;

public interface RedisPublisher {
    public void unsubscribe();
    public void connect(String host, Integer port, String password);
    public void publish(String channel, String message);
}
