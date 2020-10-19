package ru.ndg.cloud.storage.v3.client.handlers;

public interface Callback<T> {

    void callback(T args);
}
