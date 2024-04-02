package com.balarawool.vectordb;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class WeaviateImageSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeaviateImageSearchApplication.class, args);
    }

    @Bean
    WeaviateClient weaviateClient() {
        Config config = new Config("http", "localhost:8080");
        return new WeaviateClient(config);
    }

    @Bean
    ApplicationRunner initializeWeaviateDatabase(WeaviateClient weaviateClient) {
        return args -> {
            // First check if class is present. If it is not present, then create it and store data.
            // If you want to delete it then do this: weaviateClient.schema().classDeleter().withClassName("Meme").run();
            var memeClass = weaviateClient.schema().classGetter().withClassName("Meme").run();
            if (memeClass.getResult() == null) {
                // Create Class/Collection in Weaviate
                var clazz = WeaviateClass.builder()
                        .className("Meme")
                        .vectorizer("img2vec-neural")
                        .vectorIndexType("hnsw")
                        .moduleConfig(Map.of("img2vec-neural", Map.of("imageFields", List.of("image"))))
                        .properties(List.of(Property.builder().name("image").dataType(List.of("blob")).build(),
                                Property.builder().name("text").dataType(List.of("string")).build()))
                        .build();
                var res = weaviateClient.schema().classCreator().withClass(clazz).run();

                if (res.getError() == null) {
                    // Store image files and their vectors
                    // Files are present in resources directory with this structure:
                    // resources
                    //    └ data
                    //       ├ donald
                    //       ├ mickey
                    //       ├ minion
                    //       ├ olaf
                    //       ├ pooh
                    //       └ pumba
                    // Each of these subdirectories contain .jpg, .png etc. files.
                    try {
                        var sampleDir = ResourceUtils.getFile("classpath:data");
                        for (var subDir : sampleDir.listFiles()) {
                            for (var f : subDir.listFiles()) {
                                embedAndStore(weaviateClient, f);
                                System.out.println("File stored: " + f.getPath());
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
    }

    public void embedAndStore(WeaviateClient weaviateClient, File image) {
        try {
            byte[] fileContent = FileUtils.readFileToByteArray(image);
            String encodedString = Base64.getEncoder().encodeToString(fileContent);

            weaviateClient.data().creator()
                    .withClassName("Meme")
                    .withProperties(Map.of("image", encodedString, "text", image.getName()))
                    .run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
