package com.example.beststore.controllers;


import com.example.beststore.models.Product;
import com.example.beststore.models.ProductDto;
import com.example.beststore.services.ProductsRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.Binding;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductControllers {

    @Autowired
    private ProductsRepository repository;

    @GetMapping({"", "/"})
    public String showProductList(Model model ){
        List<Product> products = repository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        model.addAttribute("products", products);
        return "products/index";
    }

    @GetMapping("/create")
    public String showCreatePage(Model model){
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        return "products/createProduct";
    }

    @PostMapping("/create")
    public String createProduct(
     @Valid @ModelAttribute ProductDto productDto,
     BindingResult result
     ){
        if(productDto.getImageFile().isEmpty()){
            result.addError(new FieldError("productDto", "imageFile", "Image file is requierd"));
        }

        if(result.hasErrors()){
            return "products/createProduct";
        }

        //save Image file
        MultipartFile image = productDto.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

         try {
            String uploadDir = "public/";
            Path uploadPath = Paths.get(uploadDir);

            if(!Files.exists(uploadPath)){
             Files.createDirectories(uploadPath);
            }

            try(InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                        StandardCopyOption.REPLACE_EXISTING);
            }

        }catch (Exception ex){
             System.out.println("Exception: " + ex.getMessage());
         }

         Product product = new Product();
         product.setName(productDto.getName());
         product.setBrand(productDto.getBrand());
         product.setCategory(productDto.getCategory());
         product.setPrice(productDto.getPrice());
         product.setDescription(productDto.getDescription());
         product.setCreatedAt(createdAt);
         product.setImageFileName(storageFileName);

         repository.save(product);

        return "redirect:/products";
    }
}
