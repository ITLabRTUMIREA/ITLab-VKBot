package ru.rtuitlab.notify.redis;

public interface RedisListener {

    public void unsubscribe();
    public void listenEvents();

}
