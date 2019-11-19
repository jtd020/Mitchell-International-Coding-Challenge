package com.joseph.developer.application;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VehicleControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String getRootUrl() {
        return "http://localhost:" + port;
    }

    @Before
    public void beforeAll() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        String[] makes = {"Ferrari", "Kia", "Chevrolet", "Lexus", "BMV",
                "Honda", "Honda", "Toyota", "Toyota", "Ford"};
        String[] models = {"LaFerrari", "Sorrento", "Impala", "RX", "i8",
                "Odyssey", "Accord", "Sienna", "Corolla", "Fiesta"};
        int[] years = {2015, 2015, 2016, 2016, 2016, 2016, 2018, 2018, 2019, 2019, 2019};
        for (int i = 0; i < makes.length; i++) {
            Vehicle vehicle = new Vehicle();
            vehicle.setYear(years[i]);
            vehicle.setModel(models[i]);
            vehicle.setMake(makes[i]);
            restTemplate.postForEntity(getRootUrl() + "/vehicles", vehicle, String.class);
        }
    }

    @Test
    public void checkAllVehiclesInDatabase() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);
        String[] makes = {"Ferrari", "Kia", "Chevrolet", "Lexus", "BMV",
                "Honda", "Honda", "Toyota", "Toyota", "Ford"};
        String[] models = {"LaFerrari", "Sorrento", "Impala", "RXI", "i8",
                "Odyssey", "Accord", "Sienna", "Corolla", "Fiesta"};
        int[] years = {2015, 2015, 2016, 2016, 2016, 2016, 2018, 2018, 2019, 2019, 2019};
        ResponseEntity<List<Vehicle>> response = restTemplate.exchange(getRootUrl() + "/vehicles",
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<Vehicle>>() {});
        for(int i = 0; i < makes.length; i++) {
            Assert.assertEquals(response.getBody().get(i).getYear(), years[i]);
            Assert.assertEquals(response.getBody().get(i).getModel(), models[i]);
            Assert.assertEquals(response.getBody().get(i).getMake(), makes[i]);
        }
        Assert.assertNotNull(response.getBody());
    }

    @Test
    public void testGetAllVehicles() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(getRootUrl() + "/vehicles",
                HttpMethod.GET, entity, String.class);
        Assert.assertNotNull(response.getBody());
    }

    @Test
    public void testDeleteVehicleById()  {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        Vehicle vehicle = new Vehicle();
        vehicle.setYear(2001);
        vehicle.setModel("Corolla");
        vehicle.setMake("Toyota");
        ResponseEntity postResponse = restTemplate.postForEntity(getRootUrl() + "/vehicles",
                vehicle, Vehicle.class);
        ResponseEntity<String> response = restTemplate.exchange(getRootUrl() + "/vehicles/11",
                HttpMethod.DELETE, entity, String.class);
        ResponseEntity<List<Vehicle>> list = restTemplate.exchange(getRootUrl() +
                        "/vehicles?yearBegin=2001&yearEnd=2001&make=Toyota&model=Corolla",
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<Vehicle>>() {});
        Assert.assertEquals(0, list.getBody().size());
    }

    @Test
    public void testGetVehicleById() {
        Vehicle vehicle = restTemplate.getForObject(getRootUrl() + "/vehicles/1", Vehicle.class);
        Assert.assertEquals(2015, vehicle.getYear());
        Assert.assertEquals("LaFerrari", vehicle.getModel());
        Assert.assertEquals( "Ferrari", vehicle.getMake());
    }

    @Test
    public void testUpdateVehicle() {
        HttpHeaders headers = new HttpHeaders();
        Vehicle vehicle = restTemplate.getForObject(getRootUrl() + "/vehicles/4", Vehicle.class);
        vehicle.setModel("RXI");
        System.out.println(vehicle.getId());
        HttpEntity<Vehicle> entity = new HttpEntity<Vehicle>(vehicle, headers);
        ResponseEntity<Vehicle> updatedVehicle = restTemplate.exchange(getRootUrl() + "/vehicles",
                HttpMethod.PUT, entity, Vehicle.class);
        Assert.assertEquals(vehicle.getId(), updatedVehicle.getBody().getId());
        Assert.assertEquals(2016, updatedVehicle.getBody().getYear());
        Assert.assertEquals("RXI", updatedVehicle.getBody().getModel());
        Assert.assertEquals( "Lexus", updatedVehicle.getBody().getMake());
    }


    @Test
    public void testCreateVehicle() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        Vehicle vehicle = new Vehicle();
        vehicle.setYear(2010);
        vehicle.setModel("Civic");
        vehicle.setMake("Honda");
        ResponseEntity<Vehicle> vehicleCreated = restTemplate.postForEntity(getRootUrl() + "/vehicles", vehicle,
                Vehicle.class);
        ResponseEntity<Vehicle> vehicleGet = restTemplate.exchange(getRootUrl() + "/vehicles/" +
                        vehicleCreated.getBody().getId(), HttpMethod.GET, entity, Vehicle.class);
        Assert.assertEquals(2010, vehicleGet.getBody().getYear());
        Assert.assertEquals("Honda", vehicleGet.getBody().getMake());
        Assert.assertEquals("Civic", vehicleGet.getBody().getModel());
    }

    @Test
    public void testFilterVehicleByYear() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);
        String[] makes = {"Ferrari", "Kia", "Chevrolet", "Lexus", "BMV",
                "Honda", "Honda", "Toyota", "Toyota", "Ford"};
        String[] models = {"LaFerrari", "Sorrento", "Impala", "RX", "i8",
                "Odyssey", "Accord", "Sienna", "Corolla", "Fiesta"};
        int[] years = {2015, 2015, 2016, 2016, 2016, 2016, 2018, 2018, 2019, 2019, 2019};
        ResponseEntity<List<Vehicle>> response = restTemplate.exchange(getRootUrl() +
                        "/vehicles?yearBegin=2015&yearEnd=2018",
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<Vehicle>>() {});

        for(int i = 0; i < 8; i++) {
            Assert.assertEquals(years[i], response.getBody().get(i).getYear());
            Assert.assertEquals(models[i], response.getBody().get(i).getModel());
            Assert.assertEquals(makes[i], response.getBody().get(i).getMake());
        }
    }

    @Test
    public void testFilterVehicleByMake() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);
        String[] makes = {"Ferrari", "Kia", "Chevrolet", "Lexus", "BMV",
                "Honda", "Honda", "Toyota", "Toyota", "Ford"};
        String[] models = {"LaFerrari", "Sorrento", "Impala", "RX", "i8",
                "Odyssey", "Accord", "Sienna", "Corolla", "Fiesta"};
        int[] years = {2015, 2015, 2016, 2016, 2016, 2016, 2018, 2018, 2019, 2019, 2019};
        ResponseEntity<List<Vehicle>> response = restTemplate.exchange(getRootUrl() +
                        "/vehicles?make=Ferrari,Kia,Chevrolet",
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<Vehicle>>() {});
        for(int i = 0; i < 3; i++) {
            Assert.assertEquals(years[i], response.getBody().get(i).getYear());
            Assert.assertEquals(models[i], response.getBody().get(i).getModel());
            Assert.assertEquals(makes[i], response.getBody().get(i).getMake());
        }
    }

    @Test
    public void testFilterVehicleByModel() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);
        String[] makes = {"Ferrari", "Kia", "Chevrolet", "Lexus", "BMV",
                "Honda", "Honda", "Toyota", "Toyota", "Ford"};
        String[] models = {"LaFerrari", "Sorrento", "Impala", "RX", "i8",
                "Odyssey", "Accord", "Sienna", "Corolla", "Fiesta"};
        int[] years = {2015, 2015, 2016, 2016, 2016, 2016, 2018, 2018, 2019, 2019, 2019};
        ResponseEntity<List<Vehicle>> response = restTemplate.exchange(getRootUrl() +
                        "/vehicles?model=LaFerrari,Sorrento,Impala",
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<Vehicle>>() {});
        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(years[i], response.getBody().get(i).getYear());
            Assert.assertEquals(models[i], response.getBody().get(i).getModel());
            Assert.assertEquals(makes[i], response.getBody().get(i).getMake());
        }
    }

    @Test
    public void testFilterVehicleByYearMakeModel() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);
        ResponseEntity<List<Vehicle>> response = restTemplate.exchange(getRootUrl() +
                        "/vehicles?model=Accord&yearBegin=2015&yearEnd=2019&make=Honda",
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<Vehicle>>() {});
        for(int i = 0; i < response.getBody().size(); i++) {
            Assert.assertEquals(2018, response.getBody().get(i).getYear());
            Assert.assertEquals("Accord", response.getBody().get(i).getModel());
            Assert.assertEquals("Honda", response.getBody().get(i).getMake());
        }
    }

    @Test
    public void testFilterVehicleByMakeModel() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);
        ResponseEntity<List<Vehicle>> response = restTemplate.exchange(getRootUrl() +
                        "/vehicles?model=Sienna&make=Toyota",
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<Vehicle>>() {});
        for(int i = 0; i < response.getBody().size(); i++) {
            Assert.assertEquals(2018, response.getBody().get(i).getYear());
            Assert.assertEquals("Sienna", response.getBody().get(i).getModel());
            Assert.assertEquals("Toyota", response.getBody().get(i).getMake());
        }
    }

    @Test
    public void testFilterVehicleByYearMake() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);
        ResponseEntity<List<Vehicle>> response = restTemplate.exchange(getRootUrl() +
                        "/vehicles?yearBegin=2016&yearEnd=2016&make=Kia",
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<Vehicle>>() {});
        for(int i = 0; i < response.getBody().size(); i++) {
            Assert.assertEquals(2016, response.getBody().get(i).getYear());
            Assert.assertEquals("Sorrento", response.getBody().get(i).getModel());
            Assert.assertEquals("Kia", response.getBody().get(i).getMake());
        }
    }

    @Test
    public void testFilterVehicleByYearModel() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);
        ResponseEntity<List<Vehicle>> response = restTemplate.exchange(getRootUrl() +
                        "/vehicles?yearBegin=2018&yearEnd=2018&model=Odyssey",
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<Vehicle>>() {});

        for(int i = 0; i < response.getBody().size(); i++) {
            Assert.assertEquals(2018, response.getBody().get(i).getYear());
            Assert.assertEquals("Odyssey", response.getBody().get(i).getModel());
            Assert.assertEquals("Honda", response.getBody().get(i).getMake());
        }
    }

}
