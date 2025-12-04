package com.campus.marketplace.integration;

import com.campus.marketplace.dto.ListingDTO;
import com.campus.marketplace.entity.Listing;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionIntegrationTest extends AbstractIntegrationTest {

    @Test
    void buyerRequestAndSellerCompletesTransaction() throws Exception {
        AuthResult seller = registerUserThroughApi("Seller Tx", randomEmail(), "SellerPass321!");
        AuthResult buyer = registerUserThroughApi("Buyer Tx", randomEmail(), "BuyerPass321!");
        var category = createCategoryFixture("TxCategory");

        ListingDTO listingRequest = new ListingDTO();
        listingRequest.setSellerId(seller.getUserId());
        listingRequest.setTitle("Integration Textbook");
        listingRequest.setDescription("CMPE 202 textbook, gently used.");
        listingRequest.setPrice(new BigDecimal("75.00"));
        listingRequest.setCategoryId(category.getId());
        listingRequest.setCondition(Listing.ItemCondition.GOOD);
        listingRequest.setImages("[]");
        listingRequest.setStatus(Listing.ListingStatus.ACTIVE);

        String listingResponse = mockMvc.perform(post("/api/listings")
                        .header("Authorization", authHeader(seller.getToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listingRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String listingId = objectMapper.readTree(listingResponse).get("id").asText();

        String transactionResponse = mockMvc.perform(post("/api/transactions/request-to-buy")
                        .header("Authorization", authHeader(buyer.getToken()))
                        .param("listingId", listingId)
                        .param("buyerId", buyer.getUserId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String transactionId = objectMapper.readTree(transactionResponse).get("id").asText();

        mockMvc.perform(patch("/api/transactions/" + transactionId + "/mark-sold")
                        .header("Authorization", authHeader(seller.getToken()))
                        .param("sellerId", seller.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(get("/api/listings/" + listingId)
                        .header("Authorization", authHeader(seller.getToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SOLD"));
    }
}
