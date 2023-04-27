package guru.springframework.spring6resttemplate.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withAccepted;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6resttemplate.config.RestTemplateBuilderConfig;
import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestClientTest(BeerClientImpl.class)
@Import(RestTemplateBuilderConfig.class)
public class BeerClientMockTest {

    static final String URL = "http://localhost:8080";

    @Autowired
    RestTemplateBuilder restTemplateBuilderConfigured;

    BeerClient beerClient;
    MockRestServiceServer server;

    @Autowired
    ObjectMapper objectMapper;
    @Mock
    RestTemplateBuilder mockRestTemplateBuilder = new RestTemplateBuilder(new MockServerRestTemplateCustomizer());

    BeerDTO beerDTO;
    String dtoJson;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        RestTemplate restTemplate = restTemplateBuilderConfigured.build();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        when(mockRestTemplateBuilder.build()).thenReturn(restTemplate);
        beerClient = new BeerClientImpl(mockRestTemplateBuilder);

        beerDTO = getBeerDto();
        dtoJson = objectMapper.writeValueAsString(beerDTO);

    }

    @Test
    void testListBeers() throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(getPage());

        server.expect(method(HttpMethod.GET))
            .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_PATH))
            .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        Page<BeerDTO> dtos = beerClient.listBeers();
        assertThat(dtos.getContent().size()).isGreaterThan(0);
    }

    @Test
    void testListBeersWithQueryParam() throws JsonProcessingException {
        String response = objectMapper.writeValueAsString(getPage());

        URI uri = UriComponentsBuilder.fromHttpUrl(URL + BeerClientImpl.GET_BEER_PATH)
            .queryParam("beerName", "ALE")
            .build().toUri();

        server.expect(method(HttpMethod.GET))
            .andExpect(requestTo(uri))
            .andExpect(queryParam("beerName", "ALE"))
            .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        Page<BeerDTO> responsePage = beerClient
            .listBeers("ALE", null, null, null, null);

        assertThat(responsePage.getContent().size()).isEqualTo(1);
    }

    @Test
    void testDeleteBeerNotFound() {
        server.expect(method(HttpMethod.DELETE))
            .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, beerDTO.getId()))
            .andRespond(withResourceNotFound());

        assertThrows(HttpClientErrorException.class, () -> {
            beerClient.deleteBeer(beerDTO.getId());
        });

        server.verify();
    }

    @Test
    void testDeleteBeer() {
        server.expect(method(HttpMethod.DELETE))
            .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, beerDTO.getId()))
            .andRespond(withNoContent());

        beerClient.deleteBeer(beerDTO.getId());

        server.verify();
    }

    @Test
    void testUpdateBeer() {
        server.expect(method(HttpMethod.PUT))
            .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH,
                beerDTO.getId()))
            .andRespond(withNoContent());

        mockGetOperation();

        BeerDTO responseDto = beerClient.updateBeer(beerDTO);
        assertThat(responseDto.getId()).isEqualTo(beerDTO.getId());
    }

    @Test
    void testCreateBeer() {
        URI uri = UriComponentsBuilder.fromPath(BeerClientImpl.GET_BEER_BY_ID_PATH)
            .build(beerDTO.getId());

        server.expect(method(HttpMethod.POST))
            .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_PATH))
            .andRespond(withAccepted().location(uri));

        mockGetOperation();

        BeerDTO responseDto = beerClient.createBeer(beerDTO);
        assertThat(responseDto.getId()).isEqualTo(beerDTO.getId());
    }

    @Test
    void testGetBeerById() {
        mockGetOperation();

        BeerDTO dtos = beerClient.getBeerById(beerDTO.getId());
        assertThat(dtos.getId()).isEqualTo(beerDTO.getId());
    }

    private void mockGetOperation() {
        server.expect(method(HttpMethod.GET))
            .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_PATH + "/" + beerDTO.getId()))
            .andRespond(withSuccess(dtoJson, MediaType.APPLICATION_JSON));
//        server.expect(method(HttpMethod.GET))
//            .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, beerDTO.getId()))
//            .andRespond(withSuccess(dtoJson, MediaType.APPLICATION_JSON));
//    testGetBeerById has an alternative way of setting the URL the one above is cleaner,
//    it uses the URI template to set the value.
    }

    BeerDTO getBeerDto() {
        return BeerDTO.builder()
            .id(UUID.randomUUID())
            .price(new BigDecimal("10.99"))
            .beerName("Le Guinness")
            .beerStyle(BeerStyle.STOUT)
            .quantityOnHand(999)
            .upc("12345")
            .build();
    }

    BeerDTOPageImpl getPage() {
        return new BeerDTOPageImpl(Arrays.asList(getBeerDto()), 1, 25, 1);
    }
}