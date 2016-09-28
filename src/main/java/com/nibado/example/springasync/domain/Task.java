package com.nibado.example.springasync.domain;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

@Slf4j
public class Task {
    private final AtomicInteger counter;
    private final DeferredResult<ResponseEntity<AggregateResponse>> result;
    private final List<String> urls;
    private final List<ApiResponse> responses;
    private long startTime;

    public Task(final DeferredResult<ResponseEntity<AggregateResponse>> result, final List<String> urls) {
        this.counter = new AtomicInteger(urls.size());
        this.urls = urls;
        this.result = result;
        this.responses = urls.stream().map(s -> new ApiResponse()).collect(toList());
    }

    public List<String> getUrls() {
        return unmodifiableList(urls);
    }

    public void fail(int index, long time, Request request, IOException e) {
        responses.get(index).setStatus(502);
        responses.get(index).setBody("Failed: " + e.getMessage());
        responses.get(index).setDuration((int)(System.currentTimeMillis() - time));

        checkDone();
    }

    public void success(int index, long time, Response response) throws IOException {
        responses.get(index).setStatus(response.code());
        responses.get(index).setBody(response.body().string());
        responses.get(index).setDuration((int)(System.currentTimeMillis() - time));

        checkDone();
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

    private void checkDone() {
        synchronized (counter) {
            if (counter.decrementAndGet() == 0) {
                AggregateResponse response = new AggregateResponse(responses, (int)(System.currentTimeMillis() - startTime));
                result.setResult(ResponseEntity.ok(response));
                log.info("Finished task in {} ms", response.getDuration());
            }
        }
    }
}
