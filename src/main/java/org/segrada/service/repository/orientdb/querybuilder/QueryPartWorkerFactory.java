package org.segrada.service.repository.orientdb.querybuilder;

import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;

public class QueryPartWorkerFactory {
    private static final Logger logger = LoggerFactory.getLogger(QueryPartWorkerFactory.class);

    public @Nullable QueryPartWorker produceQueryPartWorker(String name) {
        if (name == null) return null;

        try { // create class for this entity
            Class clazz = Class.forName("org.segrada.service.repository.orientdb.querybuilder.QueryPartWorker" + Character.toUpperCase(name.charAt(0)) + name.substring(1));

            // instantiate new class
            Constructor constructor = clazz.getConstructor();
            return (QueryPartWorker) constructor.newInstance();
        } catch (Exception e) {
            logger.error("Error while producing query part worker from " + name, e);
            return null;
        }
    }
}
