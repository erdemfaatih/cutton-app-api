package com.cuttonproduct.cutton;

import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;


    @PostMapping("/add")
    public Sale addSale(@RequestBody Sale sale) {
        Product product = productRepository.findById(sale.getProductId())
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı!"));

        if (product.getStockQuantity() < sale.getQuantitySold()) {
            throw new RuntimeException("Yeterli stok yok!");
        }

        product.setStockQuantity(product.getStockQuantity() - sale.getQuantitySold());
        productRepository.save(product);


        sale.setSaleDate(new Date());
        return saleRepository.save(sale);
    }



    @GetMapping("/list")
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }


    @GetMapping("/{id}")
    public Sale getSaleById(@PathVariable String id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Satış bulunamadı!"));
    }


    @PutMapping("/update/{id}")
    public Sale updateSale(@PathVariable String id, @RequestBody Sale updatedSale) {
        Sale existingSale = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Satış bulunamadı!"));


        existingSale.setProductId(updatedSale.getProductId());
        existingSale.setQuantitySold(updatedSale.getQuantitySold());
        existingSale.setTotalPrice(updatedSale.getTotalPrice());
        existingSale.setSaleDate(updatedSale.getSaleDate());

        return saleRepository.save(existingSale);
    }



    @DeleteMapping("/delete/{id}")
    public void deleteSale(@PathVariable String id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Satış bulunamadı!"));


        Product product = productRepository.findById(sale.getProductId())
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı!"));
        product.setStockQuantity(product.getStockQuantity() + sale.getQuantitySold());
        productRepository.save(product);

        saleRepository.deleteById(id);
    }


    @GetMapping("/sales-by-date")
    public List<Sale> getSalesByDate(@RequestParam String date) {
        try {

            Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
            Date endDate = new Date(startDate.getTime() + 24 * 60 * 60 * 1000 - 1); // Günün sonuna kadar


            List<Sale> sales = saleRepository.findAll();
            return sales.stream()
                    .filter(sale -> sale.getSaleDate().after(startDate) && sale.getSaleDate().before(endDate))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Tarih formatı hatalı veya işlem başarısız!", e);
        }
    }

    @GetMapping("/sales-summary-by-date")
    public SalesSummary getSalesSummaryByDate(@RequestParam String date) {
        try {
            Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
            Date endDate = new Date(startDate.getTime() + 24 * 60 * 60 * 1000 - 1); // Günün sonuna kadar

            List<Sale> sales = saleRepository.findAll();
            List<Sale> filteredSales = sales.stream()
                    .filter(sale -> sale.getSaleDate().after(startDate) && sale.getSaleDate().before(endDate))
                    .collect(Collectors.toList());

            long totalSalesCount = filteredSales.size();
            double totalSalesAmount = filteredSales.stream()
                    .mapToDouble(Sale::getTotalPrice)
                    .sum();

            SalesSummary summary = new SalesSummary();
            summary.setTotalSalesCount(totalSalesCount);
            summary.setTotalSalesAmount(totalSalesAmount);
            return summary;
        } catch (Exception e) {
            throw new RuntimeException("Tarih formatı hatalı veya işlem başarısız!", e);
        }
    }
}
