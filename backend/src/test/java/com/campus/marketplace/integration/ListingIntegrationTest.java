package com.campus.marketplace.integration;

import com.campus.marketplace.dto.ListingDTO;
import com.campus.marketplace.entity.Listing;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ListingIntegrationTest extends AbstractIntegrationTest {

    @Test
    void sellerCanCreateAndRetrieveListing() throws Exception {
        AuthResult seller = registerUserThroughApi(
                "Seller One",
                randomEmail(),
                "SellerPass123!"
        );

        var category = createCategoryFixture("IntegrationCategory");

        ListingDTO listingRequest = new ListingDTO();
        listingRequest.setSellerId(seller.getUserId());
        listingRequest.setTitle("Integration Laptop");
        listingRequest.setDescription("Lightly used laptop for integration tests");
        listingRequest.setPrice(new BigDecimal("1299.99"));
        listingRequest.setCategoryId(category.getId());
        listingRequest.setCondition(Listing.ItemCondition.LIKE_NEW);
        listingRequest.setImages("[\"https://cdn.example.com/listings/laptop.jpg\"]");
        listingRequest.setStatus(Listing.ListingStatus.ACTIVE);

        String createResponse = mockMvc.perform(post("/api/listings")
                        .header("Authorization", authHeader(seller.getToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listingRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Laptop"))
                .andExpect(jsonPath("$.sellerId").value(seller.getUserId()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String listingId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(get("/api/listings/" + listingId)
                        .header("Authorization", authHeader(seller.getToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listingId))
                .andExpect(jsonPath("$.categoryId").value(category.getId()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(get("/api/listings/seller/" + seller.getUserId())
                        .header("Authorization", authHeader(seller.getToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Integration Laptop"))
                .andExpect(jsonPath("$[0].sellerId").value(seller.getUserId()));
    }
}
