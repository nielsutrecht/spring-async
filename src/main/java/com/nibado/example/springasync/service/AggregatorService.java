package com.nibado.example.springasync.service;

import com.nibado.example.springasync.domain.Task;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class AggregatorService {
    private OkHttpClient client = new OkHttpClient();

    public void execute(final Task task) {
        log.info("Started task with {} urls", task.getUrls().size());
        task.start();
        for(int i = 0; i < task.getUrls().size(); i++) {
            final int index = i;
            final long time = System.currentTimeMillis();
            String url = task.getUrls().get(i);
            Request req = new Request.Builder().get().url(url).build();

            client.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    task.fail(index, time, request, e);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    task.success(index, time, response);
                }
            });
        }
    }
}
