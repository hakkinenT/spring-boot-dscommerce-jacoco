package com.devsuperior.dscommerce.services;

import com.devsuperior.dscommerce.dto.ProductDTO;
import com.devsuperior.dscommerce.dto.ProductMinDTO;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.repositories.ProductRepository;
import com.devsuperior.dscommerce.services.exceptions.DatabaseException;
import com.devsuperior.dscommerce.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscommerce.tests.ProductFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {
    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    private Long existingId;

    private Long nonExisting;

    private Long dependentId;

    private Product product;

    private String productName;

    private PageImpl<Product> page;

    private ProductDTO productDTO;

    @BeforeEach
    void setUp() throws Exception{
        existingId = 1L;
        nonExisting = 2000L;
        dependentId = 2L;

        productName = "Playstation 5";

        product = ProductFactory.createProduct(productName);
        productDTO = new ProductDTO(product);
        page = new PageImpl<>(List.of(product));

        Mockito.when(productRepository.findById(existingId)).thenReturn(Optional.of(product));
        Mockito.when(productRepository.findById(nonExisting)).thenReturn(Optional.empty());

        Mockito.when(productRepository.searchByName(any(), (Pageable) any())).thenReturn(page);

        Mockito.when(productRepository.save(any())).thenReturn(product);

        Mockito.when(productRepository.getReferenceById(existingId)).thenReturn(product);
        Mockito.when(productRepository.getReferenceById(nonExisting)).thenThrow(EntityNotFoundException.class);

        Mockito.when(productRepository.existsById(existingId)).thenReturn(true);
        Mockito.when(productRepository.existsById(dependentId)).thenReturn(true);
        Mockito.when(productRepository.existsById(nonExisting)).thenReturn(false);

        Mockito.doNothing().when(productRepository).deleteById(existingId);
        Mockito.doThrow(DataIntegrityViolationException.class).when(productRepository).deleteById(dependentId);

    }

    @Test
    public void findByIdShouldReturnProductDTOWhenIdExists(){
        ProductDTO result = productService.findById(existingId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), existingId);
        Assertions.assertEquals(result.getName(), product.getName());
    }

    @Test
    public void findByIdShouldReturnResourceNotFoundExceptionWhenIdDoesNotExist(){
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            ProductDTO result = productService.findById(nonExisting);
        });
    }

    @Test
    public void findAllShouldReturnPagedProductMinDTO(){
        Pageable pageable = PageRequest.of(0,12);
        Page<ProductMinDTO> result = productService.findAll(productName, pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getSize(), 1);
        Assertions.assertEquals(result.iterator().next().getName(), productName);
    }

    @Test
    public void insertShouldReturnProductDTO(){
        ProductDTO result = productService.insert(productDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), product.getId());
    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExists(){
        ProductDTO result = productService.update(existingId, productDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), existingId);
        Assertions.assertEquals(result.getName(), productDTO.getName());
    }

    @Test
    public void updateShouldReturnResourceNotFoundExceptionWhenIdDoesNotExist(){
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            productService.update(nonExisting, productDTO);
        });
    }

    @Test
    public void deleteShouldDoNothingWhenId(){
        Assertions.assertDoesNotThrow(() -> {
            productService.delete(existingId);
        });
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist(){
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            productService.delete(nonExisting);
        });
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenDependentId(){
        Assertions.assertThrows(DatabaseException.class, () -> {
            productService.delete(dependentId);
        });
    }
}
