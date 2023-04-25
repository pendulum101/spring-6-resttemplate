package guru.springframework.spring6resttemplate.client;

import static org.junit.jupiter.api.Assertions.*;

import guru.springframework.spring6resttemplate.model.BeerDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

@SpringBootTest
class BeerClientImplTest {
    @Autowired
    BeerClientImpl beerClient;

    @Test
    void testGetBeerById() {
        Page<BeerDTO> beerDTOS = beerClient.listBeers();
        BeerDTO beerDTO = beerDTOS.getContent().get(0);
        BeerDTO byId = beerClient.getBeerById(beerDTO.getId());
        assertNotNull(byId);
    }
    @Test
    void testListBeers() {
        beerClient.listBeers(null, null, null, null, null);
    }

    @Test
    void testListBeersAle() {
        beerClient.listBeers("ALE", null, null, null,null);
    }
}
