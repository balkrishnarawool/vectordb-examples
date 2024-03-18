package vectordb;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class PGVectorStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(PGVectorStarterApplication.class, args);
    }

//    @Bean
    ApplicationRunner applicationRunner(VectorStore vectorStore) {

        return args -> {
            List<Document> documents = List.of(
                    new Document("When you multiply a number with itself, you get square of that number.", Map.of("field", "arithmetic")),
                    new Document("Square is a geometrical shape with 4 sides where each side has same length.", Map.of("field", "geometry")),
                    new Document("Complex numbers have real and imaginary parts.", Map.of("field", "algebra")),
                    new Document("Each coin has 2 sides, heads and tails, which are opposite to each other."),
                    new Document("A coin toss gives a pretty random outcome."),
                    new Document("Random number generation is difficult to achieve.", Map.of("field", "algorithms"))
            );

            vectorStore.add(documents);
        };
    }
}
