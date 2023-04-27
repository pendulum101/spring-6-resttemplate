package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface BeerClient {
    public BeerDTO getBeerById(UUID beerId);

    Page<BeerDTO> listBeers();

    Page<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize);

    BeerDTO createBeer(BeerDTO beerDTO);

    BeerDTO updateBeer(BeerDTO savedDto);

    void deleteBeer(UUID beerId);
}
