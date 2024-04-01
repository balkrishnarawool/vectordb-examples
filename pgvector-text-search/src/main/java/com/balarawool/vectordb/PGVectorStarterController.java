package com.balarawool.vectordb;

import com.pgvector.PGvector;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.ollama.OllamaEmbeddingClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class PGVectorStarterController {
    @Autowired
    VectorStore vectorStore;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @GetMapping("/search")
    public String search(@RequestParam Map<String, String> params) {

        // Retrieve documents similar to a query
        List<Document> results = vectorStore.similaritySearch(SearchRequest.query(params.get("query")).withTopK(2));

        return results.toString();
    }

    // When using a vector as search parameter, make sure to use the same embedding-model that was originally used to store the data in the database.
    @GetMapping("/searchWithVector")
    public String searchWithVector(@RequestParam Map<String, String> params) {
        var ollamaApi = new OllamaApi();

        var embeddingClient = new OllamaEmbeddingClient(ollamaApi)
                .withDefaultOptions(OllamaOptions.create()
                        .withModel("llama2"));

        EmbeddingResponse embeddingResponse = embeddingClient
                .embedForResponse(List.of(params.get("query")));

        List<Double> vector = embeddingResponse.getResults().get(0).getOutput();

        Object[] neighborParams = new Object[] { new PGvector(vector) };
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM vector_store ORDER BY embedding <-> ? LIMIT 2", neighborParams);
//        var res = "";
//        for (Map row : rows) {
//            res += row.get("content");
//        }
//        return res;
        return rows.stream().map(r -> r.get("content")).toList().toString();
    }

}
