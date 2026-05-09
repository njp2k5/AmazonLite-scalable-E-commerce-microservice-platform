package com.amazonlite.product.config;

import com.amazonlite.product.model.Product;
import com.amazonlite.product.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductDataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    public ProductDataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        List<Product> samples = List.of(
                new Product("Wireless Earbuds","High-fidelity true wireless earbuds with noise cancellation",59.99,150,"Electronics","https://example.com/images/earbuds.jpg"),
                new Product("Smart Watch","Fitness tracking smart watch with heart rate monitor",129.99,80,"Electronics","https://example.com/images/smartwatch.jpg"),
                new Product("Bluetooth Speaker","Portable Bluetooth speaker with deep bass",39.99,200,"Electronics","https://example.com/images/speaker.jpg"),
                new Product("Running Shoes","Lightweight running shoes for daily training",89.99,120,"Footwear","https://example.com/images/shoes.jpg"),
                new Product("Denim Jacket","Classic denim jacket with a modern fit",79.99,60,"Apparel","https://example.com/images/jacket.jpg"),
                new Product("Coffee Maker","Programmable drip coffee maker with 12-cup capacity",49.99,90,"Home","https://example.com/images/coffeemaker.jpg"),
                new Product("Electric Kettle","Fast-boil electric kettle with auto shutoff",29.99,140,"Home","https://example.com/images/kettle.jpg"),
                new Product("Yoga Mat","Non-slip yoga mat for all types of workouts",24.99,300,"Sports","https://example.com/images/yogamat.jpg"),
                new Product("Gaming Mouse","Ergonomic gaming mouse with programmable buttons",39.99,75,"Electronics","https://example.com/images/mouse.jpg"),
                new Product("Mechanical Keyboard","RGB mechanical keyboard with tactile switches",89.99,50,"Electronics","https://example.com/images/keyboard.jpg"),
                new Product("Backpack","Durable backpack with laptop compartment",59.99,110,"Accessories","https://example.com/images/backpack.jpg"),
                new Product("Sunglasses","Polarized sunglasses with UV protection",19.99,220,"Accessories","https://example.com/images/sunglasses.jpg"),
                new Product("Water Bottle","Insulated stainless steel water bottle 1L",22.99,180,"Home","https://example.com/images/bottle.jpg"),
                new Product("Wireless Charger","Qi-certified fast wireless charger pad",29.99,130,"Electronics","https://example.com/images/charger.jpg"),
                new Product("Laptop Stand","Adjustable laptop stand for ergonomic setup",34.99,95,"Office","https://example.com/images/stand.jpg"),
                new Product("Noise Cancelling Headphones","Over-ear headphones with ANC",149.99,40,"Electronics","https://example.com/images/headphones.jpg"),
                new Product("Smart Light Bulb","Wi-Fi enabled smart LED bulb, color-changing",14.99,250,"Home","https://example.com/images/bulb.jpg"),
                new Product("Action Camera","4K action camera with waterproof casing",99.99,45,"Electronics","https://example.com/images/camera.jpg"),
                new Product("Electric Toothbrush","Rechargeable electric toothbrush with timer",39.99,160,"Health","https://example.com/images/toothbrush.jpg"),
                new Product("Fitness Band","Activity tracker with sleep monitoring",49.99,140,"Electronics","https://example.com/images/fitnessband.jpg"),
                new Product("Cookware Set","10-piece non-stick cookware set",129.99,30,"Home","https://example.com/images/cookware.jpg"),
                new Product("Office Chair","Ergonomic office chair with lumbar support",199.99,25,"Office","https://example.com/images/chair.jpg"),
                new Product("Electric Scooter","Foldable electric scooter with 20km range",299.99,15,"Outdoors","https://example.com/images/scooter.jpg"),
                new Product("Portable SSD","1TB portable SSD with USB-C",109.99,70,"Electronics","https://example.com/images/ssd.jpg"),
                new Product("Desk Lamp","Adjustable LED desk lamp with USB port",24.99,140,"Office","https://example.com/images/desklamp.jpg"),
                new Product("Wireless Earbuds Pro","Premium true wireless earbuds with long battery",199.99,35,"Electronics","https://example.com/images/earbudspro.jpg"),
                new Product("Gaming Chair","Comfortable gaming chair with recline",149.99,20,"Furniture","https://example.com/images/gamingchair.jpg"),
                new Product("Kitchen Knife Set","High-carbon stainless steel 8-piece knife set",79.99,55,"Home","https://example.com/images/knives.jpg"),
                new Product("Smart Thermostat","Wi-Fi thermostat with energy-saving features",129.99,40,"Home","https://example.com/images/thermostat.jpg"),
                new Product("Robot Vacuum","Self-charging robot vacuum with smart mapping",249.99,22,"Home","https://example.com/images/robot-vacuum.jpg")
        );

        for (Product product : samples) {
            productRepository.findByName(product.getName()).orElseGet(() -> productRepository.save(product));
        }
    }
}
