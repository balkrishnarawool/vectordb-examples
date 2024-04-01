package com.balarawool.vectordb;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import io.weaviate.client.v1.graphql.query.fields.Field;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@RestController
public class WeaviateImageSearchController {
    WeaviateClient weaviateClient;

    WeaviateImageSearchController(WeaviateClient weaviateClient) {
        this.weaviateClient = weaviateClient;
    }

    @PostMapping("/search")
    public ResponseEntity<Result> search(@RequestParam("file") MultipartFile file) {
        try {
            Result<GraphQLResponse> r = weaviateClient.graphQL().get()
                    .withClassName("Meme")
                    .withFields(Field.builder().name("image").build())
                    .withNearImage(NearImageArgument.builder().image(Base64.getEncoder().encodeToString(file.getBytes())).build())
                    .withLimit(6)
                    .run();

            return ResponseEntity.ok(r);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
