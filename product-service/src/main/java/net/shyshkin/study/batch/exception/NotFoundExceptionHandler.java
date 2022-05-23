package net.shyshkin.study.batch.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

@Produces
@Singleton
@Requires(classes = {EntityNotFoundException.class, ExceptionHandler.class})
public class NotFoundExceptionHandler implements ExceptionHandler<EntityNotFoundException, HttpResponse> {

    @Override
    public HttpResponse handle(HttpRequest request, EntityNotFoundException exception) {
        return HttpResponse.notFound();
    }
}
