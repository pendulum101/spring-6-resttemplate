package guru.springframework.spring6resttemplate.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest
class BeerClientImplTest {

    @Autowired
    BeerClientImpl beerClient;

    @Test
    void testDeleteBeer() {
        BeerDTO newDto = BeerDTO.builder()
            .price(new BigDecimal("10.99"))
            .beerName("Mango Jims")
            .beerStyle(BeerStyle.IPA)
            .quantityOnHand(100)
            .upc("987654")
            .build();

        BeerDTO beerDto = beerClient.createBeer(newDto);

        beerClient.deleteBeer(beerDto.getId());

        assertThrows(HttpClientErrorException.class, () -> {
            beerClient.getBeerById(beerDto.getId());
        });
    }

    @Test
    void testUpdateBeer() {
        BeerDTO newDTO = BeerDTO.builder()
            .price(new BigDecimal("10.99"))
            .beerName("Mango Jims")
            .beerStyle(BeerStyle.IPA)
            .quantityOnHand(100)
            .upc("987654")
            .build();

        BeerDTO savedDto = beerClient.createBeer(newDTO);

        final String newName = "Guinness";
        savedDto.setBeerName(newName);
        BeerDTO updatedBeer = beerClient.updateBeer(savedDto);

        assertEquals(newName, updatedBeer.getBeerName());
    }

    @Test
    void testCreateBeer() {
        BeerDTO newDTO = BeerDTO.builder()
            .price(new BigDecimal("10.99"))
            .beerName("Mango Jims")
            .beerStyle(BeerStyle.IPA)
            .quantityOnHand(100)
            .upc("987654")
            .build();

        BeerDTO savedDto = beerClient.createBeer(newDTO);
        assertNotNull(savedDto);
    }

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
        beerClient.listBeers("ALE", null, null, null, null);
    }
}