package com.herokuapp.psxapi.service;

import com.herokuapp.psxapi.config.PseiProps;
import com.herokuapp.psxapi.config.PsxConstants;
import com.herokuapp.psxapi.model.StocksSimple;
import net.spy.memcached.MemcachedClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class StockServiceImplTest {

    @Mock
    RestTemplate restTemplate;

    @Mock
    MemcachedClient memcachedClient;

    @Mock
    PseiProps pseiProps;

    @InjectMocks
    StockServiceImpl stockServiceImpl;


    @Test
    public void given_memcached_when_cacheStocksHasValue_then_expectAListOfStockSimple() {
        when(memcachedClient.get(PsxConstants.CACHE_STOCKS)).thenReturn(createStocks());
        assertEquals(createStocks(), stockServiceImpl.getAllStocks());
    }


    @Test
    public void given_memcachedAndRestemplate_when_apiCalledAndHasReturn_then_cachedResponseAndExpectAList() {
        when(memcachedClient.get(PsxConstants.CACHE_STOCKS)).thenReturn(null);
        when(restTemplate.exchange(pseiProps.getUrl(), HttpMethod.POST, createRequest(),
                new ParameterizedTypeReference<List<StocksSimple>>() {
                })).thenReturn(new ResponseEntity<>(createStocks(), HttpStatus.OK));

        assertEquals(createStocks(), stockServiceImpl.getAllStocks());
    }


    @Test
    public void given_memcachedAndRestemplate_when_apiCalledAndNotOK_then_expectEmptyList() {
        when(memcachedClient.get(PsxConstants.CACHE_STOCKS)).thenReturn(null);
        when(restTemplate.exchange(pseiProps.getUrl(), HttpMethod.POST, createRequest(),
                new ParameterizedTypeReference<List<StocksSimple>>() {
                })).thenReturn(new ResponseEntity<>(createStocks(), HttpStatus.BAD_GATEWAY));

        assertEquals(Collections.EMPTY_LIST, stockServiceImpl.getAllStocks());
    }


    private List<StocksSimple> createStocks() {
        StocksSimple bdo = new StocksSimple();
        bdo.setName("Banco de Oro");
        bdo.setPercentageChange("-2.0");
        bdo.setPrice("127.00");
        bdo.setSymbol("BDO");
        bdo.setVolume("10000");

        StocksSimple bpi = new StocksSimple();
        bpi.setName("Bank of the Ph");
        bpi.setPercentageChange("-2.0");
        bpi.setPrice("127.00");
        bpi.setSymbol("BPI");
        bpi.setVolume("20000");

        List<StocksSimple> listOfStocks = new ArrayList<>();
        listOfStocks.add(bdo);
        listOfStocks.add(bpi);
        return listOfStocks;
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }

    private HttpEntity<MultiValueMap<String, String>> createRequest() {
        MultiValueMap<String, String> param = createMultiMap(PsxConstants.PSEI_TICKER_METHOD);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(param, createHttpHeaders());
        return request;
    }

    private MultiValueMap<String, String> createMultiMap(String method) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("method", method);
        map.add("ajax", "true");
        return map;
    }
}
