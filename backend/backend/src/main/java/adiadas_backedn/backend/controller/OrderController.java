package adiadas_backedn.backend.controller;

import adiadas_backedn.backend.dto.OrderRequest;
import adiadas_backedn.backend.model.Order;
import adiadas_backedn.backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable String id) {
        Order order = orderService.getOrderById(id);
        return order != null ? ResponseEntity.ok(order) : ResponseEntity.notFound().build();
    }

    // Sửa đoạn catch ở phương thức createOrder
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) { // Đổi <Order> thành <?>
        try {
            Order newOrder = orderService.createOrder(orderRequest);
            return ResponseEntity.ok(newOrder);
        } catch (Exception e) {
            e.printStackTrace();
            // Trả về lỗi chi tiết dưới dạng String
            return ResponseEntity.internalServerError().body("LỖI SERVER: " + e.getMessage());
        }
    }
}
