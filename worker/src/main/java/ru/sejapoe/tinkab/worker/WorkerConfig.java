package ru.sejapoe.tinkab.worker;

public interface WorkerConfig {

    Class<?> paramsClass();

    String filterName();
}
